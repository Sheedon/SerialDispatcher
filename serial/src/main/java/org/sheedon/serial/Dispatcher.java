package org.sheedon.serial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据处理调度器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/11 12:46
 */
public class Dispatcher {

    // 单线程池处理任务提交
    private final ExecutorService publishService = Executors.newSingleThreadExecutor();
    // 单线程池处理超时操作
    private final ExecutorService timeOutService = Executors.newSingleThreadExecutor();
    // 反馈数据，可能是高并发，使用缓存线程池
    private final ExecutorService callbackService = Executors.newCachedThreadPool();

    // 发送需要反馈的请求集合
    private final Map<String, ReadyTask> readyCalls = new ConcurrentHashMap<>();


    // 数据反馈处理集合
    private final Map<Long, Deque<String>> dataCalls = new LinkedHashMap<>();

    // 额外反馈集合
    private final Map<Long, Callback> callbacks = new ConcurrentHashMap<>();

    // 超时处理
    private TimeOutRunnable timeOut = new TimeOutRunnable(this);

    // 转化工厂
    protected List<DataConverter.Factory> converterFactories;

    protected Dispatcher() {
    }

    /**
     * 设置转化工厂集合
     */
    public void setConverterFactories(List<DataConverter.Factory> converterFactories) {
        this.converterFactories = Collections.unmodifiableList(converterFactories);
    }

    /**
     * 核实转化为对应的反馈名
     *
     * @param topic 主题
     */
    public DataConverter<byte[], Long> callbackNameCodeConverter(byte[] topic) {
        return nextCallbackNameCodeConverter(null, topic);
    }

    private DataConverter<byte[], Long> nextCallbackNameCodeConverter(
            @Nullable DataConverter.Factory skipPast, byte[] topic) {
        if (converterFactories == null || converterFactories.size() == 0)
            throw new IllegalStateException("converterFactories == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            DataConverter<byte[], Long> converter =
                    converterFactories.get(i).callbackNameCodeConverter(topic);
            if (converter != null) {
                return converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
                .append(Arrays.toString(topic))
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * 串口数据反馈
     *
     * @param name 串口名
     * @param data 内容
     */
    synchronized void enqueueSerialCallback(String name, ResponseBody data) {
        enqueueCallback(new SerialRunnable(this, name, data));
    }

    /**
     * 核实数据转化器
     */
    public DataConverter<SafetyByteBuffer, DataCheckBean> checkDataConverter() {
        return nextCheckDataConverter(null);
    }

    private DataConverter<SafetyByteBuffer, DataCheckBean> nextCheckDataConverter(
            @Nullable DataConverter.Factory skipPast) {
        if (converterFactories == null || converterFactories.size() == 0)
            throw new IllegalStateException("converterFactories == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            DataConverter<SafetyByteBuffer, DataCheckBean> converter =
                    ((DataConverterFactory) converterFactories.get(i)).checkDataConverter();
            if (converter != null) {
                return converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate checkDataConverter")
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * 任务入队，执行消息
     *
     * @param call 异步消息
     */
    public synchronized void enqueue(Runnable call) {
        publishService.execute(call);
    }

    public void addCallback(long backNameCode, Callback callback) {
        callbacks.put(backNameCode, callback);
    }

    /**
     * 是否存在指定反馈
     *
     * @param backNameCode 反馈名
     */
    public boolean hasCallback(long backNameCode) {
        Callback callback = callbacks.get(backNameCode);
        return callback != null;
    }

    public void removeCallback(long backNameCode) {
        callbacks.remove(backNameCode);
    }


    protected synchronized void enqueueCallback(Runnable runnable) {
        callbackService.execute(runnable);
    }

    /**
     * 新增反馈信息
     *
     * @param call 反馈call
     */
    synchronized DelayEvent addTaskAndNetCall(AsyncCallImpl call) {

        // 超时事件
        DelayEvent event = DelayEvent.build(call.id(), call.delayDate());

        // 添加准备反馈任务集合
        readyCalls.put(call.id(), ReadyTask.build(call.id(), call.backNameCode(), call.callback(), event));

        // 添加网络反馈集合
        dataCalls.put(call.backNameCode(), getNetCallDeque(call.backNameCode(), call.id()));

        return event;
    }

    /**
     * 添加本地超时反馈
     *
     * @param event 超时事件
     */
    public synchronized void addLocalTimeOutCall(DelayEvent event) {

        // 添加超时反馈集合数据，采用延迟队列
        timeOut.addEvent(event);
        // 若运行状态则会依次执行延迟队列
        // 未运行，线程执行
        if (timeOut.isRunning())
            return;

        timeOutService.execute(timeOut);
    }

    /**
     * 填充反馈集合
     *
     * @param code 反馈主题Code
     * @param id   UUID
     * @return Deque<String>
     */
    private Deque<String> getNetCallDeque(long code, String id) {
        Deque<String> callbacks = dataCalls.get(code);
        if (callbacks == null)
            callbacks = new ArrayDeque<>();

        callbacks.add(id);

        return callbacks;
    }


    /**
     * 反馈数据
     * 处理移除动作
     *
     * @param id 请求UUID
     */
    public synchronized void finishedByNet(String id, long backNameCode, Response response) {

        noticeCallback(backNameCode, response);

        if (id == null) {
            return;
        }

        ReadyTask task = distributeTask(id);
        if (task == null)
            return;

        // 获取反馈call，执行反馈消息
        Callback callback = task.getCallback();
        if (callback != null) {
            callback.onResponse(response);
        }

        // 移除Call
        removeCall(task);

        // 移除本地超时任务
        if (task.getEvent() != null)
            timeOut.removeEvent(task.getEvent());

    }

    /**
     * 反馈通知
     *
     * @param backNameCode 反馈名Code
     * @param response     反馈结果
     */
    private void noticeCallback(long backNameCode, Response response) {
        if (backNameCode == -1)
            return;

        Callback callback = callbacks.get(backNameCode);

        if (callback == null)
            return;

        callback.onResponse(response);
    }

    /**
     * 本地超时反馈数据
     * 处理移除动作
     *
     * @param id 请求UUID
     */
    public synchronized void finishedByLocal(String id, Throwable throwable) {
        ReadyTask task = distributeTask(id);
        if (task == null)
            return;

        // 获取反馈call，反馈错误消息
        Callback callback = task.getCallback();
        if (callback != null) {
            callback.onFailure(throwable);
        }

        // 移除Call
        removeCall(task);
    }


    /**
     * 获取任务
     *
     * @param id UUID
     * @return 任务数据
     */
    private ReadyTask distributeTask(String id) {
        if (readyCalls.size() == 0 || id == null)
            return null;

        return readyCalls.get(id);
    }

    /**
     * 移除反馈Call
     *
     * @param task 请求任务
     */
    private synchronized void removeCall(@NonNull ReadyTask task) {

        // 移除网络反馈监听
        Deque<String> deque = dataCalls.get(task.getBackNameCode());

        if (deque != null && deque.size() > 0)
            deque.remove(task.getId());

        readyCalls.remove(task.getId());
    }


    /**
     * 通过反馈名称得到网络集合中第一个数据，并且移除
     *
     * @param backNameCode 反馈名称Code
     * @return uuid
     */
    public String findNetByBackNameToFirst(long backNameCode) {
        synchronized (dataCalls) {
            Deque<String> deque = dataCalls.get(backNameCode);
            if (deque == null || deque.size() == 0)
                return null;

            return deque.removeFirst();
        }
    }
}
