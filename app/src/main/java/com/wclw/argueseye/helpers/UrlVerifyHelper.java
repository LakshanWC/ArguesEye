package com.wclw.argueseye.helpers;


import android.util.Log;

import java.net.IDN;
import java.net.URL;

public class UrlVerifyHelper {

    private final static String TAG ="UrlVerifyHelper";

    public static String getDomainFromUrl(String url){
        if (url == null || url.isEmpty()) return null;
        try{
            if (!url.matches("^\\w+://.*")) {
                url = "http://" + url;
            }

            URL webUrl = new URL(url);
            String host = webUrl.getHost();
            if(host == null){return null;}
            return host.toLowerCase();
        }
        catch (Exception e){
            Log.d(TAG,"Error " +e.getMessage());
            return null;
        }
    }

    public static boolean isSuspiciousIDN(String domain){
        String[] labels = domain.split("\\.");
        for (String label: labels) {
            if(label.startsWith("xn--")){
                return true;
            }
        }
        return false;
    }



}
