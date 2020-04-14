package org.sheedon.serial;

import android.annotation.SuppressLint;


import androidx.annotation.NonNull;

import org.sheedon.serial.serialport.SerialPort;
import org.sheedon.serial.serialport.SerialRealCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.sheedon.serial.Util.checkNotNull;

/**
 * 串口客户端
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/21 8:52
 */
public class SerialClient implements SerialRealCallback, RealClient,
        Call.Factory, Observable.Factory {

    private static final String TAG = "SERIAL_CLIENT";

    // 串口
    SerialPort port;
    // 串口名
    String name;

    // 调度器
    Dispatcher dispatcher;
    // 超时毫秒
    long timeOutMilliSecond;
    // 串口数据反馈
    SerialRealCallback callback;

    SerialClient(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.timeOutMilliSecond = builder.messageTimeout * 1000;
        this.callback = builder.callback;
        this.name = builder.name;
        this.dispatcher.setConverterFactories(Collections.unmodifiableList(builder.converterFactories));

        checkNotNull(builder.path, "path is null");
        try {
            port = new SerialPort(builder.path, builder.baudRate, builder.flags, this, this.dispatcher);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建Call
     *
     * @param request 请求数据
     * @return Call
     */
    @Override
    public Call newCall(Request request) {
        return RealCall.newRealCall(this, request);
    }

    /**
     * 创建观察者
     *
     * @param request 请求数据
     * @return Observable
     */
    @Override
    public Observable newObservable(Request request) {
        return RealObservable.newRealObservable(this, request);
    }


    @Override
    public void onCallback(ResponseBody data) {
        dispatcher.enqueueSerialCallback(name, data);

        if (callback == null)
            return;

        callback.onCallback(data);
    }

    /**
     * 获取调度器
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * 获取超时毫秒数
     */
    public long timeOutMilliSecond() {
        return timeOutMilliSecond;
    }


    public static final class Builder {

        Dispatcher dispatcher;
        String path;
        int baudRate;
        int flags;

        String name;

        int messageTimeout;

        SerialRealCallback callback;

        private final List<DataConverter.Factory> converterFactories = new ArrayList<>();

        @SuppressLint("DefaultLocale")
        public Builder() {
            dispatcher = new Dispatcher();
            baudRate = 9600;
            flags = 0;
            messageTimeout = 5;
            name = String.format("%d", this.getClass().hashCode());
        }

        /**
         * 设置用于设置策略和执行异步请求的调度程序。不能为null。
         */
        public Builder dispatcher(Dispatcher dispatcher) {
            if (dispatcher == null) throw new IllegalArgumentException("dispatcher == null");
            this.dispatcher = dispatcher;
            return this;
        }

        /**
         * 设置串口路径
         */
        public Builder path(@NonNull String path) {
            this.path = path;
            return this;
        }

        /**
         * 设置串口路径
         */
        public Builder baudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        /**
         * 设置标志位
         */
        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        /**
         * 串口名
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 设置信息请求超时时间
         */
        public Builder messageTimeout(int messageTimeout) {
            if (messageTimeout < 0)
                return this;

            this.messageTimeout = messageTimeout;
            return this;
        }

        /**
         * 消息处理工具
         */
        public Builder addConverterFactory(DataConverter.Factory factory) {
            converterFactories.add(checkNotNull(factory, "DataConverter.Factory == null"));
            return this;
        }

        /**
         * 设置反馈监听
         */
        public Builder callback(SerialRealCallback callback) {
            this.callback = callback;
            return this;
        }

        public SerialClient build() {

            if (converterFactories.size() == 0) {
                throw new IllegalStateException("converterFactories is null.");
            }
            return new SerialClient(this);
        }


    }

}
