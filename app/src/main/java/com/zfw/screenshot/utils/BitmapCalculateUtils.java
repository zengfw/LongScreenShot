package com.zfw.screenshot.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Random;

public class BitmapCalculateUtils {


    // 暂时有个bug，就是一些情况返回0
    public static int getSameHeight(Bitmap bm1, Bitmap bm2) {

        int ret = 0;
        int len = bm1.getWidth();
        int h1 = bm1.getHeight();
        int h2 = bm2.getHeight();
        int[] pixel_1 = new int[len];
        int[] pixel_2 = new int[len];
        int y1 = h1 - 1;
        int y2 = h2 - 1;
        bm1.getPixels(pixel_1, 0, len, 0, y1, len, 1);
        while (true) {
            bm2.getPixels(pixel_2, 0, len, 0, y2, len, 1);
            if (isSameRow(pixel_1, pixel_2, len)) {
                int currY1 = y1 - 1;
                int currY2 = y2 - 1;
                int count = 50;
                int flag = 0;
                for(int i = 0; i < count; i++) {
                    if (currY2 < 0) {
                        break;
                    }
                    bm1.getPixels(pixel_1, 0, len, 0, currY1, len, 1);
                    bm2.getPixels(pixel_2, 0, len, 0, currY2, len, 1);
                    if(isSameRow(pixel_1, pixel_2, len)) {
                        flag++;
                        currY1--;
                        currY2--;
                        if(flag == count) {
                            return y2;
                        }
                    } else {
                        bm1.getPixels(pixel_1, 0, len, 0, y1, len, 1);
                        break;
                    }
                }
            }
            y2--;
            if (y1 < 0 || y2 < 0) {
                break;
            }
        }
        return ret;
    }


    public static boolean isSameRow(int[] pixel_1, int[] pixel_2, int length) {
        Random random = new Random();
//        for (int i = 0; i < 100; i++) {
//            int index = random.nextInt(length);
//            if (pixel_1[index] != pixel_2[index]) {
//                return false;
//            }
//        }
        for (int i = 0; i < length; i++) {
            if (pixel_1[i] != pixel_2[i]) {
                return false;
            }
        }
        return true;
    }

}
