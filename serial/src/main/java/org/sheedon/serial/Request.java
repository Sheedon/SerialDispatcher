package org.sheedon.serial;

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



    public int delayMilliSecond() {
        return delayMilliSecond;
    }

    public String backName() {
        return backName;
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
