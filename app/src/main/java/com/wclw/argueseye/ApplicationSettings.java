package com.wclw.argueseye;

public class ApplicationSettings {

    private static ApplicationSettings applicationSettings;

    private ApplicationSettings(){}
    public static ApplicationSettings getInstance(){
        if(applicationSettings == null){
            applicationSettings = new ApplicationSettings();
        }
        return applicationSettings;
    }
}
