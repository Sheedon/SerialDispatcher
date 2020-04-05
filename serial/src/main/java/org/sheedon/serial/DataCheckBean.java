package org.sheedon.serial;

/**
 * 内容核实model
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 22:41
 */
public class DataCheckBean {
    private ResponseBody body;
    private int endIndex;

    public static DataCheckBean build(ResponseBody body, int endIndex) {
        DataCheckBean bean = new DataCheckBean();
        bean.body = body;
        bean.endIndex = endIndex;
        return bean;
    }

    public ResponseBody getBody() {
        return body;
    }

    public int getEndIndex() {
        return endIndex;
    }
}
