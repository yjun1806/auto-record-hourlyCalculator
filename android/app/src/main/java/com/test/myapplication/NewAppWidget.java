package com.test.myapplication;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {
    static SharedPreferences mPref;
    static SharedPreferences.Editor Editor;
    static String Place_id;
    static int Place_position;
    static String Place_name;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if(action.equals("android.appwidget.action.APPWIDGET_UPDATE")){
            Log.i("위젯", "위젯 업데이트 메세지를 받아 업데이트한다!!");

            Bundle extras = intent.getExtras();
            int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
            }

        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.i("AppWidget", "updateAppWidget");

        //설정에서 저장한(보기위해) 근무지 정보 가져오기
        mPref = context.getSharedPreferences("Setup", Context.MODE_PRIVATE);
        //Editor = mPref.edit();
        SharedPreferences nPref = context.getSharedPreferences("PlaceInfo", Context.MODE_PRIVATE);


        Place_id = mPref.getString("Choiced_Place_id", null);
        Place_position = mPref.getInt("Choiced_place_position", 0);
        Place_name = mPref.getString("Choiced_place_name", null);



        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.wi_place_name, Place_name);

        if(nPref.getString(Place_id, null) == null){
            views.setTextViewText(R.id.wi_place_name, "선택된 근무지가 없습니다. 설정에서 선택해주세요.");

        }else {
            //근무기록보기 버튼을 눌렀을때의 이벤트를 발생시키는 부분
            Intent intent = new Intent(context, Working_history.class);
            intent.putExtra("Place id", Place_id); // 근무지 아이디와, 포지션을 보낸다.
            Log.i("위젯", "위젯에서 보내는 id값 : " + Place_id);
            intent.putExtra("Place_postion", Place_position);
            Log.i("위젯", "위젯에서 보내는 position값 : " + Place_position);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.wi_history, pendingIntent);

            //근무기록추가 버튼을 눌렀을때의 이벤트
            Intent intent1 = new Intent(context, Inputdata.class);
            intent1.putExtra("Place id", Place_id);
            PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.wi_history_add, pendingIntent1);

        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("AppWidget", "onUpdate");


        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.i("AppWidget", "onEnabled");
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        Log.i("AppWidget", "onDisabled");
        // Enter relevant functionality for when the last widget is disabled
    }
}

