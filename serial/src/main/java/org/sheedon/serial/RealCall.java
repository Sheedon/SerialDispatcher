package org.sheedon.serial;


import java.util.Date;
import java.util.UUID;

/**
 * 真实反馈类
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/11 20:16
 */
final class RealCall implements Call {

    private final RealClient client;
    private final Request originalRequest;

    // Guarded by this.
    private boolean executed;

    private RealCall(SerialClient client, Request originalRequest) {
        this.client = client;
        this.originalRequest = originalRequest;
    }

    /**
     * 新增反馈类
     */
    static RealCall newRealCall(SerialClient client, Request originalRequest) {
        // Safely publish the Call instance to the EventListener.
        // call.eventListener = client.eventListenerFactory().create(call);
        return new RealCall(client, originalRequest);
    }

    public Request request() {
        return originalRequest;
    }

    /**
     * 新增无反馈请求
     */
    @Override
    public void publishNotCallback() {
        enqueue(null);
    }

    /**
     * 新增有消息反馈的数据
     *
     * @param responseCallback 请求反馈
     */
    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        client.dispatcher().enqueue(new AsyncCall(client, originalRequest, responseCallback));
    }

    /**
     * 取消
     */
    @Override
    public void cancel() {

    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call clone() {
        return null;
    }


    final class AsyncCall extends NamedRunnable
            implements AsyncCallImpl {

        private final Callback responseCallback;
        private final RealClient client;
        private final Request originalRequest;
        private String id;
        private Date delayDate;

        AsyncCall(RealClient client, Request originalRequest, Callback responseCallback) {
            super("AsyncCall %s", new Object());
            this.client = client;
            this.originalRequest = originalRequest;
            this.responseCallback = responseCallback;
        }

        public String backName() {
            return originalRequest.backName();
        }


        public Callback callback() {
            return responseCallback;
        }

        public String id() {
            return id;
        }

        public Date delayDate() {
            return delayDate;
        }

        public Request request() {
            return originalRequest;
        }

        long delayMillis() {
            long delayMillis = originalRequest.delayMilliSecond();
            return delayMillis <= 0 ? client.timeOutMilliSecond() : delayMillis;
        }

        public RealCall get() {
            return RealCall.this;
        }

        @Override
        protected void execute() {

            // 1. 获取是否需要反馈，有反馈监听才需要添加反馈
            // 2. 无需反馈，直接发送完成
            // 3. 需要反馈，添加任务队列Map<name,listener>
            boolean isNeedCallback = responseCallback != null;
            DelayEvent delayEvent = null;

            if (isNeedCallback) {
                id = UUID.randomUUID().toString();
                delayDate = new Date(System.currentTimeMillis() + delayMillis());
                delayEvent = client.dispatcher().addTaskAndNetCall(this);
            }


            SerialClient serialClient = (SerialClient) client;
            String data = originalRequest.getBody().getData();
            if (data == null || data.isEmpty()) {
                client.dispatcher().finishedByLocal(id(), new IllegalArgumentException("data is null"));
                return;
            }

            if(serialClient.port == null){
                client.dispatcher().finishedByLocal(id(), new IllegalArgumentException("port is null"));
                return;
            }

            boolean isSuccess = serialClient.port.sendMessage(data);

            if (!isSuccess) {
                client.dispatcher().finishedByLocal(id(), new IllegalStateException("publish is failure"));
                return;
            }

            if (isNeedCallback) {
                client.dispatcher().addLocalTimeOutCall(delayEvent);
            }
        }
    }
}
