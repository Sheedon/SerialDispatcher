package org.sheedon.demo.converters;

import org.sheedon.serial.DataCheckBean;
import org.sheedon.serial.DataConverter;
import org.sheedon.serial.ResponseBody;
import org.sheedon.serial.SafetyByteBuffer;
import org.sheedon.serial.internal.CharsUtils;

import java.util.Arrays;

/**
 * 反馈内容解析器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 0:45
 */
public class CheckDataConverter implements DataConverter<SafetyByteBuffer, DataCheckBean> {

    private static final byte STARTBIT = (byte) 0xBB;
    private static final byte ENDBIT = 0x7E;

    CheckDataConverter() {

    }

    // 数据格式
    // 协议头  数据长度位  子控设备地址  命令类型    消息体    CRC16校验
    // 7A      0800         01              03         01       B07A
    @Override
    public DataCheckBean convert(SafetyByteBuffer value) {
        if (value == null || value.length() == 0) {
            return DataCheckBean.build(null, 0);
        }

        int index = value.indexOf(STARTBIT);
        if (index == -1) {
            return DataCheckBean.build(null, 0);
        }

        if (index + 5 >= value.length()) {
            return DataCheckBean.build(null, index);
        }

        // 一个内容到总长度
        byte[] lengthStr = value.substring(index + 3, index + 5);
        int length = calcLength(lengthStr);
        if (length < 0 || index + length + 7 > value.length()) {
            return DataCheckBean.build(null, index);
        }

        byte[] content = value.substring(index + 1, index + length + 6);
        boolean check = checkContent(content);
        if (check) {

            byte[] startBit = new byte[]{STARTBIT};
            byte[] messageBit = Arrays.copyOf(content, content.length - 1);
            byte[] parityBit = Arrays.copyOfRange(content, content.length - 1, content.length);
            byte[] endBit = new byte[]{ENDBIT};

            ResponseBody body = ResponseBody.build(startBit, messageBit, parityBit, endBit,
                    value.substring(index, index + length + 7));

            return DataCheckBean.build(body, index + length + 7);
        } else {
            return DataCheckBean.build(null, index + length + 7);
        }
    }

    /**
     * 获取总长度
     *
     * @param str 字符
     */
    private int calcLength(byte[] str) {
        byte highPosition = str[0];
        byte lowPosition = str[1];

        int high = highPosition & 0xFF;
        int low = lowPosition & 0xFF;

        return high * 16 * 16 + low;

    }


    /**
     * 核实内容校验码
     * 拿到校验码 后两位
     * 拿到内容 除了后两位外的数据
     *
     * @param content 内容
     * @return 校验是否一致
     */
    private boolean checkContent(byte[] content) {
        if (content.length <= 1)
            return false;

        byte checkByte = content[content.length - 1];
        byte[] contentBytes = Arrays.copyOf(content, content.length - 1);
        byte checkResult = CharsUtils.sumCheck(contentBytes);

        return checkByte == checkResult;
    }

}
