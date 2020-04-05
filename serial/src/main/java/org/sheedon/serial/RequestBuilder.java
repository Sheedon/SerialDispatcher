package org.sheedon.serial;

/**
 * 请求构造内容
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/26 23:50
 */
public class RequestBuilder {

    RequestBody body;
    int delayMilliSecond = -1;
    String backName;
    Object tag;

    public RequestBuilder() {
        this.body = createRequestBody();
    }

    protected RequestBody createRequestBody() {
        return new RequestBody();
    }

    public RequestBuilder(Request request) {
        this.body = request.body;
        this.delayMilliSecond = request.delayMilliSecond;
        this.tag = request.tag;
    }

    public RequestBody getBody() {
        if (body == null)
            synchronized (this) {
                if (body == null) {
                    body = createRequestBody();
                }
            }
        return body;
    }

    /**
     * 设置请求消息
     *
     * @param body 消息内容
     * @return Builder
     */
    public RequestBuilder body(RequestBody body) {
        if (body == null) throw new NullPointerException("requestBody == null");
        this.body = body;
        return this;
    }

    /**
     * 单次请求超时额外设置
     *
     * @param delayMilliSecond 延迟时间（毫秒）
     * @return Builder
     */
    public RequestBuilder delayMilliSecond(int delayMilliSecond) {
        this.delayMilliSecond = delayMilliSecond;
        return this;
    }

    /**
     * 反馈主题名
     *
     * @param backName 反馈名
     * @return Builder
     */
    public RequestBuilder backName(String backName) {
        if (backName == null || backName.isEmpty())
            return this;

        this.backName = backName;
        return this;
    }

    /**
     * Attaches {@code tag} to the request. It can be used later to cancel the request. If the tag
     * is unspecified or null, the request is canceled by using the request itself as the tag.
     */
    public RequestBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    /**
     * 设置数据
     *
     * @param data 数据
     * @return Builder
     */
    public RequestBuilder data(String data) {
        checkRequestBody();
        if (data == null)
            data = "";

        getBody().setData(data);
        return this;
    }

    /**
     * 核实请求内容是否为空
     */
    private void checkRequestBody() {
        if (getBody() != null) {
            return;
        }

        body(createRequestBody());
    }

    public Request build() {
        return new Request(this);
    }
}
