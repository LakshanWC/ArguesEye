package com.wclw.argueseye.helpers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.wclw.argueseye.dto.UrlScanResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArguesEyeAPIHelper {
    private static final String API_URL = "https://argueseyeapi.onrender.com/urlscan?url=";
    private final static String TAG = "ArguesEyeAPIHelper";
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(50, TimeUnit.SECONDS) // how long to wait to establish connection
            .readTimeout(60, TimeUnit.SECONDS)    // how long to wait for server response
            .writeTimeout(30, TimeUnit.SECONDS)   // how long to wait to send request
            .build();


    public void sendRequest(String webUrl,UrlScanCallBack callBack){
        try{


            String finalUrl = API_URL + webUrl;

            Request request = new Request.Builder()
                    .addHeader("deviceId","testDeviceMobile")
                    .url(finalUrl)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if(callBack != null){
                        Log.d(TAG,"error"+e.getMessage());
                        callBack.onFailure(e);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String apiResponse = response.body().string();
                    Gson gson = new Gson();
                    UrlScanResponse urlScanResponse = gson.fromJson(apiResponse, UrlScanResponse.class);

                    if(callBack != null){
                        callBack.onSuccess(urlScanResponse);
                    }

                }
            });

        }catch (Exception e){
            Log.d(TAG,"error"+e.getMessage());
            callBack.onFailure(e);
        }
    }
}
