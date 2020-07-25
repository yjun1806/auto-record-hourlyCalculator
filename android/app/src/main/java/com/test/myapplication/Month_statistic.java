package com.test.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Month_statistic extends AppCompatActivity {
    TextView Month_num, Month_total_pay, Month_total_time;
    TextView Day_average_pay, Day_average_time;
    TextView Month_max_pay, Month_min_pay;
    TextView Date;

    int iMonth_num, iMonth_total_pay, iMonth_total_time;
    int iDay_average_pay, iDay_average_time;
    int iMonth_max_pay, iMonth_min_pay;

    Index_day_info day_index = new Index_day_info();
    ArrayList<Day_Info> day_infos = new ArrayList<>();
    SharedPreferences mPrefs;
    SharedPreferences.Editor prefsEditor;
    private Gson gson;
    private String Place_id;
    private String sYearMonth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_statistic);
        setTitle("월간 통계");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true); // 액션바에 뒤로가기 버튼 삽입부분
        actionBar.setHomeButtonEnabled(true);

        bind_layout();
        read_from_sharedpreference();
        init_data();
    }

    private void init_data() {
        // 이번달 근무 일수 초기화
        iMonth_num = day_infos.size();

        if(iMonth_num != 0) {
            for (int i = 0; i < day_infos.size(); i++) {
                iMonth_total_pay += day_infos.get(i).Daily_total_pay; // 총 급여 초기화
                iMonth_total_time += day_infos.get(i).Daily_total_time_min; // 총 시간 초기화, 분단위
            }

            iDay_average_pay = iMonth_total_pay / iMonth_num; // 하루 평균 급여 = 월급 / 일수
            iDay_average_time = iMonth_total_time / iMonth_num; // 하루 평균 근무 시간 = 총 근무시간/일수

            // 이번달 최고, 최저 급여 초기화
            iMonth_max_pay = day_infos.get(0).Daily_total_pay;
            iMonth_min_pay = day_infos.get(0).Daily_total_pay;
            for (int i = 1; i < day_infos.size(); i++) {
                if (iMonth_max_pay < day_infos.get(i).Daily_total_pay) {
                    iMonth_max_pay = day_infos.get(i).Daily_total_pay;
                }
                if (iMonth_min_pay > day_infos.get(i).Daily_total_pay) {
                    iMonth_min_pay = day_infos.get(i).Daily_total_pay;
                }
            }

            set_data();
        }
    }

    private void set_data() {
        Log.d("TTTTTEWTWTEWTWETWET", "set_data: " + sYearMonth.toString());
        Date.setText(sYearMonth.substring(0, 4) + "년 " + sYearMonth.substring(4,6) + "월");
        Month_num.setText(String.valueOf(iMonth_num) + "일");
        Month_total_pay.setText(String.valueOf(iMonth_total_pay) + "원");
        Month_total_time.setText(String.format("%02d시간 %02d분", iMonth_total_time/60, iMonth_total_time%60));
        Day_average_pay.setText(String.valueOf(iDay_average_pay) + "원");
        Day_average_time.setText(String.format("%02d시간 %02d분", iDay_average_time/60, iDay_average_time%60));
        Month_max_pay.setText(String.valueOf(iMonth_max_pay) + "원");
        Month_min_pay.setText(String.valueOf(iMonth_min_pay) + "원");
    }

    private void read_from_sharedpreference() {
        Place_id = getIntent().getStringExtra("Place_id"); // 넘겨받은 근무지 아이디 저장
        sYearMonth = getIntent().getStringExtra("YearMonth"); // 몇년 몇월의 데이터를 확인할지 받아옴
        mPrefs = getSharedPreferences(Place_id, MODE_PRIVATE); // 넘겨받은 아이디로 저장된 근무지 데이터 가져오기
        prefsEditor = mPrefs.edit(); // 에디터에 연결
        gson = new Gson(); // Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(mPrefs.getString("Index", null)); // 인덱스값 가져오기
            for(int i=0; i<jsonArray.length(); i++){
                day_index.index.add(jsonArray.optString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i=0; i< day_index.index.size(); i++){ // 가져온 인덱스값으로 근무기록 가져오기
            String day_index_compare = day_index.index.get(i).substring(0, 6);
            if(day_index_compare.equals(sYearMonth)) {
                String Json = mPrefs.getString(day_index.index.get(i), null);
                Day_Info tmp_day = gson.fromJson(Json, Day_Info.class);
                day_infos.add(tmp_day);
            }
        }

    }

    private void bind_layout() {
        Date = findViewById(R.id.ms_date);
        Month_num = findViewById(R.id.ms_month_num);
        Month_total_pay = findViewById(R.id.ms_month_total_pay);
        Month_total_time = findViewById(R.id.ms_month_total_time);
        Day_average_pay = findViewById(R.id.ms_day_average_pay);
        Day_average_time = findViewById(R.id.ms_day_average_time);
        Month_max_pay = findViewById(R.id.ms_month_max_pay);
        Month_min_pay = findViewById(R.id.ms_month_min_pay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.statisticmenu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.year_static:
                Intent intent = new Intent(getApplicationContext(), Year_statistic.class);
                intent.putExtra("Place_info", Place_id);
                startActivity(intent);
                break;
        }

        return true;
    }
}
