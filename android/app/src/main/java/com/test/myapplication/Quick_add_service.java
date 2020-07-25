
package com.test.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class Quick_add_service extends Service {
    public Quick_add_service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(getClass().toString(), "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(getClass().toString(), "onStartCommand");

        String channdlID = "Noti";
        String ChannelName = "자동근무기록 시급계산기";

        //노티피케이션 매니저를 만들어준다. 해당 기능은 노티피케이션을 관리할때 쓰인다.
        //Context에서 노티피케이션 서비스 기능을 받아온다.
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 오레오 이상에도 동작하려면 이부분이 있어야함

            int importance = NotificationManager.IMPORTANCE_DEFAULT; // 중요도 설정 부분
            // 노티피케이션 채널이 있어야 오레오 이상에서 노티를 띄울 수 있다.
            NotificationChannel channel = new NotificationChannel(channdlID, ChannelName, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
            // 노티매니저에 노티피케이션 채널을 만들어준다.
        }


        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        NotificationCompat.Builder mBuilder = // 노티컴팻 빌더를 이용해 노티의 내용을 설정해준다.
                new NotificationCompat.Builder(this, channdlID) // 채널 아이디를 줘야된다.
                        .setContentTitle("빠른기록") // 노티 타이틀
                        .setLargeIcon(icon)
                        .setShowWhen(false)
                        .setContentText("눌러서 간단히 기록을 작성하세요")
                        .setSmallIcon(R.drawable.right)
                        .setOngoing(true) // 노티가 안꺼지도록 해준다.
                        .setAutoCancel(false); //클릭시 노티를 닫을거냐 안닫을거냐
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, Quick_add.class); // 노티클릭시 어떤 액티비티를 띄울것인지
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);


        int requsetId = (int)System.currentTimeMillis(); // 현재 시간을 아이디값으로 준다.

        // 팬딩인텐트를 통해 액티비티를 띄워준다.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, requsetId, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent); // 노티에 해당 기능을 연결

// mId allows you to update the notification later on.

        notificationManager.notify(111, mBuilder.build()); // 노티를 띄워주는 부분.


        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(111);
        Log.i(getClass().toString(), "onDestroy");
    }
}
