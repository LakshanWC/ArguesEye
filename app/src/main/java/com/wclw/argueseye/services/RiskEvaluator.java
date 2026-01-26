package com.wclw.argueseye.services;

import static java.lang.Math.log;

import android.util.Log;

import com.wclw.argueseye.dto.RiskFactors;
import com.wclw.argueseye.security.SuspiciousTLDs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for evaluating risk factors of URLs/websites
 */
public class RiskEvaluator {

    private static RiskFactors riskFactors = new RiskFactors();
    private static final String TAG = "RiskEvaluator";
    private static final String SUSPICIOUS_SPECIAL_CHARS = "0@1!|lI-_~";
    private static final String IP_REGEX =
            "(?i)^(?:https?://)?(" +
                    // IPv4
                    "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.|$)){4}" +
                    "|" +
                    // IPv6
                    "\\[[0-9a-f:]+\\]" +
                    "|" +
                    // Hex
                    "0x[0-9a-f]+" +
                    "|" +
                    // Decimal
                    "\\d{8,10}" +
                    ")";


    /**
     * Main risk calculation method - to be filled with logic that combines all factors
     */
    public int calculateRiskFactor(String webUrl) {

        // You can create a RiskResult object, calculate score, collect warnings, etc.
        int riskScore = 0;
        List<String> warnings = new ArrayList<>();

        try{
            boolean isIp = isIpAddress(webUrl);
            riskFactors.setIpAddress(isIp);
            if(isIp){
                riskScore += 45;
                warnings.add("URL uses raw IP address instead of domain name");
            }

            boolean hasSymbol = hasAtSymbolInUrl(webUrl);
            riskFactors.setHasAtSymbolInUrl(hasSymbol);
            if(hasSymbol){
                riskScore +=40;
                warnings.add("URL contains '@' symbol - classic phishing obfuscation");
            }

            boolean suspiciousTld = hasSuspiciousTld(webUrl);
            riskFactors.setHasSuspiciousTld(suspiciousTld);
            if(suspiciousTld){
                riskScore += 30;
                warnings.add("Suspicious / newly popular phishing TLD detected");
            }

            int specialCharCount = countSpecialCharactersInDomain(webUrl);
            riskFactors.setSpecialCharCountInDomain(specialCharCount);
            if (specialCharCount >= 4) {
                riskScore += 20;
                warnings.add("Many special characters in domain (" + specialCharCount + ")");
            } else if (specialCharCount >= 2) {
                riskScore += 10;
                warnings.add("Some special characters in domain (" + specialCharCount + ")");
            }

            double entropy = calculateDomainEntropy(extractDomain(webUrl));
            riskFactors.setEntropyOfDomain(entropy);

            if (entropy > 4.1) {
                riskScore += 30;
                warnings.add("Very high domain entropy (random generated-looking domain)");
            } else if (entropy > 3.7) {
                riskScore += 18;
                warnings.add("High domain entropy");
            } else if (entropy < 2.9) {
//                for very clean domains
                 riskScore -= 5;
            }

            if (riskScore > 100) riskScore = 100;

            return riskScore;


        }catch (Exception e){
            Log.d(TAG,"Error "+e.getMessage());
            return 0;
        }
    }

    /**
     * Checks if the given URL uses only an IP address instead of a domain name
     */
    private boolean isIpAddress(String webUrl) {
        // TODO: Implement IP address detection

        if(webUrl != null){
            String domain = extractDomain(webUrl);
            try{

                return domain.matches(IP_REGEX);

            }catch (Exception e){
                Log.d(TAG,"Error "+e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Checks if domain is expired or suspiciously old (very old + recently changed can be suspicious)
     * Usually needs WHOIS data - this method would use the already fetched data
     */
    private boolean isExpiredOrVeryOldSuspicious() {
        // TODO: Implement based on registration/expiration dates
        // This method might need to receive dates as parameters
        return false;
    }

    /**
     * Checks if the TLD (top-level domain) belongs to suspicious/new gTLDs
     * commonly used in phishing/malware campaigns
     */
    private boolean hasSuspiciousTld(String webUrl) {
        if(webUrl != null){
            return SuspiciousTLDs.isSuspicious(extractTld(webUrl));
        }
        return false;
    }

    /**
     * Counts number of subdomains in the domain part of the URL
     * (excluding www as first level in most cases)
     */

    private int countSubdomains(String webUrl) {
        if(webUrl != null) {
            String domain = extractDomain(webUrl);
            int count = 1;
            for (char c : domain.toCharArray()) {
                if (c == '.') {
                    count++;
                }
            }
            return count;
        }return 0;
    }

    /**
     * Checks if URL contains '@' symbol (classic phishing trick)
     */
    private boolean hasAtSymbolInUrl(String webUrl) {
        return webUrl.contains("@");
    }

    /**
     * Counts number of special characters in the domain name part
     * (dashes, underscores, multiple dots etc)
     */
    private int countSpecialCharactersInDomain(String webUrl) {
        if(webUrl == null || webUrl.trim().isEmpty()) { return 0; }

        String domain = extractDomain(webUrl);
        if(domain == null || domain.isEmpty()) { return 0; }

        domain = domain.toLowerCase();
        int count = 0;

        for (char c: domain.toCharArray()) {
            if(SUSPICIOUS_SPECIAL_CHARS.indexOf(c)>=0){
                count ++;
            }
        }
        return count;
    }

    /**
     * Attempts to detect homoglyph / look-alike characters in domain
     * (very important nowadays - cyrillic, greek letters etc)
     */
    private boolean hasHomoglyphs(String domain) {
        // TODO: Implement basic or advanced homoglyph detection
        return false;
    }


    /**
     * Calculates approximate entropy (randomness) of domain name
     * Higher entropy → more likely to be DGA (Domain Generation Algorithm)
     */


    private double calculateDomainEntropy(String domain) {
        double entropy = 0;

        if(domain != null){
            Map<Character,Integer> freq = new HashMap<>();

            for (char c: domain.toCharArray()) {
                if(freq.containsKey(c)){
                    freq.replace(c,freq.getOrDefault(c,0)+1);
                }
                freq.put(c,1);
            }

            int length = domain.length();
            for (Map.Entry<Character,Integer> entry: freq.entrySet()) {
                int count = entry.getValue();
                double p = (double) count/length;
                entropy -= p * (log(p) / log(2));
            }

            return entropy;
        }
        return 0.0;
    }

    /**
     * Checks if domain contains known brand names (paypal, amazon, google, etc)
     * which is common in phishing attacks
     */
    private boolean containsSuspiciousBrand(String domain) {
        // TODO: Implement brand name list check (case insensitive)
        return false;
    }

    /**
     * Simple helper - extracts domain part from full URL
     * (without protocol, path, query, etc)
     */
    private String extractDomain(String webUrl) {
        try {
            URI uri = new URI(webUrl);
            String domain = uri.getHost();
            if (domain == null) return "";
            // Remove www. prefix if present
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
            return domain.toLowerCase();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    /**
     * Helper method - extracts TLD from domain
     */
    private String extractTld(String domain) {
        if (domain == null || domain.isEmpty()) return "";
        int lastDot = domain.lastIndexOf('.');
        if (lastDot == -1) return "";
        return domain.substring(lastDot + 1).toLowerCase();
    }
}