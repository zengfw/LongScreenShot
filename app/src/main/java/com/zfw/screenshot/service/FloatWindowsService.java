package com.zfw.screenshot.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.zfw.screenshot.MainActivity;
import com.zfw.screenshot.R;
import com.zfw.screenshot.utils.BitmapCalculateUtils;
import com.zfw.screenshot.utils.FileUtils;
import com.zfw.screenshot.utils.ImageUtils;
import com.zfw.screenshot.utils.PxUtils;
import com.zfw.screenshot.window.EventListener;
import com.zfw.screenshot.window.TouchWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FloatWindowsService extends Service implements EventListener {

    public static final String TAG = "FloatWindowsService";

    //    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private static Intent mResultData = null;


    private ImageReader mImageReader;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private GestureDetector mGestureDetector;

    private ImageView mFloatView;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;


    private TouchWindow touchWindow;

    @Override
    public void onCreate() {
        super.onCreate();

        touchWindow = new TouchWindow(getApplicationContext());
        touchWindow.setUpListener(this);

        createFloatView();
        createImageReader();
    }


    public static Intent getResultData() {
        return mResultData;
    }

    public static void setResultData(Intent mResultData) {
        FloatWindowsService.mResultData = mResultData;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createFloatView() {
        mGestureDetector = new GestureDetector(getApplicationContext(), new FloatGestureTouchListener());

        mLayoutParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        // 设置Window flag
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.x = mScreenWidth;
        mLayoutParams.y = mScreenHeight * 2 / 3 + PxUtils.dip2px(this, 60);
        mLayoutParams.width = PxUtils.dip2px(this, 60);
        mLayoutParams.height = PxUtils.dip2px(this, 60);


        mFloatView = new ImageView(getApplicationContext());
        mFloatView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.start));
        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        // ---------------------------------test match_parent layout------------------------------------
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        params.width = mScreenWidth;
        params.height = mScreenHeight;

        // ---------------------------------test match_parent layout------------------------------------

        mWindowManager.addView(mFloatView, mLayoutParams);

    }


    // 这个是截图结束的标记
    private boolean isStopFlag = false;

    private class FloatGestureTouchListener implements GestureDetector.OnGestureListener {
        int lastX, lastY;
        int paramX, paramY;

        @Override
        public boolean onDown(MotionEvent event) {
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
            paramX = mLayoutParams.x;
            paramY = mLayoutParams.y;
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!isRunning) {
                virtualDisplay();
                isRunning = true;
                isStop = false;
                mFloatView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.stop));
                touchWindow.show();
                startScreenShot();
            } else {
                // 点击stop的时候肯定是跑了两次保存截图（而且两次间隔很短）
                isStopFlag = true;
                isStop = true;
//                stopScreenShot();
                mFloatView.setVisibility(View.GONE);
                mFloatView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.start));
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 注释掉滑动
            int dx = (int) e2.getRawX() - lastX;
            int dy = (int) e2.getRawY() - lastY;
            mLayoutParams.x = paramX + dx;
            mLayoutParams.y = paramY + dy;

            Log.d(TAG, "X：" + e2.getRawX());
            Log.d(TAG, "Y：" + e2.getRawY());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    private boolean isRunning = false;
    // 这个是第一次截图完成之后的标记
    private boolean isStop = false;
    private Handler handler = new Handler();

    private void startScreenShot() {

//        mFloatView.setVisibility(View.GONE);
//        handler.postDelayed(new Runnable() {
//                public void run() {
//                    // start virtual
//                    // 在startVirtual() 方法中我们做一件事，就是获取当前屏幕内容
//                    startVirtual();
//                }
//            }, 5);

        handler.postDelayed(new Runnable() {
            public void run() {
                // capture the screen
                // 生成图片保存到本地
                startCapture();

            }
        }, 30);
    }


    private void createImageReader() {
        // 设置截屏的宽高
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
    }

    // 在startVirtual() 方法中我们做一件事，就是获取当前屏幕内容
