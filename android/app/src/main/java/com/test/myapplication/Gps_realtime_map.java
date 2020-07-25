package com.test.myapplication;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public class Gps_realtime_map extends AppCompatActivity implements OnMapReadyCallback{
    private static GoogleMap mMap;

    static double saved_latitude = 0, saved_logitude = 0;
    static double now_latitude = 0, now_logitude = 0;


    MapView mapView;
    View mapView2;

    static LatLng sydney, smsm;


    static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gps_realtime_map);
        Log.i(getClass().toString(), "onCreate");

        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);*/

        context = this;

        Intent intent = getIntent();
        saved_latitude = intent.getDoubleExtra("Saved_latitude", 0);
        saved_logitude = intent.getDoubleExtra("Saved_logitude", 0);
        Log.i(getClass().toString(), "넘겨받은 좌표 : " + saved_latitude + " , " + saved_logitude);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView4);
        mapFragment.getMapAsync(this);


    }

    private static void marked_saved_point(double saved_latitude, double saved_logitude) {
        smsm = new LatLng(saved_latitude, saved_logitude);
        Marker saved_marker = mMap.addMarker(new MarkerOptions().position(smsm).title("저장된 근무지"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(smsm));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        saved_marker.showInfoWindow();

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(getClass().toString(), "OnMapReady");
        mMap = googleMap;
        marked_saved_point(saved_latitude, saved_logitude);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(getClass().toString(), "onResume");

    }



    @SuppressLint("HandlerLeak")
    public static Handler gpsHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1: // 위치 탐색중인 경우
                    if(mMap != null) {
                        String[] point = (String[]) msg.obj;
                        now_latitude = Double.parseDouble(point[0]);
                        now_logitude = Double.parseDouble(point[1]);
                        if(now_logitude != 0 && now_latitude != 0) {
                            Log.d("받은 핸들러", point[0] + "," + point[1]);
                            mark_now_position(now_latitude, now_logitude);
                        }
                    }
                    break;
                /*case 2: // 도착한 경우, 액티비티가 켜져있으면 토스트를 띄우고 종료시킴

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                    context.finish();

                    break;*/
            }
        }
    };

    private static void mark_now_position(double now_latitude, double now_logitude) {
        mMap.clear();
        marked_saved_point(saved_latitude, saved_logitude);
        sydney = new LatLng(now_latitude, now_logitude);
        Marker marker = mMap.addMarker(new MarkerOptions().position(sydney).title("현재 위치"));
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

}
