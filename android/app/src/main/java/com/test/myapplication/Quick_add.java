package com.test.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;

public class Quick_add extends Activity {
    Spinner Place_spinner;
    AdapterSpinner adapterSpinner;
    SharedPreferences mPref, nPrefs;
    SharedPreferences.Editor editor, Editor;
    Gson gson;

    ArrayList<String> Place_Index = new ArrayList<>();
    ArrayList<MainWorkingPlace_Info> Place_info = new ArrayList<>();

    TextView date, pay, start_time, end_time, total_time, total_pay;
    private int input_year;
    private int input_month;
    private int input_day_of_month;
    int mHour_st, mMinute_st, mHour_et, mMinute_et;
    int mhour_pay, mtotal_pay;
    private String day_of_week;

    private Index_day_info day_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_quick_add);
        Place_spinner = findViewById(R.id.qa_Place_list);
        date = findViewById(R.id.qa_input_date);
        pay = findViewById(R.id.qa_Input_pay);
        start_time = findViewById(R.id.qa_Input_start_time);
        end_time = findViewById(R.id.qa_Input_end_time);
        total_time = findViewById(R.id.qa_Input_working_time);
        total_pay = findViewById(R.id.qa_total_pay);
        adapterSpinner = new AdapterSpinner(this, Place_info);
        day_index = new Index_day_info();
        get_Place_Index();

        Place_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "선택된 근무지 : " + Place_info.get(position).Place_name, Toast.LENGTH_SHORT).show();
                Log.i("퀵애드", "선택된 포지션 : " +position);
                init_data(position);
                nPrefs = getSharedPreferences(Place_info.get(position).IDNumber, MODE_PRIVATE); // 해당 근무지의 기록데이터는 근무지 아이디값으로 생성.
                Editor = nPrefs.edit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Place_spinner.setAdapter(adapterSpinner);

    }

    void init_data(int position) {
        input_year = Calendar.getInstance().get(Calendar.YEAR);
        input_month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        input_day_of_month = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
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
        date.setText(String.format("%d년 %02d월 %02d일 ", input_year, input_month, input_day_of_month) + day_of_week);
        mhour_pay = Integer.valueOf(Place_info.get(position).Hour_pay);
        mHour_st = Integer.valueOf(Place_info.get(position).Start_time_hour);
        mMinute_st = Integer.valueOf(Place_info.get(position).Start_time_min);
        mHour_et = Integer.valueOf(Place_info.get(position).End_time_hour);
        mMinute_et = Integer.valueOf(Place_info.get(position).End_time_min);

        pay.setText(mhour_pay + "원");
        start_time.setText(String.format("%02d:%02d", mHour_st, mMinute_st));
        end_time.setText(String.format("%02d:%02d", mHour_et, mMinute_et));
        total_time.setText(Place_info.get(position).Working_time + "시간 " + Place_info.get(position).Working_time_min + "분");

        mtotal_pay = Calculate(mHour_st, mMinute_st, mHour_et, mMinute_et, mhour_pay );
        total_pay.setText("일급 : " + String.valueOf(mtotal_pay) + "원");
    }

    int Calculate(int Day_start_time_hour,  int Day_start_time_min , int Day_end_time_hour,int Day_end_time_min, int hour_pay){
        int Hour;
        int Min;
        if(Day_end_time_hour >= Day_start_time_hour){
            Hour = Day_end_time_hour - Day_start_time_hour;
            Min = Day_end_time_min - Day_start_time_min;
        }else {
            Hour = Day_end_time_hour + (24-Day_start_time_hour);
            Min = Day_end_time_min - Day_start_time_min;
        }

        int Daily_total_time_min = Hour * 60 + Min;
        int Daily_total_pay = Hour*hour_pay + (Daily_total_time_min%60)*(hour_pay/60);
        return Daily_total_pay;
    }

    private void get_Place_Index() {
        mPref = getSharedPreferences("PlaceInfo", MODE_PRIVATE);
        editor = mPref.edit();
        gson = new Gson();

        String Index = mPref.getString("Index", null);
        try {
            JSONArray jsonArray = new JSONArray(Index);
            for(int i=0; i<jsonArray.length(); i++){
                Place_Index.add(jsonArray.optString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i=0; i<Place_Index.size(); i++){
            String Json = mPref.getString(Place_Index.get(i), null);
            Place_info.add(gson.fromJson(Json, MainWorkingPlace_Info.class));
        }


    }

    protected void qa_onClick_change(View v){
        switch (v.getId()){
            case R.id.qa_Date_ch: // 날짜 변경을 누른 경우
                new DatePickerDialog(this, mDateListener, input_year, input_month-1, input_day_of_month).show();
                break;

            case R.id.qa_pay_ch: // 시급 변경을 누른 경우
                AlertDialog.Builder ad; // 시급변경을 할때 띄워주는 창과 관련된 선언부

                //시급변경 관련 코드
                ad = new AlertDialog.Builder(Quick_add.this);
                ad.setTitle("시급 입력");
                ad.setMessage("수정할 시급을 입력해 주세요.");
                final EditText et = new EditText(Quick_add.this);
                et.setText(String.valueOf(mhour_pay));
                ad.setView(et);
                et.setInputType(InputType.TYPE_CLASS_NUMBER); // 숫자만 입력받도록

                ad.setPositiveButton("변경", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mhour_pay = Integer.valueOf(et.getText().toString());
                        pay.setText(String.format("%d 원",mhour_pay));
                        dialog.dismiss();
                    }
                });

                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();


                break;
            case R.id.qa_st_ch: // 시작시간 변경을 누른 경우
                new TimePickerDialog(this, mTimeSetListener, mHour_st, mMinute_st, true).show();

                break;
            case R.id.qa_et_ch: // 끝나는 시간 변경을 누른 경우
                new TimePickerDialog(this, mTimeSetListener2, mHour_et, mMinute_et, true).show();

                break;



        }


    }
    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour_st = hourOfDay;
            mMinute_st = minute;

            start_time.setText(String.format("%02d:%02d", mHour_st, mMinute_st));
            int working_min = Place_add.Calculate_time(mHour_st, mMinute_st, mHour_et, mMinute_et);
            total_time.setText(String.format("%d시간 %d분", working_min/60, working_min%60));
            mtotal_pay = Calculate(mHour_st, mMinute_st, mHour_et, mMinute_et, mhour_pay );
            total_pay.setText("일급 : " + String.valueOf(mtotal_pay) + "원");

        }
    };

    TimePickerDialog.OnTimeSetListener mTimeSetListener2 = new TimePickerDialog.OnTimeSetListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour_et = hourOfDay;
            mMinute_et = minute;

            end_time.setText(String.format("%02d:%02d", mHour_et, mMinute_et));
            int working_min = Place_add.Calculate_time(mHour_st, mMinute_st, mHour_et, mMinute_et);
            total_time.setText(String.format("%d시간 %d분", working_min/60, working_min%60));
            mtotal_pay = Calculate(mHour_st, mMinute_st, mHour_et, mMinute_et, mhour_pay );
            total_pay.setText("일급 : " + String.valueOf(mtotal_pay) + "원");

        }
    };

    DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            input_year = year;
            input_month = month+1;
            input_day_of_month = dayOfMonth;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            switch (cal.get(Calendar.DAY_OF_WEEK)){
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
            date.setText(String.format("%d년 %02d월 %02d일 ", input_year, input_month, input_day_of_month) + day_of_week);
        }
    };

    public void onClick_qa_cancel(View view) {
        finish();
    }

    public void onClick_qa_add(View view) {

        Day_Info day_info = new Day_Info();
        day_info.year = input_year;
        day_info.month = input_month;
        day_info.day_of_month = input_day_of_month;
        day_info.Day_start_time_hour = mHour_st;
        day_info.Day_start_time_min = mMinute_st;
        day_info.Day_end_time_hour = mHour_et;
        day_info.Day_end_time_min = mMinute_et;
        day_info.hour_pay = mhour_pay;
        day_info.day_of_week = day_of_week;
        day_info.Calculate();

        day_info.Day_Id = String.format("%04d%02d%02d", input_year, input_month, input_day_of_month); // 해당 기록의 키값은 연+월+일 값, 중복이 없게 만들기 위함
        Log.i(getClass().toString(), day_info.Day_Id);

        String nl = nPrefs.getString("Index", null);
        if(nl == null){
            Log.i("데이터 추가", "기존 데이터가 없습니다.");
            save_day_info_toSharedPreference(day_info);
            finish();
        }else {
            set_Index_day_info();
            boolean flags = true;
            for(int i =0; i< day_index.index.size(); i++){
                if(day_index.index.get(i).equals(day_info.Day_Id)){
                    Toast.makeText(getApplicationContext(), "이미 해당 날짜의 근무 기록이 있습니다.", Toast.LENGTH_SHORT).show();
                    flags = false;
                    break;
                }
            }
            if(flags) {
                save_day_info_toSharedPreference(day_info);
                finish();
            }
        }
    }

    private void set_Index_day_info() { // 저장된 인덱스값을 불러오는 메소드
        Log.i("데이터 추가", "저장된 인덱스 불러오기");

        try {
            JSONArray jsonArray = new JSONArray(nPrefs.getString("Index", null));
            Log.i("데이터추가" , "불러온 인덱스 : " + jsonArray);
            day_index.index.clear();
            for(int i =0; i < jsonArray.length(); i++){
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

        /*int Index = nPrefs.getInt("Size", 0); //근무지의 근무기록 파일에 얼마만큼의 데이터가 담겨있는지 확인하기 위해 사이즈값을 가져온다.
        Editor.putInt("Size", Index+1);*/
        Log.i(getClass().toString(), "저장시 리스트크기 : " + day_index.index.size());

        JSONArray jsonArray = new JSONArray();
        for(int i=0; i<day_index.index.size(); i++){
            jsonArray.put(day_index.index.get(i));
        }
        Log.i(getClass().toString(), "저장된 리스트 : " + jsonArray);


        Editor.putString(day_info.Day_Id, json);
        Editor.putString("Index", jsonArray.toString());
        Editor.commit(); // 해당 변화를 확정지어준다.
        Log.i("근무기록추가", "확정된 인덱스 : " + nPrefs.getString("Index", null));
        Toast.makeText(getApplicationContext(), "기록이 추가되었습니다.", Toast.LENGTH_SHORT).show();

    }
}
