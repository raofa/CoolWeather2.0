package com.sunyardraofa.coolweather.network;

import com.sunyardraofa.coolweather.db.City;
import com.sunyardraofa.coolweather.db.Couty;
import com.sunyardraofa.coolweather.db.Province;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AreaApi {
    @GET("china")
    Observable<List<Province>> getprovince();
    
    @GET("china/{provinceid}")
    Observable<List<City>> getcity(@Path("provinceid") String provinceid);
    
    @GET("china/{provinceid}/{cityid}")
    Observable<List<Couty>> getcouty(@Path("provinceid") String provinceid,@Path("cityid") String cityid);
}
