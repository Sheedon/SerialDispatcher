package org.sheedon.serial;

import org.sheedon.serial.internal.CharsUtils;

/**
 * @Description: 串口请求类
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/10 13:26
 */
public final class Request {

    final RequestBody body;
    final int delayMilliSecond;
    final String backName;
    final Object tag;

    private long backNameCode = -1;


    public int delayMilliSecond() {
        return delayMilliSecond;
    }

    public String backName() {
        return backName;
    }

    /**
     * 反馈名Code
     */
    public long backNameCode() {
        if (backNameCode != -1)
            return backNameCode;

        if (backName == null || backName.isEmpty())
            return -1;

        return calcBackNameCode(CharsUtils.hexStringToBytes(backName));
    }

    /**
     * 计算反馈名Code
     *
     * @param nameBytes 反馈名字节数组
     */
    private long calcBackNameCode(byte[] nameBytes) {
        long code = 0;
        for (byte nameByte : nameBytes) {
            code = code * 256 + (nameByte & 0xFF);
        }
        return code;
    }

    public Request(RequestBuilder builder) {
        this.body = builder.body;
        this.delayMilliSecond = builder.delayMilliSecond;
        this.backName = builder.backName;
        this.tag = builder.tag != null ? builder.tag : this;
    }

    public RequestBody getBody() {
        return body;
    }
}
