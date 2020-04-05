package org.sheedon.serial.serialport;

import org.sheedon.serial.ResponseBody;

/**
 * 真实串口反馈接口
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/21 9:38
 */
public interface SerialRealCallback {

    void onCallback(ResponseBody data);

}
