package com.wclw.argueseye.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SuspiciousTLDs {
    private static final Set<String> SUSPICIOUS_TLDS = new HashSet<>(Arrays.asList(
            "zip","mov","xyz","top","cyou","icu","quest","skin","tk","cc","pw","buzz","rest","cfd",
            "gq","ml","cf","ga","ws","shop","click","link","work","support","live","fit","cam","bar",
            "loan","men","bid","date","faith","trade","accountant","stream","download","review",
            "country","kim","mom","surf","racing","win","vip","party","science","help","info","today",
            "online","site","website","press","news","cloud","services","digital","company","solutions",
            "network","center","technology","systems","email","security","verify","login","secure","update"
    ));
    private SuspiciousTLDs(){}

    public static boolean isSuspicious(String tld) {
        if (tld == null) return false;
        return SUSPICIOUS_TLDS.contains(tld.toLowerCase());
    }

    public static Set<String> getAllSuspiciousTLDS(){
        return SUSPICIOUS_TLDS;
    }

}