//    public void startVirtual() {
//        if (mMediaProjection != null) {
//            virtualDisplay();
//        } else {
//            setUpMediaProjection();
//            virtualDisplay();
//        }
//    }
//
//    public void setUpMediaProjection() {
//        if (mResultData == null) {
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            startActivity(intent);
//        } else {
//            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
//        }
//    }

    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }


    /**
     * 最终得到当前屏幕的内容，注意这里mImageReader.getSurface()被传入，屏幕的数据也将会在ImageReader中的Surface中
     */
    private void virtualDisplay() {
        mVirtualDisplay = MainActivity.getMediaProjection().createVirtualDisplay("screen-mirror",
                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void startCapture() {

        // 这个方法已经被调用过，在获取另外一个新的image之前，请先关闭原有有的image
        Image image = mImageReader.acquireLatestImage();

        if (image == null) {
            startScreenShot();
        } else {
            // 现在的策略是截一次图就拼一次图，很容易影响下次的滚动截图操作。
            // 所以突然想到要么就是一次性截图好，然后全部位置标记出来，等截图结束的时候再进行拼接操作。
            SaveTask mSaveTask = new SaveTask();
            mSaveTask.execute(image);
        }
    }

    private Bitmap tempImage = null;
    private Bitmap finalImage = null;

    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            // -------------------------handler bitmap-----------------------------------------------

            // 1.策略一：把Bitmap存到一个集合列表里头。。。最后停止截图的时候再统一处理（不行）
            // 2.策略二：截一次，处理一次，拼接一次。

            if (!isStopFlag) {
                // 截取屏幕3/4的图片
                bitmap = halfTopPartBitmap(bitmap);
                // 2.第二次滚动截图，首先必须是从下往上滚动
                // 此时的bitmap是一张大小接近一半的截图
                tempImage = bitmap;
                if (finalImage == null) {
                    finalImage = tempImage;
                }
                if (finalImage != tempImage) {
                    // 方法一
                    int sameHeight = BitmapCalculateUtils.calculateSamePart4(finalImage, tempImage);

//                    int startY = BitmapCalculateUtils.calculateSamePart(finalImage, tempImage);
//                    Log.e("startY", String.valueOf(startY));

                    int cropRetX2 = 0;
                    int cropRetY2 = sameHeight;
                    int cropWidth2 = width;
                    int cropHeight2 = tempImage.getHeight() - sameHeight;
                    if (cropHeight2 > 0) {
                        tempImage = ImageUtils.imageCrop(tempImage, cropRetX2, cropRetY2, cropWidth2, cropHeight2, false);
                        // 此时的finalImage则是两个图的合并
                        finalImage = ImageUtils.mergeBitmap_TB(finalImage, tempImage, true);
                    }

                }
            } else {
                bitmap = halfBottomPartBitmap(bitmap);

                // 方法一
                // 此时的bitmap是整个屏幕的截图
                tempImage = bitmap;
                int sameHeight = BitmapCalculateUtils.calculateSamePart4(finalImage, tempImage);

//                int startY = BitmapCalculateUtils.calculateSamePart(finalImage, tempImage);
//                Log.e("startY", String.valueOf(startY));

                int statusHeight2 = ImageUtils.getStatusHeight(getApplicationContext());
                int cropRetX2 = 0;
                int cropRetY2 = statusHeight2 + sameHeight;
                int cropWidth2 = width;
                int cropHeight2 = tempImage.getHeight() - statusHeight2 - sameHeight;
                if (cropHeight2 > 0) {
                    tempImage = ImageUtils.imageCrop(tempImage, cropRetX2, cropRetY2, cropWidth2, cropHeight2, false);
                    finalImage = ImageUtils.mergeBitmap_TB(finalImage, tempImage, true);
                }


            }
            bitmap = finalImage;

//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            // -------------------------handler bitmap-----------------------------------------------
            image.close();
            File fileImage = null;
            if (bitmap != null) {
                try {
                    if (isStopFlag) {
                        fileImage = new File(FileUtils.getScreenShotsName(getApplicationContext()));
                        if (!fileImage.exists()) {
                            fileImage.createNewFile();
                        }
                        FileOutputStream out = new FileOutputStream(fileImage);
                        if (out != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                            Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(fileImage);
                            media.setData(contentUri);
                            sendBroadcast(media);
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fileImage = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    fileImage = null;
                }
            }

            if (fileImage != null) {
                return bitmap;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //预览图片
            if (mFloatView.getVisibility() == View.GONE) {
                mFloatView.setVisibility(View.VISIBLE);
                isRunning = false;
                isStopFlag = false;

                finalImage.recycle();
                finalImage = null;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent localIntent = new Intent(FloatWindowsService.this, MainActivity.class);
                        localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        FloatWindowsService.this.startActivity(localIntent);
                    }
                }, 500L);

            }

        }
    }


    /**
     * 截取上半部分的图片
     *
     * @param bitmap
     * @return
     */
    private Bitmap halfTopPartBitmap(Bitmap bitmap) {
        int statusHeight = ImageUtils.getStatusHeight(getApplicationContext());
        int cropRetX = 0;
        int cropRetY = statusHeight;
        int cropWidth = ImageUtils.ScreenWidth(getApplicationContext());
        int cropHeight = ImageUtils.ScreenHeight(getApplicationContext()) * 3 / 4 - statusHeight;
        // 每次截取一半
        return ImageUtils.imageCrop(bitmap, cropRetX, cropRetY, cropWidth, cropHeight, false);
    }

    private Bitmap halfBottomPartBitmap(Bitmap bitmap) {
        int statusHeight = ImageUtils.getStatusHeight(getApplicationContext());
        int cropRetX = 0;
        int cropRetY = statusHeight;
        int cropWidth = ImageUtils.ScreenWidth(getApplicationContext());
        int cropHeight = ImageUtils.ScreenHeight(getApplicationContext()) - statusHeight;
        // 每次截取一半
        return ImageUtils.imageCrop(bitmap, cropRetX, cropRetY, cropWidth, cropHeight, false);
    }


//    private void tearDownMediaProjection() {
//        if (mMediaProjection != null) {
//            mMediaProjection.stop();
//            mMediaProjection = null;
//        }
//    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatView != null) {
            mWindowManager.removeView(mFloatView);
        }
        stopVirtual();
//        tearDownMediaProjection();
    }

    private int currTime = 0;

    @Override
    public void onTouchSuccess(MotionEvent event) {
        if (isStop)
            return;
        handler.postDelayed(new Runnable() {
            public void run() {
                startScreenShot();
            }
            // 这里之所以要延迟100ms是因为要等isStopFlag = true的情况
            // 还有一个需要考虑的原因就是100ms也够bitmap图片拼接处理
        }, 100);
    }

}
