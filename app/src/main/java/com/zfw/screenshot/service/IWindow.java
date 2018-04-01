package com.zfw.screenshot.service;

public interface IWindow {

    void close();

    void hide();

    void setUpListener(EventListener paramEventListener);

    void show();
}
