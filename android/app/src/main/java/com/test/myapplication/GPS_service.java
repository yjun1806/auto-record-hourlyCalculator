package com.test.myapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class GPS_service extends Service {
    private LocationManager lm;
    private String CHANNEL_ID = "GPS";
    double saved_latitude, saved_logitude;
    double now_latitude, now_logitude;
    String Place_id, Place_name;
    Location location_saved, location_now;
    NotificationManager manager;
    Thread thread;

    SharedPreferences nPrefs, mPref;
    SharedPreferences.Editor Editor;
    Gson gson = new Gson();

    private Index_day_info day_index;

    private ScreenReceiver mReceiver = null;

    boolean threadFlag = true;
    private boolean arrival = false;


    public GPS_service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("GPS서비스", "onCreate");


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // 위치관리자 객체 얻기
        day_index = new Index_day_info();

        createNotificationChannel();

        mReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (threadFlag){
                    try {
                        if(!arrival) {
                            Thread.sleep(2000);
                            //핸들러로 메세지 보내기.
                            Message msg = new Message();
                            msg.what = 1;
                            Log.d("서비스 22222", now_latitude + "," + now_logitude);
                            String[] point = {String.valueOf(now_latitude), String.valueOf(now_logitude)};
                            Log.d("서비스 핸들러", point[0] + "," + point[1]);
                            msg.obj = point;
                            Gps_realtime_map.gpsHandler.sendMessage(msg);
                        }else {
                            Message msg = new Message();
                            msg.what = 2;
                            Log.d("서비스 핸들러", "근무지 도착");
                            Gps_realtime_map.gpsHandler.sendMessage(msg);
                        }
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("GPS서비스", "GPS 탐지 서비스 시작");

        if(intent != null){
            if(intent.getAction()==null){
                if(mReceiver==null){
                    mReceiver = new ScreenReceiver();
                    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                    registerReceiver(mReceiver, filter);

                }
            }
        }

        //넘겨받은 데이터 세팅
        Place_id = intent.getStringExtra("PlaceId");
        saved_latitude = Double.parseDouble(intent.getStringExtra("Saved_latitude"));
        saved_logitude = Double.parseDouble(intent.getStringExtra("Saved_logitude"));
        Place_name = intent.getStringExtra("Search_place_name");
        Log.i("GPS서비스", "넘겨받은 데이터 = id : " + Place_id + " / " + saved_latitude + " , " + saved_logitude);

        // 저장된 위도 경도값을 로케이션 값으로 변경
        location_saved = new Location("Point A");
        location_now = new Location("Point B");
        location_saved.setLatitude(saved_latitude);
        location_saved.setLongitude(saved_logitude);

        Intent noti_Intent = new Intent(this, Gps_realtime_map.class);
        noti_Intent.putExtra("Saved_latitude", saved_latitude);
        noti_Intent.putExtra("Saved_logitude", saved_logitude);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, noti_Intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("현재 사용자의 GPS 위치 탐색 중")
                .setContentText("근무지 [ " + Place_name + " ]의 자동기록을 위해 사용자 위치를 탐색중입니다.")
                .setSmallIcon(R.drawable.right)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                100, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);




        return START_STICKY_COMPATIBILITY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.d("GPS서비스", "서비스 파괴!!");


        threadFlag = false;
        thread.interrupt();
        if(mReceiver != null){

            unregisterReceiver(mReceiver);

        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            Log.d("gps service", "onLocationChanged, location:" + location);
            now_logitude = location.getLongitude(); //경도
            now_latitude = location.getLatitude();   //위도
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.

            location_now.setLatitude(now_latitude);
            location_now.setLongitude(now_logitude);

            Log.i("GPS service", now_latitude  + " , " + now_logitude + "변하는 중?");
            // 해당 좌표로 화면 줌


            //두 좌표간 거리 계산
            float distance = location_saved.distanceTo(location_now);
            Log.i("GPS service", "두 좌표 사이의 거리 : " + distance + "m");



            if (distance < 30) {
                Log.i("GPS service", "근무지 도착");
                Intent noti_Intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, noti_Intent, 0);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setContentTitle("근무지 도착")
                        .setContentText("근무지에 도착하여 기록을 자동으로 추가합니다.")
                        .setSmallIcon(R.drawable.right)
                        .setContentIntent(pendingIntent);
                manager.notify(555, builder.build());
                lm.removeUpdates(mLocationListener); // GPS 탐색을 꺼준다.
                arrival = true;
                auto_save();
                
                stopSelf();
            }
        }

        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

    private void auto_save() {
        mPref = getSharedPreferences("PlaceInfo", MODE_PRIVATE);

        String Json = mPref.getString(Place_id, null);
        MainWorkingPlace_Info mi = gson.fromJson(Json, MainWorkingPlace_Info.class);

        int input_year = Calendar.getInstance().get(Calendar.YEAR);
        int input_month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int input_day_of_month = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String day_of_week = null;
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                day_of_week = "월요일";
                break;
            case Calendar.TUESDAY:
                day_of_week = "화요일";
                break;
            case Calendar.WEDNESDAY:
                day_of_week = "수요일";
                break;
            case Calendar.THURSDAY:
                day_of_week = "목요일";
                break;
            case Calendar.FRIDAY:
                day_of_week = "금요일";
                break;
            case Calendar.SATURDAY:
                day_of_week = "토요일";
                break;
            case Calendar.SUNDAY:
                day_of_week = "일요일";
                break;
        }

        int hour_pay = Integer.valueOf(mi.Hour_pay);
        int mHour_st = Integer.valueOf(mi.Start_time_hour);
        int mMinute_st = Integer.valueOf(mi.Start_time_min);
        int mHour_et = Integer.valueOf(mi.End_time_hour);
        int mMinute_et = Integer.valueOf(mi.End_time_min);

        nPrefs = getSharedPreferences(Place_id, MODE_PRIVATE);
        Editor = nPrefs.edit();

        Day_Info day_info = new Day_Info();
        day_info.year = input_year;
        day_info.month = input_month;
        day_info.day_of_month = input_day_of_month;
        day_info.Day_start_time_hour = mHour_st;
        day_info.Day_start_time_min = mMinute_st;
        day_info.Day_end_time_hour = mHour_et;
        day_info.Day_end_time_min = mMinute_et;
        day_info.hour_pay = hour_pay;
        day_info.day_of_week = day_of_week;
        day_info.Calculate();

        day_info.Day_Id = String.format("%04d%02d%02d", input_year, input_month, input_day_of_month); // 해당 기록의 키값은 연+월+일 값, 중복이 없게 만들기 위함
        Log.i(getClass().toString(), day_info.Day_Id);

        String nl = nPrefs.getString("Index", null);
        if (nl == null) {
            Log.i("데이터 추가", "기존 데이터가 없습니다.");
            save_day_info_toSharedPreference(day_info);
        } else {
            set_Index_day_info();
            boolean flags = true;
            for (int i = 0; i < day_index.index.size(); i++) {
                if (day_index.index.get(i).equals(day_info.Day_Id)) {
                    Toast.makeText(getApplicationContext(), "이미 해당 날짜의 근무 기록이 있습니다.", Toast.LENGTH_SHORT).show();
                    flags = false;
                    break;
                }
            }
            if (flags) {
                save_day_info_toSharedPreference(day_info);
            }
        }

    }

    private void set_Index_day_info() { // 저장된 인덱스값을 불러오는 메소드
        Log.i("데이터 추가", "저장된 인덱스 불러오기");

        try {
            JSONArray jsonArray = new JSONArray(nPrefs.getString("Index", null));
            Log.i("데이터추가", "불러온 인덱스 : " + jsonArray);
            day_index.index.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                String index_data = jsonArray.optString(i);
                day_index.index.add(index_data);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void save_day_info_toSharedPreference(Day_Info day_info) {
        Log.i(getClass().toString(), "근무기록 저장" + day_info.Day_Id);


        String json = gson.toJson(day_info); // 해당 객체를 json String으로 변환시켜준다.
        day_index.index.add(day_info.Day_Id); // 생성된 객체의 아이디값을 저장시켜준다.


        Log.i(getClass().toString(), "저장시 리스트크기 : " + day_index.index.size());

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < day_index.index.size(); i++) {
            jsonArray.put(day_index.index.get(i));
        }
        Log.i(getClass().toString(), "저장된 리스트 : " + jsonArray);


        Editor.putString(day_info.Day_Id, json);
        Editor.putString("Index", jsonArray.toString());
        Editor.commit(); // 해당 변화를 확정지어준다.
        Log.i("근무기록추가", "확정된 인덱스 : " + nPrefs.getString("Index", null));

    }


}
