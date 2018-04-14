package com.zfw.screenshot.utils;

import android.graphics.Bitmap;

public class SewUtils {

    static {
        System.loadLibrary("sew");
    }

    public static Bitmap merge(Bitmap bmp1, Bitmap bmp2) {
        int samePart = ImageUtils.compare(bmp1, bmp2);
        int cropHeight = bmp2.getHeight() - samePart;
        Bitmap result;
        if (cropHeight > 0) {
            int len = bmp1.getWidth();
            int h0 = bmp1.getHeight() + cropHeight;
            result = Bitmap.createBitmap(len, h0, Bitmap.Config.ARGB_8888);
            merge(result, bmp1, bmp2, h0, bmp1.getHeight(), bmp2.getHeight(), samePart, len);
        } else {
            return bmp1;
        }
        bmp1.recycle();
        bmp2.recycle();
        bmp1 = null;
        bmp2 = null;
        return result;
    }

    public native static void merge(Bitmap result, Bitmap bmp1, Bitmap bmp2, int h0, int h1, int h2, int samePart, int len);

    public native static int compare(Bitmap bm1, Bitmap bm2, int h1, int h2, int len);

}
