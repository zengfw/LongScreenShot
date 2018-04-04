package com.zfw.screenshot.utils;

import android.graphics.Bitmap;

public class BitmapCalculateUtils {

    public static int calculateSamePart(Bitmap bm1, Bitmap bm2) {
        int targetH = 100;
        int len = bm1.getWidth();
        int h1 = bm1.getHeight();
        int h2 = bm2.getHeight();
        int[] pixel_1 = new int[len];
        int[] pixel_2 = new int[len];

        int y1 = h1 - 1;
        int y2 = h2 - 1;
        int currY = 0;

        bm1.getPixels(pixel_1, 0, len, 0, y1, len, 1);

        for(; y2 > 0; y2--) {
            bm2.getPixels(pixel_2, 0, len, 0, y2, len, 1);
            if(isSameRow(pixel_1, pixel_2, len)) {
                for(int i = 0; i < targetH; i++) {
                    y1--;
                    y2--;
                    bm1.getPixels(pixel_1, 0, len, 0, y1, len, 1);
                    bm2.getPixels(pixel_2, 0, len, 0, y2, len, 1);
                    if(!isSameRow(pixel_1, pixel_2, len)) {
                        y1 = h1 - 1;
                        bm1.getPixels(pixel_1, 0, len, 0, y1, len, 1);
                        break;
                    }
                    if(i == targetH - 1) {
                        return currY;
                    }
                }
            } else {
                currY = y2;
            }
        }
        // 这个currY只是返回从低currY行开始，bitmap不同
        return currY;
    }

    /**
     * 3.非理想状态的算法：求出相似部分的高度
     * 这个算法有问题
     * @param bm1
     * @param bm2
     * @return
     */
    public static int calculateSamePart4(Bitmap bm1, Bitmap bm2) {
        int bmW = bm1.getWidth();
        int bm1H = bm1.getHeight();
        int bm2H = bm2.getHeight();

        int[] pixel_1 = new int[bmW];
        int[] pixel_2 = new int[bmW];

        int middleY = 0;

        int y1 = bm1H - 1;
        bm1.getPixels(pixel_1, 0, bmW, 0, y1, bmW, 1);

        for(int y2 = 0; y2 < bm2H; y2++) {
            bm2.getPixels(pixel_2, 0, bmW, 0, y2, bmW, 1);
            if(isSameRow(pixel_1, pixel_2, bmW)) {
                middleY = y2;
                break;
            }
        }

        return middleY * 2;
    }

    /**
     * 判断某一行像素是否相等
     * @param pixel_1
     * @param pixel_2
     * @param length
     * @return
     */
    public static boolean isSameRow(int[] pixel_1, int[] pixel_2, int length) {
        for (int i = 0; i < length; i++) {
            if (pixel_1[i] != pixel_2[i]) {
                return false;
            }
        }
        return true;
    }

}
