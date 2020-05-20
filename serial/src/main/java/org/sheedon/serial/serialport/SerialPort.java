package org.sheedon.serial.serialport;

import org.sheedon.serial.DataCheckBean;
import org.sheedon.serial.DataConverter;
import org.sheedon.serial.Dispatcher;
import org.sheedon.serial.SafetyByteBuffer;
import org.sheedon.serial.Util;
import org.sheedon.serial.internal.CharsUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 基础串口类，用于直接发送和接受
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/2/21 9:27
 */
public class SerialPort implements SafeThread.OnThreadHandleListener {

    private android.serialport.SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    private SerialRealCallback callback;

    private DataConverter<SafetyByteBuffer, DataCheckBean> converter;
    private SafetyByteBuffer serialData = new SafetyByteBuffer();

    private int interval;
    private SafeThread safeThread;

    /**
     * 构建 创建客户端
     *
     * @param path     路径
     * @param baudRate 波特率
     * @param flags    标志位
     * @param interval 线程间隔
     * @param callback 反馈
     */
    public SerialPort(String path, int baudRate, int flags, int interval,
                      SerialRealCallback callback, Dispatcher dispatcher)
            throws IOException, SecurityException {
        Util.checkNotNull(path, "path is null");
        Util.checkNotNull(dispatcher, "dispatcher is null");
        this.interval = interval;
        converter = Util.checkNotNull(dispatcher.checkDataConverter(), "converter is null");

        this.callback = callback;

        serialPort = new android.serialport.SerialPort(new File(path), baudRate, flags);

        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();

        safeThread = new SafeThread();
        safeThread.initConfig(interval, this);

    }

    /**
     * 真实反馈一个就足够
     *
     * @param callback 反馈
     */
    public void setCallback(SerialRealCallback callback) {
        this.callback = callback;
    }

    @Override
    public void readThread() {
        byte[] data = getDataByte();
        if (data == null || callback == null)
            return;

        serialData.append(data);
        dealWithData();
    }

    private volatile boolean isStartDealWithData = false;

    /**
     * 处理数据
     */
    private void dealWithData() {
        if (isStartDealWithData)
            return;

        isStartDealWithData = true;
        while (true) {
            DataCheckBean convert = converter.convert(serialData);
            if (convert.getEndIndex() == 0)
                break;

            if (convert.getBody() == null) {
                serialData.delete(0, convert.getEndIndex());
            } else {
                if (callback != null) {
                    callback.onCallback(convert.getBody());
                }
                serialData.delete(0, convert.getEndIndex());
            }
        }
        isStartDealWithData = false;
    }

    /**
     * @return byte[] 返回一个接收的数据
     * @throws NullPointerException 如果inputStream为空，就抛出异常
     */
    private synchronized byte[] getDataByte() throws NullPointerException {
        if (inputStream == null)
            return null;
        try {
            int size = inputStream.available();
            if (size > 0) {
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                return buffer;
            } else return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean sendMessage(String data) {
//        callback.onCallback(ResponseBody.build("7A", "08000101", "0101", "", "7A080001010101"));
//        return true;
        return setData(CharsUtils.hexStringToBytes(data));
    }

    /**
     * 发送数据
     *
     * @param bytes 显示的16进制的字符串
     */
    private boolean setData(byte[] bytes) throws NullPointerException {
        if (outputStream == null)
            return false;
        try {
            outputStream.write(bytes);
            return true;//发送成功
        } catch (IOException e) {
            e.printStackTrace();
            return false;//发送失败
        }
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        if(safeThread != null){
            safeThread.close();
            safeThread = null;
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }

        if (inputStream != null) {
            inputStream = null;
        }

        if (outputStream != null) {
            outputStream = null;
        }

    }
}
