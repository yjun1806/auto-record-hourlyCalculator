package com.test.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;

public class History_popup extends Activity {
    Day_Info day; // 인텐트로 전달받은 데이터를 담을 객체변수
    TextView date, hp, hst, het, hwt, hdiary;
    ImageView iv;

    SharedPreferences nPrefs;
    SharedPreferences.Editor Editor;
    private Gson gson;

    String Place_id;
    String Day_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_history_popup);

        date = findViewById(R.id.hp_date);
        hp = findViewById(R.id.hp_pay);
        hst = findViewById(R.id.hp_start_time);
        het = findViewById(R.id.hp_end_time);
        hwt = findViewById(R.id.hp_working_time);
        iv = findViewById(R.id.hp_image);
        hdiary = findViewById(R.id.hp_diary);

        //SharedPreference 관련 코드
        gson = new Gson(); // 리스트를 통쨰로 저장하기 위한 gson 객체를 생성해준다.

        Place_id = getIntent().getStringExtra("Place_id");
        nPrefs = getSharedPreferences(Place_id, MODE_PRIVATE);
        Editor  = nPrefs.edit();

        Day_id = getIntent().getStringExtra("day_info_id");

        Log.i(getClass().toString(), "받아온 아이템 아이디값 : " + Place_id);

        day = get_day_info(Day_id);

        date.setText(String.format("%d년 %02d월 %02d일 ", day.year, day.month, day.day_of_month) + day.day_of_week);
        hp.setText(day.hour_pay + "원");
        hst.setText(String.format("%02d:%02d", day.Day_start_time_hour, day.Day_start_time_min));
        het.setText(String.format("%02d:%02d", day.Day_end_time_hour, day.Day_end_time_min));
        hwt.setText(day.Daily_total_time_min/60 + "시간 " + day.Daily_total_time_min%60 + "분");
        hdiary.setText(day.diary);

        if(day.daily_image_path != null) {
            File file = new File(day.daily_image_path);
            iv.setImageURI(Uri.fromFile(file));
        }
        /*if(day.daily_image != null){
            Bitmap bmp = BitmapFactory.decodeByteArray(day.daily_image, 0, day.daily_image.length); // 인텐트로 넘겨받은 이미지 배열값을 저장
            iv.setImageBitmap(bmp); // 이미지뷰에 표시해준다.
        }*/
    }

    private Day_Info get_day_info(String day_id) {
        String json = nPrefs.getString(day_id, null);
        return gson.fromJson(json, Day_Info.class);
    }



    public void hp_close(View v){
        //액티비티(팝업) 닫기
        finish();
    }

    public void hp_modi(View v){
        Intent intent = new Intent(getApplicationContext(), Detail_history_check_modi.class);
        intent.putExtra("day_info_id", Day_id);
        intent.putExtra("Place_id", Place_id);
        startActivityForResult(intent, 2);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == 2){
                Intent result = getIntent();
                setResult(RESULT_OK, result);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
