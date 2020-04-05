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

    private String startBit;
    private String messageBit;
    private String parityBit;
    private String endBit;
    private String info;

    public static ResponseBody build(String startBit, String messageBit,
                                     String parityBit, String endBit, String info) {
        ResponseBody body = new ResponseBody();
        body.startBit = startBit;
        body.messageBit = messageBit;
        body.parityBit = parityBit;
        body.endBit = endBit;
        body.info = info;
        return body;
    }

    public String getStartBit() {
        return startBit;
    }

    public void setStartBit(String startBit) {
        this.startBit = startBit;
    }

    public String getMessageBit() {
        return messageBit;
    }

    public void setMessageBit(String messageBit) {
        this.messageBit = messageBit;
    }

    public String getParityBit() {
        return parityBit;
    }

    public void setParityBit(String parityBit) {
        this.parityBit = parityBit;
    }

    public String getEndBit() {
        return endBit;
    }

    public void setEndBit(String endBit) {
        this.endBit = endBit;
    }

    public String getBody() {
        return info;
    }

    public void close() {

    }
}
