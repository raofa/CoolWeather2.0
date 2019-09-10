package com.sunyardraofa.coolweather.gson;

import java.util.List;

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    public List<Forecast> daily_forecast;
    
}
