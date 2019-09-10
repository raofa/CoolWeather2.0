package com.sunyardraofa.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sunyardraofa.coolweather.gson.Forecast;
import com.sunyardraofa.coolweather.gson.OldWeather;
import com.sunyardraofa.coolweather.gson.Weather;
import com.sunyardraofa.coolweather.network.NetWork;
import com.sunyardraofa.coolweather.service.AutoUpdateService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.observable.ObservableError;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class WeatherActivity extends AppCompatActivity {
    
    private static final String TAG = "WeatherActivity";
    
    public DrawerLayout drawerLayout;
    
    private Button home;
    
    private ImageView backpic;

    private TextView title_city, title_updatetime;

    private TextView now_temp, now_cond;

    private LinearLayout forecast_layout;
    
    private TextView aqi_aqi,aqi_pm25;
    
    private TextView suggestion_comf,suggestion_cw,suggestion_sport;

    private TextView forecast_item_date, forecast_item_info, forecast_item_max, forecast_item_min;

    public SwipeRefreshLayout refreshLayout;

    private String cacheweatherId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorview = getWindow().getDecorView();
            decorview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        backpic = findViewById(R.id.weather_backpic);
        drawerLayout = findViewById(R.id.drawerlayout);
        home = findViewById(R.id.homes);
        title_city = findViewById(R.id.title_city);
        title_updatetime = findViewById(R.id.title_update_time);
        now_temp = findViewById(R.id.now_temp);
        now_cond = findViewById(R.id.now_cond);
        aqi_aqi = findViewById(R.id.aqi_aqi);
        aqi_pm25 = findViewById(R.id.aqi_pm25);
        suggestion_comf = findViewById(R.id.suggestion_comf);
        suggestion_cw = findViewById(R.id.suggestion_cw);
        suggestion_sport = findViewById(R.id.suggestion_sport);
        forecast_layout = findViewById(R.id.forecast_layout);
        refreshLayout = findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pic = preferences.getString("backpic",null);
        if(pic != null){
            Glide.with(this)
                    .load(pic)
                    .into(backpic);
        }else {
            loadbackpic();
        }
        cacheweatherId = getIntent().getStringExtra("weather_id");
        requestWeather(cacheweatherId);
        
        refreshLayout.setColorSchemeColors(Color.BLUE,Color.RED,Color.GREEN);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(cacheweatherId);
            }
        });
        
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void loadbackpic() {
        NetWork.getBingApi().getbingurl()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        String picurl = responseBody.string();
                        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("backpic",picurl);
                        editor.commit();
                        Glide.with(WeatherActivity.this)
                                .load(picurl)
                                .into(backpic);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        Toast.makeText(WeatherActivity.this, "获取背景图片失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void requestWeather(String weatherId) {
        cacheweatherId = weatherId;
        //cache
        Observable<Weather> cache = Observable.create(new ObservableOnSubscribe<Weather>() {
            @Override
            public void subscribe(ObservableEmitter<Weather> e) throws Exception {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherjson = preferences.getString("weather", null);
                if(weatherjson != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(weatherjson);
                        JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
                        String weatherContent = jsonArray.getJSONObject(0).toString();
                        e.onNext((new Gson().fromJson(weatherContent,Weather.class)));

                    }catch(JSONException e1){
                        e1.printStackTrace();
                    }
                }else {
                    e.onComplete();
                }
            }
        });
        //网络获取
        Observable<Weather> network =  NetWork.getWeatherApi().getweatherinfo(weatherId,"a8f26d8b6f88437c82ef3d01821e1cba")
                .flatMap(new Function<OldWeather, ObservableSource<Weather>>() {
                    @Override
                    public ObservableSource<Weather> apply(final OldWeather oldWeather) throws Exception {
                        Observable Observable = io.reactivex.Observable.create(new ObservableOnSubscribe<Weather>() {
                            @Override
                            public void subscribe(ObservableEmitter<Weather> e) throws Exception {
                                String json = new Gson().toJson(oldWeather);
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("weather", json);
                                editor.commit();
                                e.onNext(oldWeather.HeWeather.get(0));
                                e.onComplete();
                            }
                        });
                        return Observable;
                    }
                });
        //Rxjava concat
        Observable.concat(cache,network)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Weather>() {
                    @Override
                    public void accept(Weather weather) throws Exception {
                        showWeatherInfo(weather);
                        refreshLayout.setRefreshing(false);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
        
        
        loadbackpic();
    }

    private void showWeatherInfo(Weather weather) {
        if(weather != null && weather.status.equals("ok")) {
            forecast_layout.removeAllViews();
            String cityName = weather.basic.cityName;
            String updatetime = weather.basic.update.updateTime;
            String nowtemp = weather.now.tmp + "°C";
            String nowcond = weather.now.cond.txt;
            String aqi = weather.aqi.city.aqi;
            String pm25 = weather.aqi.city.pm25;
            String comf = "舒适度：" + weather.suggestion.comf.txt;
            String cw = "洗车指数：" + weather.suggestion.cw.txt;
            String sport = "运动建议：" + weather.suggestion.sport.txt;

            title_city.setText(cityName);
            title_updatetime.setText(updatetime);
            now_temp.setText(nowtemp);
            now_cond.setText(nowcond);
            aqi_aqi.setText(aqi);
            aqi_pm25.setText(pm25);
            suggestion_comf.setText(comf);
            suggestion_cw.setText(cw);
            suggestion_sport.setText(sport);

            View defview = createForecastItem();
            forecast_item_date.setText("日期");
            forecast_item_info.setText("天气");
            forecast_item_max.setText("最高气温");
            forecast_item_min.setText("最低气温");
            forecast_layout.addView(defview);

            for(Forecast forecast : weather.daily_forecast) {
                View view = createForecastItem();
                forecast_item_date.setText(forecast.date);
                forecast_item_info.setText(forecast.cond.txt_d);
                forecast_item_max.setText(forecast.tmp.max);
                forecast_item_min.setText(forecast.tmp.min);
                forecast_layout.addView(view);
            }

            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent); 
        }else {
            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }
    
    private View createForecastItem(){
        View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecast_layout,false);
        forecast_item_date = view.findViewById(R.id.forecast_item_date);
        forecast_item_info = view.findViewById(R.id.forecast_item_info);
        forecast_item_max = view.findViewById(R.id.forecast_item_max);
        forecast_item_min = view.findViewById(R.id.forecast_item_min);
        return view;
    }
}
