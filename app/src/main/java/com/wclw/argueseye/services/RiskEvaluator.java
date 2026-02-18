package com.wclw.argueseye.services;

import static java.lang.Math.log;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.wclw.argueseye.dto.RiskFactors;
import com.wclw.argueseye.dto.RiskResult;
import com.wclw.argueseye.security.SuspiciousTLDs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for evaluating risk factors of URLs/websites
 */
public class RiskEvaluator {

    private static RiskFactors riskFactors = new RiskFactors();
    private List<String> warnings = new ArrayList<>();
    private RiskResult riskResults;
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
     * Main risk calculation method - combines all security factors
     */
    public RiskResult calculateRiskFactor(String webUrl, Context context) {
        int riskScore = 0;
        warnings.clear();

        try {
            // Check 1: IP Address Detection
            boolean isIp = isIpAddress(webUrl);
            riskFactors.setIpAddress(isIp);
            if (isIp) {
                riskScore += 45;
                warnings.add("URL uses raw IP address instead of domain name");
            }

            // Check 2: @ Symbol Detection
            boolean hasSymbol = hasAtSymbolInUrl(webUrl);
            riskFactors.setHasAtSymbolInUrl(hasSymbol);
            if (hasSymbol) {
                riskScore += 40;
                warnings.add("URL contains '@' symbol - common phishing obfuscation technique");
            }

            // Check 3: Suspicious TLD
            boolean suspiciousTld = hasSuspiciousTld(webUrl);
            riskFactors.setHasSuspiciousTld(suspiciousTld);
            if (suspiciousTld) {
                riskScore += 30;
                warnings.add("Suspicious or newly popular phishing TLD detected");
            }

            // Check 4: Special Characters Count
            int specialCharCount = countSpecialCharactersInDomain(webUrl);
            riskFactors.setSpecialCharCountInDomain(specialCharCount);
            if (specialCharCount >= 4) {
                riskScore += 20;
                warnings.add("Excessive special characters in domain (" + specialCharCount + " found)");
            } else if (specialCharCount >= 2) {
                riskScore += 10;
                warnings.add("Multiple special characters in domain (" + specialCharCount + " found)");
            }

            // Check 5: Domain Entropy (Randomness)
            double entropy = calculateDomainEntropy(extractDomain(webUrl));
            riskFactors.setEntropyOfDomain(entropy);
            if (entropy > 4.1) {
                riskScore += 30;
                warnings.add("Very high domain entropy - appears randomly generated("+entropy+")");
            } else if (entropy > 3.7) {
                riskScore += 18;
                warnings.add("High domain entropy - unusual character distribution("+entropy+")");
            } else if (entropy < 2.9) {
                // Reward for very clean domains
                riskScore -= 5;
            }

            // Check 6: Homoglyph Detection
            boolean hasHomoglyphs = hasHomoglyphs(extractDomain(webUrl), context);
            riskFactors.setHasHomoglyphs(hasHomoglyphs);
            if (hasHomoglyphs) {
                riskScore += 45;
                warnings.add("Domain contains Unicode homoglyphs (look-alike characters)");
            }

            // Check 7: Subdomain Count
            int subdomainCount = countSubdomains(webUrl);
            if (subdomainCount > 3) {
                riskScore += 25;
                warnings.add("Excessive subdomains detected (" + subdomainCount + " levels)");
            } else if (subdomainCount > 2) {
                riskScore += 10;
                warnings.add("Multiple subdomains detected (" + subdomainCount + " levels)");
            }

            // Check 8: Non-Standard Ports
            boolean nonStandardPorts = hasNonStandardPort(webUrl);
            if (nonStandardPorts) {
                riskScore += 20;
                warnings.add("Non-standard port detected - potential security risk");
            }

            // Check 9: URL Shortener Detection
            boolean isShortedUrl = isShortenerUrl(extractDomain(webUrl));
            if (isShortedUrl) {
                riskScore += 30;
                warnings.add("URL shortener detected - actual destination unknown");
            }

            // Check 10: Suspicious Brand Names
            boolean hasBrand = containsSuspiciousBrand(extractDomain(webUrl), context);
            if (hasBrand) {
                riskScore += 25;
                warnings.add(" Domain contains commonly phished brand name");
            }

            // Check 11: HTTPS Check
            boolean lacksHttps = lacksHttps(webUrl);
            if (lacksHttps) {
                riskScore += 20;
                warnings.add("No HTTPS encryption - insecure connection");
            }

            // Check 12: URL Length Check
            boolean urlTooLong = isUrlTooLong(webUrl);
            if (urlTooLong) {
                riskScore += 15;
                warnings.add("Unusually long URL (" + webUrl.length() + " characters)");
            }

            // Check 13: Suspicious Keywords
            boolean hasSuspiciousKeywords = hasSuspiciousKeywords(webUrl);
            if (hasSuspiciousKeywords) {
                riskScore += 15;
                warnings.add("Contains suspicious keywords (verify/login/secure/account)");
            }

            // Check 14: TLD in Subdomain
            boolean tldInSubdomain = hasTldInSubdomain(webUrl);
            if (tldInSubdomain) {
                riskScore += 35;
                warnings.add("TLD appears in subdomain (e.g., paypal.com.fake-site.com)");
            }

            // Check 15: High Digit Ratio
            double digitRatio = getDigitRatio(extractDomain(webUrl));
            if (digitRatio > 0.3) {
                riskScore += 15;
                warnings.add("High number of digits in domain (" + String.format("%.0f%%", digitRatio * 100) + ")");
            }

            // Check 16: Typosquatting Detection
            boolean hasTyposquatting = hasTyposquatting(extractDomain(webUrl));
            if (hasTyposquatting) {
                riskScore += 40;
                warnings.add("Possible typosquatting - domain resembles known brand");
            }

            // Cap risk score at 100
            if (riskScore > 100) riskScore = 100;
            if (riskScore < 0) riskScore = 0;

            riskResults = new RiskResult(riskScore, warnings);
            return riskResults;

        } catch (Exception e) {
            Log.e(TAG, "Error calculating risk: " + e.getMessage());
            return new RiskResult(0, new ArrayList<>());
        }
    }

