package org.sheedon.demo.converters;


import org.sheedon.serial.DataConverter;
import org.sheedon.serial.internal.CharsUtils;

import java.util.Arrays;

/**
 * @Description: java类作用描述
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 0:45
 */
public class CallbackRuleConverter implements DataConverter<byte[], Long> {

    CallbackRuleConverter() {

    }

    // 数据格式
    // 协议头  数据长度位  子控设备地址  命令类型    消息体    CRC16校验
    // 7A      0800         01              03         01       B07A
    @Override
    public Long convert(byte[] value) {
        if (value == null || value.length < 3)
            return -1L;

        return (long) (byteToHex(value[1]) * 16 * 16 + byteToHex(value[2]));
    }

    private int byteToHex(byte b) {
        if (b < 0)
            return b & 0xff;
        return b;
    }
}
