package com.test.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
* 안드로이드 차트 만들기
* https://github.com/PhilJay/MPAndroidChart
*
* */

public class Year_statistic extends AppCompatActivity {

    BarChart barChart;

    Index_day_info day_index = new Index_day_info();
    ArrayList<Day_Info> day_infos = new ArrayList<>();
    String[] month = {"1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};
    int[] month_total_pay = new int[12];
    int year_total_pay;
    int year_total_num;

    SharedPreferences mPrefs;
    SharedPreferences.Editor prefsEditor;
    private Gson gson;
    private String Place_id;

    int Nyear;
    TextView tyear, year_num, year_pay;

    ImageButton pre, next;

    List<BarEntry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_year_statistic);
        setTitle("연간 통계");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true); // 액션바에 뒤로가기 버튼 삽입부분
        actionBar.setHomeButtonEnabled(true);

        Nyear = Calendar.getInstance().get(Calendar.YEAR);
        tyear = findViewById(R.id.ys_year);
        tyear.setText(String.valueOf(Nyear) + "년");

        barChart = findViewById(R.id.chart);
        entries = new ArrayList<>();

        pre = findViewById(R.id.ys_pre);
        next = findViewById(R.id.ys_next);
        year_num = findViewById(R.id.ys_year_num);
        year_pay = findViewById(R.id.ys_year_pay);

        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nyear--;
                tyear.setText(String.valueOf(Nyear) + "년");
                read_from_sharedpreference();
                display_chart();
                barChart.notifyDataSetChanged();
                barChart.invalidate();
            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nyear++;
                tyear.setText(String.valueOf(Nyear) + "년");
                read_from_sharedpreference();
                display_chart();
                barChart.notifyDataSetChanged();
                barChart.invalidate();
            }
        });




       /* read_from_sharedpreference();
        display_chart();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("연간통계", "onResum");
        read_from_sharedpreference();
        display_chart();
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    private void display_chart() {
        //BarEntry(x축, y축) 차트에 표시할 데이터 입력

        // 해당 연도의 데이터를 월별로 분리해서, 월별 일급 총합 구하기

        entries.clear();

        for(int i=0; i<12; i++){
            entries.add(new BarEntry(i, month_total_pay[i]));
        }

        BarDataSet barDataSet = new BarDataSet(entries, "월간 급여");
        barChart.setTouchEnabled(false); // 터치 잠금
        barChart.getDescription().setEnabled(false);


        XAxis xAxis = barChart.getXAxis(); // x축 커스텀
        xAxis.setValueFormatter(new MyxAxisValueFormatter(month)); // x축 데이터 넣기
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 위치 정하기
        xAxis.setTextColor(Color.BLUE); // x축 색상
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true); // x축 그리드 라인 설정
        xAxis.setLabelCount(12); // x축 라벨의 표시할 개수 설정


        YAxis yAxis = barChart.getAxisRight();
        yAxis.setEnabled(false);
        yAxis.setAxisMinimum(0f);

        YAxis yAxis1 = barChart.getAxisLeft();
        yAxis1.setAxisMinimum(0f);


        BarData data = new BarData(barDataSet);
        barChart.setData(data);

    }

    private void read_from_sharedpreference() {


        Place_id = getIntent().getStringExtra("Place_info"); // 넘겨받은 근무지 아이디 저장
        Log.i(getClass().toString(), "넘겨받은 id : " + Place_id);
        mPrefs = getSharedPreferences(Place_id, MODE_PRIVATE); // 넘겨받은 아이디로 저장된 근무지 데이터 가져오기
        prefsEditor = mPrefs.edit(); // 에디터에 연결
        gson = new Gson(); // Gson 객체 생성


        String index = mPrefs.getString("Index", null);
        day_index.index.clear();
        Log.i(getClass().toString(), "가져온 인덱스 : " + index);
        try {
            JSONArray jsonArray = new JSONArray(index); // 인덱스값 가져오기
            for(int i=0; i<jsonArray.length(); i++){
                day_index.index.add(jsonArray.optString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //해당 연도의 데이터만 가져오기
        day_infos.clear();


        for(int i=0; i< day_index.index.size(); i++){
            String day_index_compare = day_index.index.get(i).substring(0, 4);
            Log.i("인덱스 연도 확인 ", day_index_compare +", "+Nyear+ "반복 몇번? " +i);
            if(day_index_compare.equals(String.valueOf(Nyear))) {
                String Json = mPrefs.getString(day_index.index.get(i), null);
                Day_Info tmp_day = gson.fromJson(Json, Day_Info.class);
                Log.i("얼마나 저장되니? ", day_index_compare + "반복 몇번? " + i);
                day_infos.add(tmp_day);
            }
        }
        year_total_num = day_infos.size();

        for(int i=0; i<month_total_pay.length; i++){ // 0으로 초기화
            month_total_pay[i] = 0;
        }
        year_total_pay = 0;

        for(int i=0; i <day_infos.size(); i++){
            year_total_pay += day_infos.get(i).Daily_total_pay;
            switch (day_infos.get(i).month){
                case 1:
                    month_total_pay[0] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+" /1월 총계 : " + month_total_pay[0]);
                    break;
                case 2:
                    month_total_pay[1] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/2월 총계 : " + month_total_pay[1]);
                    break;
                case 3:
                    month_total_pay[2] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/3월 총계 : " + month_total_pay[2]);
                    break;
                case 4:
                    month_total_pay[3] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/4월 총계 : " + month_total_pay[3]);
                    break;
                case 5:
                    month_total_pay[4] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/5월 총계 : " + month_total_pay[4]);
                    break;
                case 6:
                    month_total_pay[5] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/6월 총계 : " + month_total_pay[5]);
                    break;
                case 7:
                    month_total_pay[6] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/7월 총계 : " + month_total_pay[6]);
                    break;
                case 8:
                    month_total_pay[7] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/8월 총계 : " + month_total_pay[7]);
                    break;
                case 9:
                    month_total_pay[8] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/9월 총계 : " + month_total_pay[8]);
                    break;
                case 10:
                    month_total_pay[9] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/10월 총계 : " + month_total_pay[9]);
                    break;
                case 11:
                    month_total_pay[10] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/11월 총계 : " + month_total_pay[10]);
                    break;
                case 12:
                    month_total_pay[11] += day_infos.get(i).Daily_total_pay;
                    Log.i("월", i+"/12월 총계 : " + month_total_pay[11]);
                    break;
            }
        }

        DecimalFormat format = new DecimalFormat("###,###");
        String pay = format.format(Double.valueOf(year_total_pay));
        year_num.setText(String.format("%d 일", year_total_num));
        year_pay.setText(pay + "원");



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }
}
