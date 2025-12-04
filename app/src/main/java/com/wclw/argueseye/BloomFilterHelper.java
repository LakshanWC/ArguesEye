package com.wclw.argueseye;

import android.content.Context;
import android.widget.Toast;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BloomFilterHelper {

    private static final String TRUSTED_FILE = "tranco_trusted_initial.bloom";
    private static final String UNTRUSTED_FILE = "urlhash_scam_initial.bloom";

    private static BloomFilter<String> trustedFilter;
    private static BloomFilter<String> untrustedFilter;


//    LOAD FILTERS (only once, cached in memory)
    public static void initialize(Context context) {
        if (trustedFilter == null)
            trustedFilter = loadBloom(context, true);

        if (trustedFilter != null) {
            Toast.makeText(context, "Trusted filter loaded successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to load trusted filter", Toast.LENGTH_SHORT).show();
        }

        if (untrustedFilter == null)
            untrustedFilter = loadBloom(context, false);

        if (untrustedFilter != null) {
            Toast.makeText(context, "Untrusted filter loaded successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to load untrusted filter", Toast.LENGTH_SHORT).show();
        }
    }


    private static BloomFilter<String> loadBloom(Context context, boolean isTrusted) {
        try {
            File file = new File(context.getFilesDir(),
                    isTrusted ? TRUSTED_FILE : UNTRUSTED_FILE);

            if (!file.exists()) return null;

            FileInputStream fis = new FileInputStream(file);

            // Use UTF-8 funnel explicitly
            BloomFilter<String> filter =
                    BloomFilter.readFrom(fis, Funnels.stringFunnel(StandardCharsets.UTF_8));

            fis.close();
            return filter;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------------------------------------------------
    // BOOLEAN QUERY
    // ------------------------------------------------------------
    public static boolean mightContain(String value, boolean isTrusted) {
        BloomFilter<String> filter = isTrusted ? trustedFilter : untrustedFilter;
        if (filter == null) return false; // Not loaded yet
        return filter.mightContain(value);
    }


    public static FilterType checkFilter(String value) {
        if (trustedFilter != null && trustedFilter.mightContain(value)) {
            return FilterType.TRUSTED;
        }
        if (untrustedFilter != null && untrustedFilter.mightContain(value)) {
            return FilterType.UNTRUSTED;
        }
        return FilterType.NONE;
    }


    // Optional getters
    public static BloomFilter<String> getTrustedFilter() {
        return trustedFilter;
    }

    public static BloomFilter<String> getUntrustedFilter() {
        return untrustedFilter;
    }
}
