package com.wclw.argueseye.dto;

public class DomainTimeData {
    public String domainAge;
    public boolean isExpired;
    public String message;

    public DomainTimeData(String domainAge,boolean isExpired,String message){
        this.domainAge = domainAge;
        this.isExpired = isExpired;
        this.message = message;
    }
}
