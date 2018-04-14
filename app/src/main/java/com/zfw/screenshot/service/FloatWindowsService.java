package com.zfw.screenshot.service;

import android.annotation.TargetApi;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
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
import com.zfw.screenshot.utils.FileUtils;
import com.zfw.screenshot.utils.ImageUtils;
import com.zfw.screenshot.utils.PxUtils;
import com.zfw.screenshot.utils.SewUtils;
import com.zfw.screenshot.window.EventListener;
import com.zfw.screenshot.window.TouchWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FloatWindowsService extends Service implements EventListener {

    public static final String TAG = "FloatWindowsService";

    private VirtualDisplay mVirtualDisplay;

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

        mWindowManager.addView(mFloatView, mLayoutParams);

    }

    // 截图结束的标记
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
                isStopFlag = true;
                isStop = true;
                mFloatView.setVisibility(View.GONE);
                mFloatView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.start));
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int dx = (int) e2.getRawX() - lastX;
            int dy = (int) e2.getRawY() - lastY;
            mLayoutParams.x = paramX + dx;
            mLayoutParams.y = paramY + dy;

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

    // 第一次截图完成之后的标记
    private boolean isStop = false;
    private boolean isRunning = false;
    private Handler handler = new Handler();

    private void startScreenShot() {

        handler.postDelayed(new Runnable() {
            public void run() {
                startCapture();

            }
        }, 30);
    }

    private void createImageReader() {
        // 设置截屏的宽高
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
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
            SaveTask mSaveTask = new SaveTask();
            mSaveTask.execute(image);
        }
    }

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
            // 每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            // 总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);

            bitmap.copyPixelsFromBuffer(buffer);
            if (!isStopFlag) {
                // 截图
                bitmap = ImageUtils.screenShotBitmap(getApplicationContext(), bitmap, false);
                if (finalImage == null) {
                    finalImage = bitmap;
                }
                if (finalImage != bitmap) {
                    finalImage = SewUtils.merge(finalImage, bitmap);
                }
            } else {
                bitmap = ImageUtils.screenShotBitmap(getApplicationContext(), bitmap, true);
                finalImage = SewUtils.merge(finalImage, bitmap);
            }
            bitmap = finalImage;
            image.close();
            File fileImage = null;
            if (bitmap != null) {
                try {
                    if (isStopFlag) {
                        fileImage = new File(FileUtils.getFileName(getApplicationContext()));
                        FileOutputStream out = new FileOutputStream(fileImage);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(fileImage);
                        media.setData(contentUri);
                        sendBroadcast(media);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
    }

    @Override
    public void onTouchSuccess(MotionEvent event) {
        if (isStop) {
            return;
        }
        handler.postDelayed(new Runnable() {
            public void run() {
                startScreenShot();
            }
        }, 100);
    }

}
