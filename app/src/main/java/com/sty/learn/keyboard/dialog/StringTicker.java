package com.sty.learn.keyboard.dialog;


import android.view.View;

final class StringTicker implements Runnable {
    static StringTicker instance_;
    boolean running_ = true;
    int start_;
    int speed_;
    int x_;
    int y_;
    int width_;
    int height_;
    boolean waiting_ = true;
    long timeout_;
    Thread thread_;
    private View currentView;

    final static int FLPS = 8000;

    int getStart() {
        return start_;
    }

    void setStart(int start) {
        start_ = start;
    }

    void scrollRunOver() {
        running_ = false;
        thread_ = null;
        instance_ = null;
    }

    void initTicker(int start, int speed, int x, int y, int width, int height) {
        start_ = start;
        speed_ = speed;
        x_ = x;
        y_ = y;
        width_ = width;
        height_ = height;
    }

    void notifyTicker() {
        synchronized (this) {
            waiting_ = false;
            running_ = true;
            if (thread_ == null || !thread_.isAlive()) {
                thread_ = new Thread(this);
                thread_.start();
            } else {
                this.notify();
            }
        }
    }

    void pauseTicker() {
        start_ = 0;
        waiting_ = true;
    }

    boolean isPause() {
        return waiting_;
    }

    public void run() {
        while (running_) {
            try {
                synchronized (this) {
                    if (waiting_) {
                        timeout_ = System.currentTimeMillis();
                        wait(FLPS);
                        timeout_ = System.currentTimeMillis() - timeout_;
                        if (timeout_ >= FLPS) {
                            running_ = false;
                            waiting_ = false;
                            thread_ = null;
                            break;
                        }
                    }
                }
                Thread.sleep(speed_);
            } catch (InterruptedException ex) {
                // LPUtils.printException(ex);
            }
            if (running_) {
                if (currentView != null) {
                    currentView.postInvalidate();
                }
            }
        }
    }

    static StringTicker instance() {
        if (instance_ == null) {
            instance_ = new StringTicker();
        }
        return instance_;
    }

    int strCurrentIndex = 0;

}
