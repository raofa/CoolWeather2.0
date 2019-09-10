package com.sunyardraofa.coolweather.network;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

public interface BingApi {
    @GET("bing_pic")
    Observable<ResponseBody> getbingurl();
}
