# SerialDispatcher

### Gradle

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```
	dependencies {
	        implementation 'com.github.Sheedon:SerialDispatcher:1.1.0'
	}
```



### Maven

**Step 1.** Add the JitPack repository to your build file

```
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

**Step 2.** Add the dependency

```
	<dependency>
	    <groupId>com.github.Sheedon</groupId>
	    <artifactId>SerialDispatcher</artifactId>
	    <version>v1.0.0</version>
	</dependency>
```



### Create

```
SerialClient client = new SerialClient.Builder()
	.path("/tty/s4")
	.name("qrcode")
	.addConverterFactory(DataConverterFactory.create())
	.callback(this)
	.build();

Request request = new RequestBuilder()
	.backName("0101")
	.data("7A123132137456")
	.build();

Call call = client.newCall(request);
	call.enqueue(new Callback() {
		@Override
		public void onFailure(Throwable e) {
			System.out.println(e);
		}

		@Override
         public void onResponse(Response response) {
			ResponseBody body = response.body();
			System.out.println(body == null ? "" : body.getBody());
         }
});
```

```
/**
 * 反馈内容解析器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2020/3/11 0:45
 */
public class CheckDataConverter implements DataConverter<StringBuffer, DataCheckBean> {

    private static final String STARTBIT = "7A";
    private static final String ENDBIT = "";

    CheckDataConverter() {

    }

    // 数据格式
    // 协议头  数据长度位  子控设备地址  命令类型    消息体    CRC16校验
    // 7A      0800         01              03         01       B07A
    @Override
    public DataCheckBean convert(StringBuffer value) {
        if (value == null || value.length() == 0) {
            return DataCheckBean.build(null, 0);
        }

        int index = value.indexOf(STARTBIT);
        if (index == -1) {
            return DataCheckBean.build(null, 0);
        }

        if (index + 6 >= value.length()) {
            return DataCheckBean.build(null, index);
        }

        // 一个内容到总长度
        String lengthStr = value.substring(index + 2, index + 6);
        int length = calcLength(lengthStr) * 2;
        if (length < 0 || index + length > value.length()) {
            return DataCheckBean.build(null, index);
        }

        String content = value.substring(index, index + length);
        boolean check = checkContent(content);
        if (check) {
            ResponseBody body = ResponseBody.build(STARTBIT,
                    content.substring(STARTBIT.length(), content.length() - 4),
                    content.substring(content.length() - 4),
                    ENDBIT, content);

            return DataCheckBean.build(body, index + length);
        } else {
            return DataCheckBean.build(null, index + length);
        }
    }

    /**
     * 获取总长度
     *
     * @param str 字符
     */
    private int calcLength(String str) {
        String lowPosition = str.substring(0, 2);
        String highPosition = str.substring(2, 4);

        int low = Integer.parseInt(lowPosition, 16);
        int high = Integer.parseInt(highPosition, 16);

        return high * 16 * 16 + low;

    }


    /**
     * 核实内容校验码
     * 拿到校验码 后四位
     * 拿到内容 除了后四位外的数据
     *
     * @param content 内容
     * @return 校验是否一致
     */
    private boolean checkContent(String content) {
        if (content.length() <= 4)
            return false;

        String checkStr = content.substring(content.length() - 4);
        String contentStr = content.substring(0, content.length() - 4);
        StringBuilder checkResult = new StringBuilder(CRC16M.getBufHexStr(CRC_16.getSendBuf(contentStr)));
        checkResult = checkResult.delete(0, content.length() - 4);
        for (int index = checkResult.length(); index < 4; index++) {
            checkResult.insert(0, "0");
        }

        return checkResult.toString().equalsIgnoreCase(checkStr);
    }
}
```

