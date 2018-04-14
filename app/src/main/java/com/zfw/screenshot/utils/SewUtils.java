package com.zfw.screenshot.utils;

import android.graphics.Bitmap;

public class SewUtils {

    static {
        System.loadLibrary("sew");
    }

    public native static int compare(Bitmap bm1, Bitmap bm2, int h1, int h2, int len);

    public native static int isSameRow(int[] pixel_1, int[] pixel_2, int length);
}
