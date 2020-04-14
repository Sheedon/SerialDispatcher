package org.sheedon.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.sheedon.demo.converters.DataConverterFactory;
import org.sheedon.serial.Call;
import org.sheedon.serial.Callback;
import org.sheedon.serial.Observable;
import org.sheedon.serial.Request;
import org.sheedon.serial.RequestBuilder;
import org.sheedon.serial.Response;
import org.sheedon.serial.ResponseBody;
import org.sheedon.serial.SerialClient;
import org.sheedon.serial.serialport.SerialRealCallback;

public class MainActivity extends AppCompatActivity implements SerialRealCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Observable observable = client.newObservable(request);
        observable.subscribe(new Callback<Response>() {
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
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Throwable e) {
//                System.out.println(e);
//            }
//
//            @Override
//            public void onResponse(Response response) {
//                ResponseBody body = response.body();
//                System.out.println(body == null ? "" : body.getBody());
//            }
//
//        });

        call.publishNotCallback();
        observable.cancel();
        Call call2 = client.newCall(request);
        call2.publishNotCallback();
    }

    @Override
    public void onCallback(ResponseBody data) {

    }
}
