package org.sheedon.serial;

/**
 * 真实观察者类
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/14 20:16
 */
final class RealObservable implements Observable {

    private final RealClient client;
    private final Request originalRequest;

    // Guarded by this.
    private boolean executed;

    private RealObservable(SerialClient client, Request originalRequest) {
        this.client = client;
        this.originalRequest = originalRequest;
    }


    /**
     * 新增观察者类
     */
    static RealObservable newRealObservable(SerialClient client, Request originalRequest) {
        // Safely publish the Call instance to the EventListener.
        // call.eventListener = client.eventListenerFactory().create(call);
        return new RealObservable(client, originalRequest);
    }

    public Request request() {
        return originalRequest;
    }



    @Override
    public void enqueue(Callback callback) {
        if (originalRequest == null
                || originalRequest.backName() == null
                || originalRequest.backName().isEmpty()
                || callback == null) {
            return;
        }

        client.dispatcher().addCallback(originalRequest.backName(), callback);
    }

    /**
     * 取消
     */
    @Override
    public void cancel() {
        if (originalRequest == null
                || originalRequest.backName() == null
                || originalRequest.backName().isEmpty()) {
            return;
        }
        client.dispatcher().removeCallback(originalRequest.backName());
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Observable clone() {
        return null;
    }
}
