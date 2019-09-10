package com.sunyardraofa.coolweather.gson;

public class Forecast {
    public class cond{
        public String txt_d;
    }
    public class tmp{
        public String max;
        public String min;
    }
    
    public String date;
    
    public cond cond;
    
    public tmp tmp;
}