    /**
     * Checks if the given URL uses only an IP address instead of a domain name
     */
    private boolean isIpAddress(String webUrl) {
        if (webUrl == null) return false;

        try {
            URI uri = new URI(webUrl);
            String host = uri.getHost();

            if (host == null) {
                //  manually strip scheme and extract before first slash/colon
                String stripped = webUrl.replaceFirst("^[a-zA-Z][a-zA-Z0-9+\\-.]*://", "");
                if (stripped.contains("@")) {
                    stripped = stripped.substring(stripped.lastIndexOf('@') + 1);
                }

                stripped = stripped.split("[/?#]")[0];
                stripped = stripped.split(":")[0];
                host = stripped;
            }

            return host.matches(IP_REGEX);
        } catch (Exception e) {
            Log.d(TAG, "Error in isIpAddress: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if domain is expired or suspiciously old
     */
    private boolean isExpiredOrVeryOldSuspicious() {
        // TODO: Implement based on WHOIS registration/expiration dates
        return false;
    }

    /**
     * Checks if the TLD belongs to suspicious/new gTLDs commonly used in phishing
     */
    private boolean hasSuspiciousTld(String webUrl) {
        if (webUrl != null) {
            return SuspiciousTLDs.isSuspicious(extractTld(extractDomain(webUrl)));
        }
        return false;
    }

    /**
     * Counts number of subdomains in the domain part of the URL
     */
    private int countSubdomains(String webUrl) {
        if (webUrl != null) {
            String domain = extractDomain(webUrl);
            int count = 0;
            for (char c : domain.toCharArray()) {
                if (c == '.') {
                    count++;
                }
            }
            return count;
        }
        return 0;
    }

    /**
     * Checks if URL contains @ symbol
     */
    private boolean hasAtSymbolInUrl(String webUrl) {
        return webUrl != null && webUrl.contains("@");
    }

    /**
     * Counts number of special characters in the domain name part
     */
    private int countSpecialCharactersInDomain(String webUrl) {
        if (webUrl == null || webUrl.trim().isEmpty()) {
            return 0;
        }

        String domain = extractDomain(webUrl);
        if (domain == null || domain.isEmpty()) {
            return 0;
        }

        domain = domain.toLowerCase();
        int count = 0;

        for (char c : domain.toCharArray()) {
            if (SUSPICIOUS_SPECIAL_CHARS.indexOf(c) >= 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Attempts to detect homoglyph / look-alike characters in domain
     */
    private boolean hasHomoglyphs(String domain, Context context) {
        try {
            List<String> domainCodePoints = toUnicodeList(domain);

            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("confusables.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("#.*", "").trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 2) continue;

                String confusable = parts[0].trim().toUpperCase();

                if (domainCodePoints.contains(confusable)) {
                    reader.close();
                    return true;
                }
            }

            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Error in hasHomoglyphs: " + e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Calculates approximate entropy (randomness) of domain name
     */
    private double calculateDomainEntropy(String domain) {
        if (domain == null || domain.isEmpty()) {
            return 0.0;
        }

        Map<Character, Integer> freq = new HashMap<>();

        for (char c : domain.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        int length = domain.length();

        for (Map.Entry<Character, Integer> entry : freq.entrySet()) {
            int count = entry.getValue();
            double p = (double) count / length;
            entropy -= p * (log(p) / log(2));
        }

        return entropy;
    }

    /**
     * Checks if domain contains known brand names
     */
    private boolean containsSuspiciousBrand(String domain, Context context) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("suspicious_brands.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String lowerDomain = domain.toLowerCase();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (lowerDomain.contains(line)) {
                    reader.close();
                    return true;
                }
            }

            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading suspicious brands file: " + e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * Checks if URL shortener is used
     */
    private boolean isShortenerUrl(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        String[] shorteners = {
                "bit.ly", "tinyurl.com", "goo.gl", "ow.ly", "t.co",
                "buff.ly", "is.gd", "cutt.ly", "short.io", "rebrand.ly"
        };

        for (String shortener : shorteners) {
            if (domain.contains(shortener)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if URL uses non-standard port
     */
    private boolean hasNonStandardPort(String webUrl) {
        try {
            URI uri = new URI(webUrl);
            int port = uri.getPort();
            return port != -1 && port != 80 && port != 443;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if URL lacks HTTPS
     */
    private boolean lacksHttps(String webUrl) {
        return webUrl != null && !webUrl.toLowerCase().startsWith("https://");
    }

    /**
     * Checks if URL is too long
     */
    private boolean isUrlTooLong(String webUrl) {
        return webUrl != null && webUrl.length() > 75;
    }

    /**
     * Checks for suspicious keywords in URL path
     */
    private boolean hasSuspiciousKeywords(String webUrl) {
        if (webUrl == null) return false;

        String[] keywords = {
                "verify", "account", "secure", "update", "suspend",
                "confirm", "login", "signin", "banking", "password",
                "credential", "validate", "authenticate"
        };

        String lowerUrl = webUrl.toLowerCase();
        for (String keyword : keywords) {
            if (lowerUrl.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if TLD appears in subdomain (e.g., paypal.com.fake.site)
     */
    private boolean hasTldInSubdomain(String webUrl) {
        String domain = extractDomain(webUrl);
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        String[] commonTlds = {".com", ".net", ".org", ".co", ".io", ".gov", ".edu"};

        // Remove the actual TLD first
        int lastDot = domain.lastIndexOf('.');
        if (lastDot > 0) {
            String withoutTld = domain.substring(0, lastDot);
            for (String tld : commonTlds) {
                if (withoutTld.contains(tld)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calculates digit-to-character ratio in domain
     */
    private double getDigitRatio(String domain) {
        if (domain == null || domain.isEmpty()) {
            return 0.0;
        }

        int digits = 0;
        for (char c : domain.toCharArray()) {
            if (Character.isDigit(c)) {
                digits++;
            }
        }
        return (double) digits / domain.length();
    }

    /**
     * Detects typosquatting attempts using Levenshtein distance
     */
    private boolean hasTyposquatting(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        String[] trustedBrands = {
                "paypal", "google", "amazon", "facebook", "microsoft",
                "apple", "netflix", "instagram", "twitter", "linkedin",
                "ebay", "walmart", "chase", "wellsfargo", "bankofamerica",
                "spotify", "adobe", "dropbox", "yahoo", "outlook",
                "gmail", "icloud", "whatsapp", "youtube", "reddit",
                "coinbase", "binance", "blockchain", "metamask"
        };

        // Remove TLD for comparison
        String domainName = removeTld(domain);

        for (String brand : trustedBrands) {
            int distance = levenshteinDistance(domainName, brand);

            // Distance of 1-2 indicates likely typosquatting
            if (distance >= 1 && distance <= 2) {
                return true;
            }

            // Check for common character substitutions
            if (hasCommonTypoPattern(domainName, brand)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[len1][len2];
    }

    /**
     * Checks for common typosquatting patterns (character substitutions)
     */
    private boolean hasCommonTypoPattern(String domain, String brand) {
        domain = domain.toLowerCase();
        brand = brand.toLowerCase();

        // Common substitutions used in typosquatting
        String[][] substitutions = {
                {"0", "o"}, {"1", "l"}, {"1", "i"}, {"5", "s"},
                {"8", "b"}, {"vv", "w"}, {"rn", "m"}, {"cl", "d"}
        };

        for (String[] sub : substitutions) {
            String modified = brand.replace(sub[1], sub[0]);
            if (domain.equals(modified)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes TLD from domain for comparison purposes
     */
    private String removeTld(String domain) {
        if (domain == null || domain.isEmpty()) {
            return "";
        }

        int lastDot = domain.lastIndexOf('.');
        if (lastDot == -1) {
            return domain;
        }

        return domain.substring(0, lastDot);
    }

    /**
     * Extracts domain from full URL
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
     * Extracts TLD from domain
     */
    private String extractTld(String domain) {
        if (domain == null || domain.isEmpty()) return "";
        int lastDot = domain.lastIndexOf('.');
        if (lastDot == -1) return "";
        return domain.substring(lastDot + 1).toLowerCase();
    }

    /**
     * Converts string to list of Unicode code points
     */
    private List<String> toUnicodeList(String input) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < input.length(); ) {
            int cp = input.codePointAt(i);
            list.add(String.format("%04X", cp));
            i += Character.charCount(cp);
        }

        return list;
    }
}