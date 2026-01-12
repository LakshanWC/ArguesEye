package com.wclw.argueseye.services;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.wclw.argueseye.AdRule;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class BloomFilterManager {
    private static BloomFilter<CharSequence> bloomFilter;

    public static void init(List<AdRule> rules) {
        bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                rules.size(),
                0.01
        );

        for (AdRule r : rules) {
            if (!r.isRegex) {
                bloomFilter.put(r.pattern);
            }
        }
    }

    public static boolean mightContain(String domain) {
        return bloomFilter != null && bloomFilter.mightContain(domain);
    }
}
