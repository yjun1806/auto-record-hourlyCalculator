package com.test.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;

public class Popup_day_info extends Activity {
    TextView tv, tv_time, tv_pay;
    int day, month, year;
    ArrayList<Day_Info> popday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup_day_info);

        tv = (TextView)findViewById(R.id.popup_day);
        tv_time = findViewById(R.id.popup_time);
        tv_pay = findViewById(R.id.popup_pay);

        Intent intent = getIntent();
        day = Integer.valueOf(intent.getStringExtra("day"));
        month = intent.getIntExtra("month", 0);
        year = intent.getIntExtra("year", 0);
        popday = (ArrayList<Day_Info>) intent.getSerializableExtra("day_list");

        tv.setText(String.format("%d년 %d월 %d일", year, month, day));
        day_time(popday);

    }

    public void day_time(ArrayList<Day_Info> day){
        int tmp_hour=0;
        int tmp_pay=0;
        for(int i=0; i<day.size(); i++){
            if(day.get(i).year == year && day.get(i).month == month && day.get(i).day_of_month == this.day){
                tmp_hour += day.get(i).Daily_total_time_min;
                tmp_pay += day.get(i).Daily_total_pay;
            }
        }
        tv_time.setText(tmp_hour/60 + "시간 " + tmp_hour%60 + "분");
        tv_pay.setText(tmp_pay + "원");
    }


    public void mOnClose(View v){
        //액티비티(팝업) 닫기
        finish();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


}
