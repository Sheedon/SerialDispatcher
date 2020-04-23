package org.sheedon.serial;

import java.util.Arrays;

/**
 * 线程安全可变字符序列
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/4/22 21:45
 */
public class SafetyByteBuffer {

    byte[] bytes;

    private transient byte[] toBytesCache;

    int count;

    public SafetyByteBuffer() {
        this(0);
    }

    public SafetyByteBuffer(int capacity) {
        bytes = new byte[capacity];
    }


    public synchronized int length() {
        return count;
    }

    /**
     * 添加字节
     *
     * @param b 字节
     * @return SafetyByteBuffer
     */
    public synchronized SafetyByteBuffer append(byte b) {
        return append(new byte[]{b});
    }

    /**
     * 添加字节数组
     *
     * @param newBytes 添加的字符数组内容
     * @return SafetyByteBuffer
     */
    public synchronized SafetyByteBuffer append(byte[] newBytes) {
        toBytesCache = null;
        int newLenght = newBytes.length;
        int allLenght = count + newLenght;
        byte[] copy = new byte[allLenght];
        System.arraycopy(bytes, 0, copy, 0, count);
        System.arraycopy(newBytes, 0, copy, count, newLenght);
        bytes = copy;
        count = allLenght;
        return this;
    }

    public synchronized SafetyByteBuffer delete(int start, int end) {
        toBytesCache = null;
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (end > count)
            end = count;
        if (start > end)
            throw new StringIndexOutOfBoundsException();
        int len = end - start;
        if (len > 0) {
            System.arraycopy(bytes, start + len, bytes, start, count - end);
            count -= len;
        }
        return this;
    }


    public synchronized byte[] substring(int start, int end) {
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (end > count)
            throw new StringIndexOutOfBoundsException(end);
        if (start > end)
            throw new StringIndexOutOfBoundsException(end - start);
        return Arrays.copyOfRange(bytes, start, end);
    }

    public int indexOf(byte b) {
        return indexOf(b, 0);
    }

    public int indexOf(byte b, int fromIndex) {
        if (fromIndex >= count) {
            return -1;
        }

        for (int index = fromIndex; index < count; index++) {
            if (bytes[index] == b) {
                return index;
            }
        }

        return -1;
    }

    public synchronized byte[] getBytes() {
        if (toBytesCache == null) {
            toBytesCache = Arrays.copyOfRange(bytes, 0, count);
        }

        return toBytesCache;
    }

}
