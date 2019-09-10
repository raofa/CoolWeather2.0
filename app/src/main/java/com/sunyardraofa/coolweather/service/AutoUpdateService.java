package com.sunyardraofa.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sunyardraofa.coolweather.WeatherActivity;
import com.sunyardraofa.coolweather.gson.OldWeather;
import com.sunyardraofa.coolweather.gson.Weather;
import com.sunyardraofa.coolweather.network.NetWork;
import com.sunyardraofa.coolweather.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updatebackic();
        
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 5*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i  = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        
        return super.onStartCommand(intent, flags, startId);
    }

    private void updatebackic() {
        NetWork.getBingApi().getbingurl()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        String picurl = responseBody.string();
                        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("backpic",picurl);
                        editor.commit();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    private void updateWeather() {
        Weather weather = new Weather();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherinfo = preferences.getString("weather", null);
        if(weatherinfo != null) {
            try {
                JSONObject jsonObject = new JSONObject(weatherinfo);
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
                String weatherContent = jsonArray.getJSONObject(0).toString();
                weather = new Gson().fromJson(weatherContent, Weather.class);

            } catch(JSONException e1) {
                e1.printStackTrace();
            }
            String weatherId = weather.basic.weatherId;
            NetWork.getWeatherApi().getweatherinfo(weatherId, "a8f26d8b6f88437c82ef3d01821e1cba").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<OldWeather>() {
                @Override
                public void accept(OldWeather oldWeather) throws Exception {
                    String json = new Gson().toJson(oldWeather);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("weather", json);
                    editor.commit();
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    throwable.printStackTrace();
                }
            });

        }
    }
}
