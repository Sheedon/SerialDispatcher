package org.sheedon.serial;

/**
 * 串口基本处理工厂
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/14 14:29
 */
public interface SerialFactory {

    Call newCall(Request request);

    Observable newObservable(Request request);
}
