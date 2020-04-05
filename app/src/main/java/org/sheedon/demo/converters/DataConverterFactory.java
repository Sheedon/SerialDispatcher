package org.sheedon.demo.converters;


import androidx.annotation.Nullable;

import org.sheedon.serial.DataCheckBean;
import org.sheedon.serial.DataConverter;

/**
 * @Description: java类作用描述
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 0:40
 */
public class DataConverterFactory extends org.sheedon.serial.DataConverterFactory {

    private CallbackRuleConverter ruleConverter;
    private CheckDataConverter checkConverter;

    public static DataConverterFactory create() {
        return new DataConverterFactory();
    }

    private DataConverterFactory() {
        ruleConverter = new CallbackRuleConverter();
        checkConverter = new CheckDataConverter();
    }

    @Nullable
    @Override
    public DataConverter<String, String> callbackNameConverter(String data) {
        return ruleConverter == null ? ruleConverter = new CallbackRuleConverter() : ruleConverter;
    }

    @Nullable
    @Override
    public DataConverter<StringBuffer, DataCheckBean> checkDataConverter() {
        return checkConverter == null ? checkConverter = new CheckDataConverter() : checkConverter;
    }
}
