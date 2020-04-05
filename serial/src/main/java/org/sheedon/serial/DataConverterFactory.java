package org.sheedon.serial;

import androidx.annotation.Nullable;

/**
 * 数据解析类
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 21:54
 */
public abstract class DataConverterFactory extends DataConverter.Factory {
    public @Nullable
    DataConverter<StringBuffer, DataCheckBean> checkDataConverter() {
        return null;
    }
}
