package com.wclw.argueseye;

import android.content.Context;

import androidx.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Connection;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CertificateChecker {

    public static class Results {
        public String subject;
        public String issuer;
        public String validFrom;
        public String validUntil;
        public String fingerprint;
        public List<String> sans = new ArrayList<>();
        public boolean expired;
        public boolean hostnameMismatch;
        public boolean selfSigned;
        public String error;
    }

    public interface Listener {
        void onComplete(Results results);
    }

    private final OkHttpClient client = new OkHttpClient.Builder().build();

    public void checkCertificate(String url, Context debugContext, Listener listener) {

        String host = cleanHost(url);
        if (host == null) {
            Results results = new Results();
            results.error = "Invalid URL";
            listener.onComplete(results);
            return;
        }

        Request request = new Request.Builder()
                .url("https://" + host + "/")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Results results = new Results();
                results.error = e.getMessage();
                listener.onComplete(results);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    Handshake handshake = response.handshake();
                    if (handshake == null) {
                        Results r = new Results();
                        r.error = "No TLS handshake available";
                        listener.onComplete(r);
                        return;
                    }
                    
                    X509Certificate cert = (X509Certificate) handshake.peerCertificates().get(0);

                    Results r = new Results();

                    r.subject = cert.getSubjectX500Principal().getName();
                    r.issuer = cert.getIssuerX500Principal().getName();
                    r.validFrom = cert.getNotBefore().toString();
                    r.validUntil = cert.getNotAfter().toString();
                    r.fingerprint = sha256Fingerprint(cert);

                    // Extract SANs
                    try {
                        Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
                        if (altNames != null) {
                            for (List<?> entry : altNames) {
                                if ((Integer) entry.get(0) == 2) { // DNS entry
                                    r.sans.add((String) entry.get(1));
                                }
                            }
                        }
                    } catch (Exception ignored) {}

                    // Basic checks
                    r.expired = cert.getNotAfter().before(new Date());
                    r.selfSigned = r.subject.equals(r.issuer);
                    r.hostnameMismatch = !r.sans.contains(host);

                    listener.onComplete(r);

                } catch (Exception e) {
                    Results r = new Results();
                    r.error = "Parsing error: " + e.getMessage();
                    listener.onComplete(r);
                } finally {
                    response.close();
                }
            }
        });
    }

    private String cleanHost(String input) {
        if (!input.contains("://")) input = "https://" + input;
        HttpUrl parsed = HttpUrl.parse(input);
        return parsed != null ? parsed.host() : null;
    }

    private static String sha256Fingerprint(X509Certificate cert) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(cert.getEncoded());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02X", b));
            return sb.toString();
        } catch (Exception e) {
            return "N/A";
        }
    }
}
