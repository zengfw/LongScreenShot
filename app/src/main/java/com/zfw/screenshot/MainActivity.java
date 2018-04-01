package com.zfw.screenshot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.zfw.screenshot.adapter.ImageListAdapter;
import com.zfw.screenshot.service.FloatWindowsService;
import com.zfw.screenshot.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;


// 作为一个库的原则之一，就是即插即用，不需要导入任何其他多余的图片。如果想把长截图的功能开源出去，就需要符合这种要求。
public class MainActivity extends AppCompatActivity {

    private ListView img_list;
    private ImageListAdapter mAdapter;
    private List<String> filePathList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img_list = findViewById(R.id.img_list);
        mAdapter = new ImageListAdapter(filePathList);
        img_list.setAdapter(mAdapter);

        int result = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
        } else {
            filePathList.clear();
            filePathList.addAll(FileUtils.getFileList());
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0) {
                filePathList.clear();
                filePathList.addAll(FileUtils.getFileList());
                mAdapter.notifyDataSetChanged();
            }
        }
    }


    public static final int REQUEST_MEDIA_PROJECTION = 18;
    MediaProjectionManager mediaProjectionManager;

    public void start(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        if(mediaProjectionManager != null) {
            return;
        }
        // How to use MediaProjectionManager
        // 1.get an instance of MediaProjectionManager
        mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // 2.create the permissions intent and show it to user
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    // 3.handle the onActivityResult callback to get a MediaProjection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:

                if (resultCode == RESULT_OK && data != null) {
                    FloatWindowsService.setResultData(data);
                    mediaProjection = this.mediaProjectionManager.getMediaProjection(resultCode, data);
                    startService(new Intent(getApplicationContext(), FloatWindowsService.class));
                    moveTaskToBack(false);
                }
                break;
        }

    }

    private static MediaProjection mediaProjection = null;

    public static MediaProjection getMediaProjection()
    {
        return mediaProjection;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, FloatWindowsService.class));
        if (mediaProjection != null)
        {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        filePathList.clear();
        filePathList.addAll(FileUtils.getFileList());
        mAdapter.notifyDataSetChanged();
    }

}
