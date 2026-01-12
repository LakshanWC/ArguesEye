package com.wclw.argueseye.services;

import android.net.Uri;

import com.wclw.argueseye.AdRule;

import java.util.List;

public class AdBlockerService {

        private static final AdBlockerService adBlockerService = new AdBlockerService();
        private List<AdRule> rules;
        private boolean initialized = false;


        public synchronized void init(List<AdRule> rules){
            if(initialized) return;

            this.rules = rules;
            BloomFilterManager.init(rules);
            initialized = true;
        }

        public static AdBlockerService getInstance(){
            return adBlockerService;
        }

        public boolean isInitialized(){
            return initialized;
        }


        public boolean isAd(String url) {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host == null) return false;

            // Fast reject
            if (!BloomFilterManager.mightContain(host)) return false;

            // Exact match
            for (AdRule rule : rules) {
                if (!rule.isRegex && host.contains(rule.pattern)) {
                    return true;
                }
                if (rule.isRegex && url.matches(rule.pattern)) {
                    return true;
                }
            }
            return false;
        }

}
