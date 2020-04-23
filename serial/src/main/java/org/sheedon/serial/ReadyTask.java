package org.sheedon.serial;

/**
 * 请求任务类
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/13 12:51
 */
public class ReadyTask {
    private String id;// 任务UUID
    private long backNameCode; // 反馈名称编号
    private Callback callback;// 反馈监听器
    private DelayEvent event;// 超时处理事件


    public static ReadyTask build(String id, long backNameCode,
                                  Callback callback, DelayEvent event) {
        ReadyTask task = new ReadyTask();
        task.id = id;
        task.backNameCode = backNameCode;
        task.callback = callback;
        task.event = event;
        return task;
    }

    public String getId() {
        return id;
    }

    public long getBackNameCode() {
        return backNameCode;
    }

    public Callback getCallback() {
        return callback;
    }

    public DelayEvent getEvent() {
        return event;
    }
}
