package com.zfw.screenshot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import java.io.ByteArrayOutputStream;

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
        Bitmap result = Bitmap.createBitmap(bitmap, cropRetX, cropRetY, cropWidth, cropHeight, null, false);
        bitmap.recycle();
        bitmap = null;
        return result;
    }


    public static int compare(Bitmap bm1, Bitmap bm2) {
        int len = bm1.getWidth();
        int h1 = bm1.getHeight();
        int h2 = bm2.getHeight();
        int ret = SewUtils.compare(bm1, bm2, h1, h2, len);
        return ret;
    }

}
