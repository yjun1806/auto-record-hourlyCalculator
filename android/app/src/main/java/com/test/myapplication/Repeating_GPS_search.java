package com.test.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class Repeating_GPS_search extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(getClass().toString(), "Receive 데이터");

        int mHour_st = intent.getIntExtra("Start_Hour", 0);
        int mMinute_st = intent.getIntExtra("Start_Min", 0);
        String Place_id = intent.getStringExtra("PlaceId");
        String Place_latitude = intent.getStringExtra("Saved_latitude");
        String Place_longitude = intent.getStringExtra("Saved_logitude");
        String Place_name = intent.getStringExtra("Search_place_name");
        Start_GPS_search_service(context, Place_id, Place_latitude, Place_longitude, Place_name);

        Calendar gps_cal = Calendar.getInstance();
        gps_cal.set(Calendar.HOUR_OF_DAY, mHour_st);
        gps_cal.set(Calendar.MINUTE, mMinute_st);
        gps_cal.add(Calendar.MINUTE, -30);
        //gps_cal.add(Calendar.HOUR_OF_DAY, 24);
        long aTime = System.currentTimeMillis(); // 현재시간
        long bTime = gps_cal.getTimeInMillis(); // 설정한 시간

        // 예를들어 현재 시간이 13시이고, 설정한 시간이 12시라면 이미 시간이 지나 다음날 울려야 하므로
        if(aTime > bTime){
            gps_cal.set(Calendar.HOUR_OF_DAY, mHour_st);
            gps_cal.set(Calendar.MINUTE, mMinute_st);
            gps_cal.add(Calendar.MINUTE, -30);
            gps_cal.add(Calendar.HOUR_OF_DAY, 24); // 24시간 후에 울리도록 설정

        }
            Intent gps_intent = new Intent(context, Repeating_GPS_search.class);
            gps_intent.putExtra("PlaceId", Place_id);
            gps_intent.putExtra("Saved_latitude", Place_latitude);
            gps_intent.putExtra("Saved_logitude", Place_longitude);
            gps_intent.putExtra("Start_Hour", mHour_st);
            gps_intent.putExtra("Start_Min", mMinute_st);
            gps_intent.putExtra("Search_place_name", Place_name);

            AlarmManager alarmManager1 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 333, gps_intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager1.set(AlarmManager.RTC_WAKEUP, gps_cal.getTimeInMillis(), pendingIntent);
        

    }

    private void Start_GPS_search_service(Context context, String place_id, String place_latitude, String place_longitude, String place_name) {
        Log.i(getClass().toString(), "서비스 시작시켜!!");
        Intent gps_service = new Intent(context, GPS_service.class);
        gps_service.putExtra("PlaceId", place_id);
        gps_service.putExtra("Saved_latitude", place_latitude);
        gps_service.putExtra("Saved_logitude", place_longitude);
        gps_service.putExtra("Search_place_name", place_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(gps_service);
        } else {
            context.startService(gps_service);
        }

    }
}
