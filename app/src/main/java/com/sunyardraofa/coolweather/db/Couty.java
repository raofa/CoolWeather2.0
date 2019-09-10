package com.sunyardraofa.coolweather.db;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.LitePalSupport;

public class Couty extends LitePalSupport {
    @SerializedName("name")
    public String coutyName;
    @SerializedName("weather_id")
    public String weatherId;
    @SerializedName("id")
    public int coutyid;

}
