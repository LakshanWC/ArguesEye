package com.wclw.argueseye.helpers;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdFilterDownloadHelper {

    private static final String EASYLIST =
            "https://easylist.to/easylist/easylist.txt";
    private static final String EASYPRIVACY =
            "https://easylist.to/easylist/easyprivacy.txt";

    private static final OkHttpClient client = new OkHttpClient();

    public static String downloadFilters(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String downloadAll() throws IOException {
        return downloadFilters(EASYLIST) + "\n" + downloadFilters(EASYPRIVACY);
    }
}
