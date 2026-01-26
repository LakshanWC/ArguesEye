package com.wclw.argueseye.helpers;

import com.wclw.argueseye.dto.UrlScanResponse;

public interface UrlScanCallBack {

    void onSuccess(UrlScanResponse urlScanResponse);
    void onFailure(Exception e);
}
