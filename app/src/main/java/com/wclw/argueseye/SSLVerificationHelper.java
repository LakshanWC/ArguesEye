package com.wclw.argueseye;

import android.content.Context;
import android.widget.Toast;

import okhttp3.Connection;
import okhttp3.HttpUrl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;

public class SSLVerificationHelper {

    public static class CertificateParts{
        String subject;
        String issuer;
        String validFrom;
        String validUntil;

    }


    public CertificateParts checkSSLCertificate(String url,Context debugContext) {
        String cleanedUrl = cleanUrl(url);

        URL webUrl = null;
        try {
            if(isHttps(cleanedUrl)){
                webUrl = new URL(cleanedUrl);
                HttpsURLConnection httpsConn = (HttpsURLConnection) webUrl.openConnection();
                return getCertificationDetails(httpsConn);

            }
            else{
                return null;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private CertificateParts getCertificationDetails(HttpsURLConnection httpsConn){
        try{
            httpsConn.connect();
            CertificateParts certParts = new CertificateParts();
            Certificate[] serverCertificate = httpsConn.getServerCertificates();
            for(Certificate cert : serverCertificate){
                if(cert instanceof X509Certificate){
                    X509Certificate x509Cert = (X509Certificate) cert;
                    certParts.subject = x509Cert.getSubjectDN().toString();
                    certParts.issuer = x509Cert.getIssuerDN().toString();
                    certParts.validFrom = x509Cert.getNotBefore().toString();
                    certParts.validUntil = x509Cert.getNotAfter().toString();
                    httpsConn.disconnect();
                    break;
                }
            }

            return certParts;

        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private boolean isHttps(String url){
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) return false;
        return httpUrl.scheme().equalsIgnoreCase("https");
    }

    private String cleanUrl(String url){

        String tempUrlHolder = url;

        if (!url.contains("://")) {
            // default to HTTP if url = www.example.com
            url = "http://" + url;
        }

        HttpUrl httpUrl = HttpUrl.parse(url);

        tempUrlHolder = httpUrl.scheme()+"://"+httpUrl.host();

        return tempUrlHolder;
    }
}
