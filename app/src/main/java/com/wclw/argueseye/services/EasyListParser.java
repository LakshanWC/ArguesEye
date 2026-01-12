package com.wclw.argueseye.services;
import com.wclw.argueseye.AdRule;
import java.util.ArrayList;
import java.util.List;

public class EasyListParser {
    public static List<AdRule> parse(String list) {
        List<AdRule> rules = new ArrayList<>();

        for (String line : list.split("\n")) {
            line = line.trim();

            // Ignore comments & empty lines
            if (line.isEmpty() || line.startsWith("!") || line.startsWith("["))
                continue;

            // Domain rule
            if (line.startsWith("||")) {
                String domain = line.substring(2).split("\\^")[0];
                rules.add(new AdRule(domain, false));
            }
            // Regex rule
            else if (line.startsWith("/") && line.endsWith("/")) {
                rules.add(new AdRule(
                        line.substring(1, line.length() - 1), true
                ));
            }
        }
        return rules;
    }
}
