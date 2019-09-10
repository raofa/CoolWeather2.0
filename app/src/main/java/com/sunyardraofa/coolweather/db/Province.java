package com.sunyardraofa.coolweather.db;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.DataSupport;
import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {
//    public int id;
    
    @SerializedName("id")
    public int provinceid;
    
    @SerializedName("name")
    public String provincename;
}
