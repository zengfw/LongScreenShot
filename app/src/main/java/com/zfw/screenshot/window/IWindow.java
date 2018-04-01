package com.zfw.screenshot.window;

public interface IWindow {

    void close();

    void hide();

    void setUpListener(EventListener paramEventListener);

    void show();
}
