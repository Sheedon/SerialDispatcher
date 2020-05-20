package org.sheedon.serial.serialport;

import org.sheedon.serial.NamedRunnable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 定时任务保活
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/26 10:34
 */
public class SafeThread {

    private Thread thread;
    private int interval;

    private Timer pingTimer;
    private TimerTask pingTask;

    private OnThreadHandleListener listener;


    public void initConfig(int interval, OnThreadHandleListener listener) {
        this.interval = interval;
        this.listener = listener;
        startThread();
        startPing();
    }

    private void startThread() {
        if (thread == null) {
            thread = new Thread(runnable);
        }

        if (thread.isInterrupted() || !thread.isAlive()) {
            thread.start();
        }
    }

    private void startPing() {
        if (pingTimer == null) {
            pingTimer = new Timer();
        }

        if (pingTask == null) {
            pingTask = new TimerTask() {
                @Override
                public void run() {
                    if (thread == null || thread.isInterrupted() || !thread.isAlive()) {
                        startThread();
                    }
                }
            };
        }

        pingTimer.schedule(pingTask, 30 * 1000, 30 * 1000);
    }

    final NamedRunnable runnable = new NamedRunnable("SafeThread") {

        @Override
        protected void execute() {
            while (!Thread.currentThread().isInterrupted()) {

                if (listener != null) {
                    listener.readThread();
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void close() {
        if (pingTimer != null) {
            pingTimer.cancel();
            pingTimer = null;
        }

        if (pingTask != null) {
            pingTask.cancel();
            pingTask = null;
        }

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

    }

    interface OnThreadHandleListener {
        void readThread();
    }
}
