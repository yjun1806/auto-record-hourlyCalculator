package com.test.myapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class NotiService extends Service {
    public NotiService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "서비스가 시작되었습니다", Toast.LENGTH_SHORT).show();
        Log.i("Service", "Service Start!!");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("Service", "Noti Start!!");
        String channdlID = "chID";
        String ChannelName = "chName";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 오레오 이상에도 동작하려면 이부분이 있어야함
            Log.i(getClass().toString(), "onClick st2");

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channdlID, ChannelName, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), channdlID)
                        .setSmallIcon(R.drawable.right)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!")
                        .setAutoCancel(true); //클릭시 노티를 닫을거냐 안닫을거냐
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);


        int requsetId = (int)System.currentTimeMillis();

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), requsetId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

// mId allows you to update the notification later on.

        notificationManager.notify(111, mBuilder.build());


        return START_STICKY_COMPATIBILITY;
    }


}
