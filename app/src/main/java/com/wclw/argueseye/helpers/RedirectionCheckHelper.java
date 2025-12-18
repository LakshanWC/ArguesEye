package com.wclw.argueseye.helpers;

import android.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RedirectionCheckHelper {

    private static final String TAG = "RedirectionCheckHelper";

    public static List<String> getRedirectionChain(String webUrl){
        List<String> redirectChain = new ArrayList<>();
        redirectChain.add(webUrl);

        String currentUrl = webUrl;
        int maxRedirects = 20;
        boolean redirect;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        try {
            for (int i = 0; i <= maxRedirects; i++) {

                Request request = new Request.Builder()
                        .url(currentUrl)
                        .head()
                        .build();

                try (Response response = okHttpClient.newCall(request).execute()) {
                    int code = response.code();

                    if (code >= 300 && code < 400) {
                        String location = response.header("Location");
                        if (location == null || location.isEmpty()) {
                            break;
                        }

                        URL resolvedUrl = new URL(new URL(currentUrl), location);
                        String nextUrl = resolvedUrl.toString();

                        // Avoid loops
                        if (redirectChain.contains(nextUrl)) {
                            break;
                        }

                        redirectChain.add(nextUrl);
                        currentUrl = nextUrl;
                    } else {
                        String finalUrl = response.request().url().toString();
                        if (!finalUrl.equals(redirectChain.get(redirectChain.size() - 1))) {
                            redirectChain.add(finalUrl);
                        }
                        break;
                    }
                }
            }
            return redirectChain;
        }
        catch (Exception e){
            Log.d(TAG,"Error Following Redirection"+ e.getMessage());
            return redirectChain;
        }
    }
}
