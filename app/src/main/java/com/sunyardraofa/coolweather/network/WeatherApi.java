package com.sunyardraofa.coolweather.network;

import com.sunyardraofa.coolweather.gson.OldWeather;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Observable<OldWeather> getweatherinfo(@Query("cityid") String cityid, @Query("key") String key);
}
