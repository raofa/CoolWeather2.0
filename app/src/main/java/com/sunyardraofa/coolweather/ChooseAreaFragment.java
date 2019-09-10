package com.sunyardraofa.coolweather;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunyardraofa.coolweather.db.City;
import com.sunyardraofa.coolweather.db.Couty;
import com.sunyardraofa.coolweather.db.LocalCity;
import com.sunyardraofa.coolweather.db.LocalCouty;
import com.sunyardraofa.coolweather.db.Province;
import com.sunyardraofa.coolweather.network.NetWork;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseAreaFragment extends Fragment {
    
    private static final String TAG =" ChooseAreaFragment ";
    
    private static final int PROVINCE_LEVEL = 0;
    private static final int CITY_LEVEL = 1;
    private static final int COUTY_LEVEL = 2;
    private int currentlevel;
    private boolean profromnet,cityfromnet,coutyfromnet;
    
    private TextView title;
    private Button back;
    private ListView listView;
    private List<String> datalist = new ArrayList<>();

    private Province selectedProvince;
    private City selectedCity;

    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<LocalCity> localCityList;
    private List<Couty> coutyList = new ArrayList<>();
    private List<LocalCouty> localCoutyList ;
    
    private ArrayAdapter<String> adapter;
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);
        title = view.findViewById(R.id.title);
        back =  view.findViewById(R.id.back);
        listView = view.findViewById(R.id.list);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentlevel == PROVINCE_LEVEL) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if(currentlevel == CITY_LEVEL) {
                    selectedCity = cityList.get(position);
                    queryCouties();
                } else if(currentlevel == COUTY_LEVEL){
                    String weatherId = coutyList.get(position).weatherId;
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity =(WeatherActivity) getActivity();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        activity.drawerLayout.closeDrawers();
                        activity.refreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlevel == COUTY_LEVEL) {
                    queryCities();
                } else if(currentlevel == CITY_LEVEL) {
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }

    private void queryCouties() {
        title.setText(selectedCity.cityName);
        back.setVisibility(View.VISIBLE);
        //数据库
        Observable<List<Couty>> cache = Observable.create(new ObservableOnSubscribe<List<Couty>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Couty>> e) throws Exception {
                localCoutyList = LitePal.where("cityid = ?", String.valueOf(selectedCity.cityId)).find(LocalCouty.class);
                List<Couty> cachecoutyList = new ArrayList<>();
                if(localCoutyList != null &&localCoutyList.size()>0){
                    coutyfromnet = false;
                    for(LocalCouty localCouty : localCoutyList){
                        LocalCouty couty = new LocalCouty();
                        couty.weatherId = localCouty.weatherId;
                        couty.coutyid = localCouty.coutyid;
                        couty.coutyName = localCouty.coutyName;
                        cachecoutyList.add(couty);
                    }
                    e.onNext(cachecoutyList);
                }else {
                    coutyfromnet = true;
                    e.onComplete();
                }
            }
        });
        
        //网络
        Observable<List<Couty>> network = NetWork.getAreaApi().getcouty(String.valueOf(selectedProvince.provinceid),String.valueOf(selectedCity.cityId));
    
        //concat
        Observable.concat(cache,network)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Couty>>() {
                    @Override
                    public void accept(List<Couty> couties) throws Exception {
                        Log.e(TAG, "获取成功");
                        if(coutyfromnet) {
                            Log.e(TAG, "网络获取");
                            for(Couty couty : couties) {
                                LocalCouty localCouty = new LocalCouty();
                                localCouty.coutyName = couty.coutyName;
                                localCouty.coutyid = couty.coutyid;
                                localCouty.weatherId = couty.weatherId;
                                localCouty.cityid = selectedCity.cityId;
                                localCouty.save();
                            }
                        }
                        Log.e(TAG, "数据库获取成功");
                        datalist.clear();
                        coutyList.clear();
                        for(Couty couty : couties) {
                            Log.e("name", couty.coutyName);
                            datalist.add(couty.coutyName);
                            coutyList.add(couty);
                        }
                        listView.setSelection(0);
                        adapter.notifyDataSetChanged();
                        currentlevel = COUTY_LEVEL;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void queryProvinces() {
        title.setText("中国");
        back.setVisibility(View.GONE);
        //数据库获取
        Observable<List<Province>> cache = Observable.create(new ObservableOnSubscribe<List<Province>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Province>> e) throws Exception {
                List<Province> cacheprovinceList;
                cacheprovinceList = LitePal.findAll(Province.class);
                if(cacheprovinceList != null &&cacheprovinceList.size()>0){
                    profromnet = false;
                    Log.e(TAG, "数据库获取");
                    e.onNext(cacheprovinceList);
                }else {
                    profromnet = true;
                    e.onComplete();
                }
            }
        });
        //net获取
        Observable<List<Province>> network = NetWork.getAreaApi().getprovince();
        
        //RxJava concat
        Observable.concat(cache,network)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Province>>() {
                    @Override
                    public void accept(List<Province> provinces) throws Exception {
                        Log.e(TAG, "获取成功");
                        if(profromnet) {
                            Log.e("+++++++++++++++", "网络获取");
                            for(Province province : provinces) {
                                province.save();
                            }
                        }
                        datalist.clear();
                        provinceList.clear();
                        for(Province province : provinces) {
                            Log.e(TAG, province.provincename);
                            datalist.add(province.provincename);
                            provinceList.add(province);
                        }
                        listView.setSelection(0);
                        adapter.notifyDataSetChanged();
                        currentlevel = PROVINCE_LEVEL ;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void queryCities() {
        title.setText(selectedProvince.provincename);
        back.setVisibility(View.VISIBLE);
        //数据库
        Observable<List<City>> cache = Observable.create(new ObservableOnSubscribe<List<City>>() {
            @Override
            public void subscribe(ObservableEmitter<List<City>> e) throws Exception {
                localCityList = LitePal.where("provinceid = ?",String.valueOf(selectedProvince.provinceid)).find(LocalCity.class);
                List<City> cachecityList = new ArrayList<>();
                if(localCityList != null &&localCityList.size()>0){
                    cityfromnet = false;
                    for(LocalCity localCity : localCityList){
                        City city = new City();
                        city.cityId = localCity.cityId;
                        city.cityName = localCity.cityName;
                        cachecityList.add(city);
                    }
                    e.onNext(cachecityList);
                }else {
                    cityfromnet = true;
                    e.onComplete();
                }
            }
        });
        //网络
        Observable<List<City>> network = NetWork.getAreaApi().getcity(String.valueOf(selectedProvince.provinceid));

        //concat
        Observable.concat(cache,network)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<City>>() {
                    @Override
                    public void accept(List<City> cities) throws Exception {
                        Log.e(TAG, "获取成功");
                        if(cityfromnet) {
                            Log.e(TAG,"网络获取");
                            for(City city : cities) {
                                LocalCity localCity = new LocalCity();
                                localCity.cityId = city.cityId;
                                localCity.cityName = city.cityName;
                                localCity.provinceid = selectedProvince.provinceid;
                                localCity.save();
                            }
                        }
                        datalist.clear();
                        cityList.clear();
                        for(City city : cities) {
                            Log.e("name", city.cityName);
                            datalist.add(city.cityName);
                            cityList.add(city);
                        }
                        listView.setSelection(0);
                        adapter.notifyDataSetChanged();
                        currentlevel = CITY_LEVEL;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
