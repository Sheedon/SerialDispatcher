package org.sheedon.serial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
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

    // 超时处理集合
    private final DelayQueue<DelayEvent> timeOutCalls = new DelayQueue<>();

    // 数据反馈处理集合
    private final Map<String, Deque<String>> dataCalls = new LinkedHashMap<>();

    // 超时处理
    private TimeOutRunnable timeOut = new TimeOutRunnable(this, timeOutCalls);

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
     * @param topic 主题
     */
    public DataConverter<String, String> callbackNameConverter(String topic) {
        return nextCallbackNameConverter(null, topic);
    }

    private DataConverter<String, String> nextCallbackNameConverter(
            @Nullable DataConverter.Factory skipPast, String topic) {
        if (converterFactories == null || converterFactories.size() == 0)
            throw new IllegalStateException("converterFactories == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            DataConverter<String, String> converter =
                    converterFactories.get(i).callbackNameConverter(topic);
            if (converter != null) {
                return converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
                .append(topic)
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
    public DataConverter<StringBuffer, DataCheckBean> checkDataConverter() {
        return nextCheckDataConverter(null);
    }

    private DataConverter<StringBuffer, DataCheckBean> nextCheckDataConverter(
            @Nullable DataConverter.Factory skipPast) {
        if (converterFactories == null || converterFactories.size() == 0)
            throw new IllegalStateException("converterFactories == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            DataConverter<StringBuffer, DataCheckBean> converter =
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
        readyCalls.put(call.id(), ReadyTask.build(call.id(), call.backName(), call.callback(), event));

        // 添加网络反馈集合
        dataCalls.put(call.backName(), getNetCallDeque(call.backName(), call.id()));

        return event;
    }

    /**
     * 添加本地超时反馈
     *
     * @param event 超时事件
     */
    public synchronized void addLocalTimeOutCall(DelayEvent event) {

        // 添加超时反馈集合数据，采用延迟队列
        timeOutCalls.add(event);
        // 若运行状态则会依次执行延迟队列
        // 未运行，线程执行
        if (timeOut.isRunning())
            return;

        timeOutService.execute(timeOut);
    }

    /**
     * 填充反馈集合
     *
     * @param name 反馈主题
     * @param id   UUID
     * @return Deque<String>
     */
    private Deque<String> getNetCallDeque(String name, String id) {
        Deque<String> callbacks = dataCalls.get(name);
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
    public synchronized void finishedByNet(String id, Response response) {

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
        Deque<String> deque = dataCalls.get(task.getBackName());

        if (deque != null && deque.size() > 0)
            deque.remove(task.getId());

        // 移除本地超时任务
        if (task.getEvent() != null)
            timeOutCalls.remove(task.getEvent());

        readyCalls.remove(task.getId());


    }


    /**
     * 通过反馈名称得到网络集合中第一个数据，并且移除
     *
     * @param backName 反馈名称
     * @return uuid
     */
    public String findNetByBackNameToFirst(String backName) {
        synchronized (dataCalls) {
            Deque<String> deque = dataCalls.get(backName);
            if (deque == null || deque.size() == 0)
                return null;

            return deque.removeFirst();
        }
    }
}
