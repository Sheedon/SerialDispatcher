package org.sheedon.serial;

/**
 * 调度封装的基本接口
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/11 12:46
 */
public interface Call extends Cloneable {
    /**
     * Returns the original request that initiated this call.
     */
    Request request();


    void publishNotCallback();


    <R extends Response> void enqueue(Callback<R> callback);

    <R extends Response> void bind(Callback<R> callback);

    void unBind();

    /**
     * Cancels the request, if possible. Requests that are already complete cannot be canceled.
     */
    void cancel();


    boolean isExecuted();

    boolean isCanceled();


    Call clone();

    interface Factory {
        Call newCall(Request request);
    }
}
