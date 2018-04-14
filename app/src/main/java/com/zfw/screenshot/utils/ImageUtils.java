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

    public static int compare(Bitmap bm1, Bitmap bm2) {
        int len = bm1.getWidth();
        int h1 = bm1.getHeight();
        int h2 = bm2.getHeight();
        int ret = SewUtils.compare(bm1, bm2, h1, h2, len);
        return ret;
    }

}
