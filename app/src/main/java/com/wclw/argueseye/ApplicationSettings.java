package com.wclw.argueseye;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class ApplicationSettings {

    private static final String PREF_NAME = "app_settings";
    private static final String KEY_HTTP_PROTOCOL = "http_protocol";


    private static ApplicationSettings applicationSettings;
    private boolean isHttpProtocol; //selected spinner items status
    private final static String TAG ="ApplicationSettings";

    private ApplicationSettings(){}
    public static ApplicationSettings getInstance(){
        if(applicationSettings == null){
            applicationSettings = new ApplicationSettings();
        }
        return applicationSettings;
    }

    public void load(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isHttpProtocol = prefs.getBoolean(KEY_HTTP_PROTOCOL, true);
    }

    public void setHttpProtocol(Context context, boolean status) {
        isHttpProtocol = status;

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_HTTP_PROTOCOL, status).apply();
    }

    public boolean getIsHttpProtocol(){return isHttpProtocol;}
}
