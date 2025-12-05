package com.wclw.argueseye.helpers;

import com.wclw.argueseye.dto.RdapRespose;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WhoisApi {
    @GET("domain/{domain}")
    Call<RdapRespose> getWhois(@Path("domain") String domain);
}
