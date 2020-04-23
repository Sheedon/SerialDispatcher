package org.sheedon.serial;

import androidx.annotation.Nullable;

/**
 * 内容转化
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/10 23:29
 */
public interface DataConverter<F, T> {
    T convert(F value);

    abstract class Factory {
        public @Nullable DataConverter<byte[], Long> callbackNameCodeConverter(byte[] topic) {
            return null;
        }


    }
}
