package com.wclw.argueseye;

public class AdRule {
        public final String pattern;
        public final boolean isRegex;

        public AdRule(String pattern, boolean isRegex) {
            this.pattern = pattern;
            this.isRegex = isRegex;
        }
}
