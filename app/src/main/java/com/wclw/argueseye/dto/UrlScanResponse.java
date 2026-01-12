package com.wclw.argueseye.dto;

import com.google.gson.annotations.SerializedName;

public class UrlScanResponse {
    public Scan scan;
    @SerializedName("tokens-left")
    public int tokensLeft;
    public String status;

    public static class Scan {
        public String uuid;
        public String url;
        public String apiMessage;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiMessage() {
            return apiMessage;
        }

        public void setApiMessage(String apiMessage) {
            this.apiMessage = apiMessage;
        }
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public int getTokensLeft() {
        return tokensLeft;
    }

    public void setTokensLeft(int tokensLeft) {
        this.tokensLeft = tokensLeft;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
