package com.zfw.screenshot.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FileUtils {

    public static List<String> getFileList() {

        String directory = (Environment.getExternalStorageDirectory() + "/ScreenCapture/Screenshots/");
        File localFile = new File(directory);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        List<String> result = new ArrayList<>();
        File localFile1 = new File(directory);
        if (localFile1.exists()) {
            File[] arrayOfFile = localFile1.listFiles();
            ArrayList localArrayList = new ArrayList();
            int i = arrayOfFile.length;
            for (int j = 0; j < i; j++) {
                localArrayList.add(arrayOfFile[j]);
            }
            Iterator localIterator = localArrayList.iterator();
            while (localIterator.hasNext()) {
                File localFile2 = (File) localIterator.next();
                Log.d("FilePathList", localFile2.getAbsolutePath());
                result.add(localFile2.getAbsolutePath());
            }
        }
        return result;
    }

    public static final String SCREENCAPTURE_PATH = "ScreenCapture" + File.separator + "Screenshots" + File.separator;

    public static final String SCREENSHOT_NAME = "Screenshot";

    public static String getAppPath(Context context) {

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {


            return Environment.getExternalStorageDirectory().toString();

        } else {

            return context.getFilesDir().toString();
        }

    }


    public static String getScreenShots(Context context) {

        StringBuffer stringBuffer = new StringBuffer(getAppPath(context));
        stringBuffer.append(File.separator);

        stringBuffer.append(SCREENCAPTURE_PATH);

        File file = new File(stringBuffer.toString());

        if (!file.exists()) {
            file.mkdirs();
        }

        return stringBuffer.toString();

    }

    public static String getFileName(Context context) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

        String date = simpleDateFormat.format(new Date());

        StringBuilder stringBuffer = new StringBuilder(getScreenShots(context));
        stringBuffer.append(SCREENSHOT_NAME);
        stringBuffer.append("_");
        stringBuffer.append(date);
        stringBuffer.append(".png");

        return stringBuffer.toString();

    }


}
