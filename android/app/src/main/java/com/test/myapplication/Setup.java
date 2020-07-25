package com.test.myapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;



public class Setup extends AppCompatActivity {
    static Switch noti_sw;
    String choiced_place_name;
    String choiced_place_id;
    int choiced_place_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setTitle("설정");

        noti_sw = findViewById(R.id.noti_Switch);


        SharedPreferences pref = getSharedPreferences("Setup", Activity.MODE_PRIVATE);
        noti_sw.setChecked(pref.getBoolean("service", false));
        TextView cp = findViewById(R.id.choiced_place);
        if(pref.getString("Choiced_place_name", null) == null){
            cp.setText("현재 선택된 근무지 : 없음");
        }else {
            cp.setText("현재 선택된 근무지 : " + pref.getString("Choiced_place_name", null));
        }


        noti_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            Intent intent = new Intent(getApplicationContext(), Quick_add_service.class);

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(noti_sw.isChecked()){
                    startService(intent);
                }else {
                    stopService(intent);
                }
            }
        });



    }

    public void onStop(){
        super.onStop();
        SharedPreferences pref = getSharedPreferences("Setup", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("service", noti_sw.isChecked());
        editor.putString("Choiced_Place_id", choiced_place_id);
        editor.putString("Choiced_place_name", choiced_place_name);
        editor.putInt("Choiced_place_position", choiced_place_position);
        editor.commit();

        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        Intent update = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        update.setClass(this, NewAppWidget.class);
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mgr.getAppWidgetIds(new ComponentName(this, NewAppWidget.class)));
        this.sendBroadcast(update);


    }


    protected void onClick_check(View v){ // 확인을 누르면 이전 액티비티로 이동


        finish(); // 확인 버튼을 누르면 더 이상 액티비티가 쓸모없기 때문에 종료시켜준다.
    }


    public void onClick_choice(View v) throws JSONException {

        // 저장된 근무지 기록 불러오기
        SharedPreferences place = getSharedPreferences("PlaceInfo", MODE_PRIVATE);

        // 저장된 인덱스 배열 가져오기
        String Index_array = place.getString("Index", null);
        JSONArray jsonArray = new JSONArray(Index_array);

        // 변환을 위한 gson 객체 생성
        Gson gson = new Gson();

        // 가져온 인덱스 배열을 사용할수 있도록 배열에 저장
        String[] place_index = new String[jsonArray.length()];
        for(int i =0; i<jsonArray.length(); i++){
            place_index[i] = jsonArray.optString(i);
            Log.i("Setup", "인덱스 : " + i + "번 " + place_index[i]);
        }

        // 가져온 배열을 이용해 근무지 정보를 가져옴
        final MainWorkingPlace_Info[] place_infos = new MainWorkingPlace_Info[jsonArray.length()];
        final CharSequence[] place_name = new CharSequence[jsonArray.length()];
        for(int i=0; i< place_index.length; i++){
            String json = place.getString(place_index[i], null);
            place_infos[i] = gson.fromJson(json, MainWorkingPlace_Info.class);
            place_name[i] = place_infos[i].Place_name;
        }

        // 목록 선택창을 띄우는 부분
        AlertDialog.Builder alert = new AlertDialog.Builder(Setup.this);

        alert.setTitle("위젯에 표시할 근무지를 선택해주세요."); // 타이틀을 정해준다.
        alert.setItems(place_name, new DialogInterface.OnClickListener() { // 목록 리스트를 설정하고, 선택시 처리이벤트를 정해준다.
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), place_name[which] +  " 선택했습니다.", Toast.LENGTH_SHORT).show();
                choiced_place_id = place_infos[which].IDNumber;
                choiced_place_name = String.valueOf(place_name[which]);
                choiced_place_position = place_infos[which].position;
                TextView cp = findViewById(R.id.choiced_place);
                cp.setText("현재 선택된 근무지 : " + choiced_place_name);
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }



}
