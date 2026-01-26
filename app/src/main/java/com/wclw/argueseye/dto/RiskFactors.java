package com.wclw.argueseye.dto;

public class RiskFactors {
    boolean isHttps;
    boolean isIpAddress;
    int domainAgeDays;                  // -1 = unknown
    boolean isExpiredOrVeryOldSuspicious;
    boolean hasSuspiciousTld;
    int subdomainCount;
    boolean hasAtSymbolInUrl;
    int specialCharCountInDomain;
    boolean hasHomoglyphs;
    int redirectionCount;
    boolean hasKnownMaliciousRedirect;
    boolean blacklisted;
    boolean certificateProblem;
    boolean certificateNewOrShortLived;
    double entropyOfDomain;
    boolean suspiciousBrandInDomain;
    boolean hasManyExternalScriptsOrLinks;

    public boolean isHttps() {
        return isHttps;
    }

    public void setHttps(boolean https) {
        isHttps = https;
    }

    public boolean isIpAddress() {
        return isIpAddress;
    }

    public void setIpAddress(boolean ipAddress) {
        isIpAddress = ipAddress;
    }

    public int getDomainAgeDays() {
        return domainAgeDays;
    }

    public void setDomainAgeDays(int domainAgeDays) {
        this.domainAgeDays = domainAgeDays;
    }

    public boolean isExpiredOrVeryOldSuspicious() {
        return isExpiredOrVeryOldSuspicious;
    }

    public void setExpiredOrVeryOldSuspicious(boolean expiredOrVeryOldSuspicious) {
        isExpiredOrVeryOldSuspicious = expiredOrVeryOldSuspicious;
    }

    public boolean isHasSuspiciousTld() {
        return hasSuspiciousTld;
    }

    public void setHasSuspiciousTld(boolean hasSuspiciousTld) {
        this.hasSuspiciousTld = hasSuspiciousTld;
    }

    public int getSubdomainCount() {
        return subdomainCount;
    }

    public void setSubdomainCount(int subdomainCount) {
        this.subdomainCount = subdomainCount;
    }

    public boolean isHasAtSymbolInUrl() {
        return hasAtSymbolInUrl;
    }

    public void setHasAtSymbolInUrl(boolean hasAtSymbolInUrl) {
        this.hasAtSymbolInUrl = hasAtSymbolInUrl;
    }

    public int getSpecialCharCountInDomain() {
        return specialCharCountInDomain;
    }

    public void setSpecialCharCountInDomain(int specialCharCountInDomain) {
        this.specialCharCountInDomain = specialCharCountInDomain;
    }

    public boolean isHasHomoglyphs() {
        return hasHomoglyphs;
    }

    public void setHasHomoglyphs(boolean hasHomoglyphs) {
        this.hasHomoglyphs = hasHomoglyphs;
    }

    public int getRedirectionCount() {
        return redirectionCount;
    }

    public void setRedirectionCount(int redirectionCount) {
        this.redirectionCount = redirectionCount;
    }

    public boolean isHasKnownMaliciousRedirect() {
        return hasKnownMaliciousRedirect;
    }

    public void setHasKnownMaliciousRedirect(boolean hasKnownMaliciousRedirect) {
        this.hasKnownMaliciousRedirect = hasKnownMaliciousRedirect;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public boolean isCertificateProblem() {
        return certificateProblem;
    }

    public void setCertificateProblem(boolean certificateProblem) {
        this.certificateProblem = certificateProblem;
    }

    public boolean isCertificateNewOrShortLived() {
        return certificateNewOrShortLived;
    }

    public void setCertificateNewOrShortLived(boolean certificateNewOrShortLived) {
        this.certificateNewOrShortLived = certificateNewOrShortLived;
    }

    public double getEntropyOfDomain() {
        return entropyOfDomain;
    }

    public void setEntropyOfDomain(double entropyOfDomain) {
        this.entropyOfDomain = entropyOfDomain;
    }

    public boolean isSuspiciousBrandInDomain() {
        return suspiciousBrandInDomain;
    }

    public void setSuspiciousBrandInDomain(boolean suspiciousBrandInDomain) {
        this.suspiciousBrandInDomain = suspiciousBrandInDomain;
    }

    public boolean isHasManyExternalScriptsOrLinks() {
        return hasManyExternalScriptsOrLinks;
    }

    public void setHasManyExternalScriptsOrLinks(boolean hasManyExternalScriptsOrLinks) {
        this.hasManyExternalScriptsOrLinks = hasManyExternalScriptsOrLinks;
    }
}
