package org.sheedon.demo.converters;


import org.sheedon.serial.DataConverter;

/**
 * @Description: java类作用描述
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 0:45
 */
public class CallbackRuleConverter implements DataConverter<String, String> {

    CallbackRuleConverter() {

    }

    // 数据格式
    // 协议头  数据长度位  子控设备地址  命令类型    消息体    CRC16校验
    // 7A      0800         01              03         01       B07A
    @Override
    public String convert(String value) {
        if(value == null || value.isEmpty() || value.length()<10)
            return "";

        return value.substring(6,10);
    }
}
