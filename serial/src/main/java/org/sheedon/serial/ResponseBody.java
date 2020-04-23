package org.sheedon.serial;

/**
 * 反馈内容
 * 主要内容划分 【起始位】 + 【消息位】 + 【校验位】 + 【停止位】
 *
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/26 16:31
 */
public class ResponseBody {

    private byte[] startBit;
    private byte[] messageBit;
    private byte[] parityBit;
    private byte[] endBit;
    private byte[] info;

    public static ResponseBody build(byte[] startBit, byte[] messageBit,
                                     byte[] parityBit, byte[] endBit, byte[] info) {
        ResponseBody body = new ResponseBody();
        body.startBit = startBit;
        body.messageBit = messageBit;
        body.parityBit = parityBit;
        body.endBit = endBit;
        body.info = info;
        return body;
    }

    public byte[] getStartBit() {
        return startBit;
    }

    public void setStartBit(byte[] startBit) {
        this.startBit = startBit;
    }

    public byte[] getMessageBit() {
        return messageBit;
    }

    public void setMessageBit(byte[] messageBit) {
        this.messageBit = messageBit;
    }

    public byte[] getParityBit() {
        return parityBit;
    }

    public void setParityBit(byte[] parityBit) {
        this.parityBit = parityBit;
    }

    public byte[] getEndBit() {
        return endBit;
    }

    public void setEndBit(byte[] endBit) {
        this.endBit = endBit;
    }

    public byte[] getBody() {
        return info;
    }

    public void close() {

    }
}
