package com.zfw.screenshot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class ImageUtils {

    public static Bitmap screenShotBitmap(Context context, Bitmap bitmap, boolean finish) {
        int scope = 0;
        if (!finish) {
            scope = PxUtils.ScreenHeight(context) / 4;
        }
        int statusHeight = PxUtils.getStatusHeight(context);
        int cropRetX = 0;
        int cropRetY = statusHeight;
        int cropWidth = PxUtils.ScreenWidth(context);
        int cropHeight = PxUtils.ScreenHeight(context) - statusHeight - scope;
        return ImageUtils.bitmapCrop(bitmap, cropRetX, cropRetY, cropWidth, cropHeight, false);
    }

    /**
     * @param bitmap
     * @param x        裁剪起始x
     * @param y        裁剪起始y
     * @param width    裁剪的宽度
     * @param height   裁剪的高度
     * @param defValue 默认方形裁剪
     * @return
     */
    public static Bitmap bitmapCrop(Bitmap bitmap, int x, int y, int width, int height, boolean defValue) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int ww = 0;
        int wh = 0;
        int retX = 0;
        int retY = 0;
        if (defValue) {
            wh = w > h ? h : w;// 裁切后所取的正方形区域边长
            ww = wh;
            retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
            retY = w > h ? 0 : (h - w) / 2;
        } else {
            retX = x;
            retY = y;
            ww = width;
            wh = height;
        }

        return Bitmap.createBitmap(bitmap, retX, retY, ww, wh, null, false);
    }

    /**
     * @param topBitmap
     * @param bottomBitmap
     * @param isBaseMax    是否以高度大的位图为准，true则小图等比拉伸，false则大图等比压缩
     * @return
     */
    public static Bitmap bitmapMerge(Bitmap topBitmap, Bitmap bottomBitmap, boolean isBaseMax) {

        if (topBitmap == null || topBitmap.isRecycled()
                || bottomBitmap == null || bottomBitmap.isRecycled()) {
            return null;
        }
        int width = 0;
        if (isBaseMax) {
            width = topBitmap.getWidth() > bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        } else {
            width = topBitmap.getWidth() < bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        }
        Bitmap tempBitmapT = topBitmap;
        Bitmap tempBitmapB = bottomBitmap;

        if (topBitmap.getWidth() != width) {
            tempBitmapT = Bitmap.createScaledBitmap(topBitmap, width, (int) (topBitmap.getHeight() * 1f / topBitmap.getWidth() * width), false);
        } else if (bottomBitmap.getWidth() != width) {
            tempBitmapB = Bitmap.createScaledBitmap(bottomBitmap, width, (int) (bottomBitmap.getHeight() * 1f / bottomBitmap.getWidth() * width), false);
        }

        int height = tempBitmapT.getHeight() + tempBitmapB.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Rect topRect = new Rect(0, 0, tempBitmapT.getWidth(), tempBitmapT.getHeight());
        Rect bottomRect = new Rect(0, 0, tempBitmapB.getWidth(), tempBitmapB.getHeight());

        Rect bottomRectT = new Rect(0, tempBitmapT.getHeight(), width, height);

        canvas.drawBitmap(tempBitmapT, topRect, topRect, null);
        canvas.drawBitmap(tempBitmapB, bottomRect, bottomRectT, null);
        return bitmap;
    }

}
