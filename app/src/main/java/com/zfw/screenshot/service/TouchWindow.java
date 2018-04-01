package com.zfw.screenshot.service;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.zfw.screenshot.R;

// 这个应该是一个透明的WindowBackground

public class TouchWindow implements IWindow {

    private Context context;
    private View floatView;
    private boolean isHide = true;
    private WindowManager.LayoutParams layoutParams;
    private EventListener listener;
    private WindowManager windowManager;

    public TouchWindow(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        this.windowManager = ((WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE));
        this.layoutParams = new WindowManager.LayoutParams();
        this.layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        this.layoutParams.format = PixelFormat.RGBA_8888;

        // 这个会拦截了全部的touch事件，导致touch事件不能传到下一层
//        this.layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;

//        这种可以点击下层的icon了，但是不能 知道什么时候触发touch事件
        this.layoutParams.flags =
                LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE
                        | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        this.floatView = LayoutInflater.from(this.context).inflate(R.layout.full_screen_float_window, null);
        this.floatView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                Log.e("TouchWindow", "onTouch");
                TouchWindow.this.listener.onTouchSuccess(event);
                return false;
            }
        });
    }

    public void close() {
        if (!this.isHide)
            this.windowManager.removeView(this.floatView);
    }

    public void hide() {
        floatView.setVisibility(View.GONE);
        this.isHide = true;
    }

    public void setUpListener(EventListener paramEventListener) {
        this.listener = paramEventListener;
    }

    public void show() {
        if (this.floatView.getParent() != null)
            this.windowManager.removeView(this.floatView);
        this.windowManager.addView(this.floatView, this.layoutParams);
        floatView.setVisibility(View.VISIBLE);

        this.isHide = false;
    }

}