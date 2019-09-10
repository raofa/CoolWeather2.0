package com.sunyardraofa.coolweather.db;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.LitePalSupport;

public class City extends LitePalSupport {
    @SerializedName("name")
    public String cityName;
    @SerializedName("id")
    public int cityId;

   
}
