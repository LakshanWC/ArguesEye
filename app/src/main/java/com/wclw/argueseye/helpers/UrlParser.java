package com.wclw.argueseye.helpers;

import okhttp3.HttpUrl;

public class UrlParser {

    public static class Parts {
        public String scheme;
        public String subdomain;
        public String domain;
        public String tld;
        public String host;
        public String path;
        public String query;
    }

    public Parts parseUrl(String url) {
        try {

            String originalUrl = url;

            if (!url.contains("://")) {
                // default to HTTP if url = www.example.com
                url = "http://" + url;
            }

            HttpUrl httpUrl = HttpUrl.parse(url);
            Parts urlParts = new Parts();

            urlParts.scheme = httpUrl.scheme();
            urlParts.host = httpUrl.host();
            urlParts.path = httpUrl.encodedPath();
            urlParts.query = httpUrl.query();

            // Split hostname into components
            String[] hostParts = urlParts.host.split("\\.");

            if (hostParts.length >= 2) {
                urlParts.tld = hostParts[hostParts.length - 1];
                urlParts.domain = hostParts[hostParts.length - 2];
            }

            if (hostParts.length > 2) {
                StringBuilder subdomainBuilder = new StringBuilder();
                for (int i = 0; i < hostParts.length - 2; i++) {
                    if (i > 0) subdomainBuilder.append(".");
                    subdomainBuilder.append(hostParts[i]);
                }
                urlParts.subdomain = subdomainBuilder.toString();
            } else {
                urlParts.subdomain = ""; // no subdomain
            }

            return urlParts;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}
