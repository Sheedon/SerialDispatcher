package org.sheedon.serial;

/**
 * 串口反馈Runnable处理
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/14 23:08
 */
public class SerialRunnable extends NamedRunnable {

    // 调度器
    private final Dispatcher dispatcher;
    private final String name;
    // 内容
    private final ResponseBody data;

    SerialRunnable(Dispatcher dispatcher, String name, ResponseBody data) {
        super("SerialRunnable");
        this.dispatcher = dispatcher;
        this.name = name;
        this.data = data;
    }

    /**
     * 执行数据处理，并且反馈到调度器结束任务处理
     */
    @Override
    protected void execute() {

        // 设置反馈名称和类型的默认值
        String backName = name;

        // 借助具体客户端工具转化反馈名
        DataConverter<String, String> converter = dispatcher.callbackNameConverter(data.getBody());
        if (converter != null) {
            backName = converter.convert(data.getBody());
        }


        // 获取UUID
        String id = dispatcher.findNetByBackNameToFirst(backName);

        // 结束网络调度
        dispatcher.finishedByNet(id, backName, getResponse(data));
    }

    /**
     * 获取响应内容
     *
     * @param body 数据内容
     * @return 内容
     */
    private Response getResponse(ResponseBody body) {
        return new ResponseBuilder()
                .code(200)
                .body(body)
                .build();
    }
}
