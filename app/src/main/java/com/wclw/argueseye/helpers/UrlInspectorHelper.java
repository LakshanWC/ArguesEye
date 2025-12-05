package com.wclw.argueseye.helpers;

import android.util.Log;
import android.widget.Switch;

import com.wclw.argueseye.dto.DomainTimeData;
import com.wclw.argueseye.dto.RdapEvent;
import com.wclw.argueseye.dto.RdapRespose;

import okhttp3.OkHttpClient;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UrlInspectorHelper {
    private static final String RDAP_BASE_URL = "https://rdap.org/";
    private static final String TAG = "UrlInspectorHelper";
    private static UrlInspectorHelper instance;
    private Retrofit rdapRetrofit;

    private UrlInspectorHelper() {
        rdapRetrofit = new Retrofit.Builder()
                .baseUrl(RDAP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Log.d(TAG, "Retrofit instance created with base URL: " + RDAP_BASE_URL);
    }

    public static UrlInspectorHelper getInstance() {
        if (instance == null) {
            instance = new UrlInspectorHelper();
            Log.d(TAG, "UrlInspectorHelper instance created");
        }
        return instance;
    }

    public String findEventDate(List<RdapEvent> events, String action) {
        Log.d(TAG, "Searching for event: " + action);
        if (events == null) {
            Log.d(TAG, "Events list is null");
            return "Unknown";
        }
        for (RdapEvent rdapEvent : events) {
            Log.d(TAG, "Checking event: " + rdapEvent.eventAction + " -> " + rdapEvent.eventDate);
            if (action.equalsIgnoreCase(rdapEvent.eventAction)) {
                Log.d(TAG, "Found event date: " + rdapEvent.eventDate);
                return rdapEvent.eventDate;
            }
        }
        Log.d(TAG, "Event not found for action: " + action);
        return "Unknown";
    }

    public String cleanTimeStamp(String timeStamp){
        try {

            String cleanTimeStamp = timeStamp.substring(0, timeStamp.lastIndexOf('T') + 1);

            cleanTimeStamp = cleanTimeStamp + "|"
                    + timeStamp.substring(timeStamp.lastIndexOf('T') + 1
                    , timeStamp.lastIndexOf('Z') + 1);

            Log.d(TAG, "TimeStamp " + cleanTimeStamp);

            return cleanTimeStamp;
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
            return "~N/A~";
        }
    }

    public DomainTimeData domainAgeCheck(String registration, String expiration){
        try {
            
            // Convert "1997-09-15T04:00:00Z" → "1997-09-15 04:00:00"
            String cleanReg = registration.replace("|", "").replace('T', ' ').replace("Z", "");
            String cleanExp = expiration.replace("|", "").replace('T', ' ').replace("Z", "");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setLenient(false);

            Date regDate = sdf.parse(cleanReg);
            Date expDate = sdf.parse(cleanExp);
            Date now = new Date();

            //age calculation
            long ageInMillis = now.getTime() - regDate.getTime();
            long days = TimeUnit.DAYS.convert(ageInMillis, TimeUnit.MILLISECONDS);

            long years = days / 365;
            long remainingDays = days % 365;
            long months = remainingDays / 30;
            long extraDays = remainingDays % 30;

            //expiry calculation
            boolean isExpired = now.after(expDate);
            long daysToExpiry = TimeUnit.DAYS.convert(expDate.getTime() - now.getTime(), TimeUnit.MILLISECONDS);

            String domainAge = years + " years, " + months + " months, " + extraDays + " days";
            String message;

            if (days <= 7) {
                message = "This domain is very new! Be cautious as newly registered domains can be suspicious.";
            } else if (days <= 30) {
                message = "This domain is recently registered. Exercise caution before trusting it.";
            } else if (years < 2) {
                message = "This domain is relatively young. Consider checking its reputation before use.";
            } else {
                message = "This domain has been around for a while.";
            }

            return new DomainTimeData(domainAge, isExpired, message);
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
            return new DomainTimeData("N/A",false,"N/A");
        }
    }

    public void fetchWhois(String domain, retrofit2.Callback<RdapRespose> callback) {
        Log.d(TAG, "Fetching RDAP data for domain: " + domain);

        String tld = domain.substring(domain.lastIndexOf('.') + 1).toLowerCase();
        Log.d(TAG, "Extracted TLD: " + tld);

        String baseUrl;
        switch (tld) {
            case "com":
            case "net":
                baseUrl = "https://rdap.verisign.com/com/v1/";
                break;
            default:
                callback.onFailure(null, new Throwable("Unsupported TLD: " + tld));
                Log.e(TAG, "Unsupported TLD: " + tld);
                return;
        }
        Log.d(TAG, "Using authoritative RDAP server: " + baseUrl);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Log.d(TAG, "Retrofit instance created with base URL: " + baseUrl);

        WhoisApi whoisApi = retrofit.create(WhoisApi.class);

        long startTime = System.currentTimeMillis();
        Log.d(TAG, "Enqueuing RDAP request for domain: " + domain);

        whoisApi.getWhois(domain).enqueue(new Callback<RdapRespose>() {
            @Override
            public void onResponse(Call<RdapRespose> call, Response<RdapRespose> response) {
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "RDAP response received in " + (endTime - startTime) + " ms");

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "RDAP response successful for domain: " + domain);
                    callback.onResponse(call, response);
                } else {
                    Log.e(TAG, "RDAP response failed or empty, code: " + response.code());
                    callback.onFailure(call, new Throwable("Empty or failed response, code: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<RdapRespose> call, Throwable t) {
                long endTime = System.currentTimeMillis();
                Log.e(TAG, "RDAP request failed for domain: " + domain + " after " + (endTime - startTime) + " ms", t);
                callback.onFailure(call, t);
            }
        });
    }
}
