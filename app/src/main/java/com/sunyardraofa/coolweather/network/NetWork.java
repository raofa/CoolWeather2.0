package com.sunyardraofa.coolweather.network;



import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetWork {
    
    private static String BASE_URL = "http://guolin.tech/api/";
    private static BingApi bingApi;
    private static AreaApi areaApi;
    private static WeatherApi weatherApi;
    private static OkHttpClient okHttpClient = new OkHttpClient();
    private static Converter.Factory gsonConverterFactory = GsonConverterFactory.create();
    private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJava2CallAdapterFactory.create();
    
    public static AreaApi getAreaApi(){
        if(areaApi == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .build();
            areaApi = retrofit.create(AreaApi.class);
        }
        return areaApi;
    }
    
    public static WeatherApi getWeatherApi(){
        if(weatherApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .addConverterFactory(gsonConverterFactory)
                    .build();
            weatherApi = retrofit.create(WeatherApi.class);
            
        }
        return weatherApi;
    }
    
    public static BingApi getBingApi(){
        if(bingApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .build();
            bingApi = retrofit.create(BingApi.class);
        }
        return bingApi;
    }
}
