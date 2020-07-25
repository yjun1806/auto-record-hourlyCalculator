package com.test.myapplication;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.Calendar;

import static com.test.myapplication.Place_add.Calculate_time;
import static com.test.myapplication.Place_add.getAddress;

public class Place_modify extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "Place_Add";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    TextView st, et, wt, wl;
    EditText working_name, pay;
    private GoogleMap mMap;
    public double latitude;
    public double longitude;
    MainWorkingPlace_Info workingPlace_infos;
    public String[] week_checked = new String[7]; // 요일 체크 부분, 체크되면 1 아니면 0 인덱스는 0 : 일, 1: 월, 2: 화 3: 수 4: 목 5: 금 6: 토
    int mHour_st, mMinute_st, mHour_et, mMinute_et; // 출근 시간 , 분 퇴근 시간, 분 변수
    CheckBox[] week = new CheckBox[7];
    RadioButton ra5, ra15, ra30, rano;

    public AlarmManager malarmManager;

    SharedPreferences mPrefs;
    SharedPreferences.Editor prefsEditor;
    private Gson gson;
    private String key = "PlaceInfoData";

    String get_id;
    int get_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_modify);
        st = (TextView) findViewById(R.id.pm_working_start_time);
        et = (TextView) findViewById(R.id.pm_working_end_time);
        wt = (TextView) findViewById(R.id.pm_working_time_cal);
        wl = (TextView) findViewById(R.id.pm_working_locate);
        working_name = (EditText)findViewById(R.id.pm_working_name);
        pay = (EditText)findViewById(R.id.pm_hour_pay);
        week[0] = (CheckBox)findViewById(R.id.pm_sunday); // 추가 버튼을 누르는 순간 선택되어있는 체크박스의 데이터가 넘어가도록 하는 부분.
        week[1] = (CheckBox)findViewById(R.id.pm_monday);
        week[2] = (CheckBox)findViewById(R.id.pm_tuesday);
        week[3] = (CheckBox)findViewById(R.id.pm_wednesday);
        week[4] = (CheckBox)findViewById(R.id.pm_thursday);
        week[5] = (CheckBox)findViewById(R.id.pm_friday);
        week[6] = (CheckBox)findViewById(R.id.pm_saturday);
        ra5 = findViewById(R.id.pm_5m);
        ra15 = findViewById(R.id.pm_15m);
        ra30 = findViewById(R.id.pm_30m);
        rano = findViewById(R.id.pm_no);

        //workingPlace_infos = (MainWorkingPlace_Info) intent.getSerializableExtra("workingPlace");
        malarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        //SharedPreference 관련 코드
        mPrefs = getSharedPreferences("PlaceInfo", MODE_PRIVATE); //현재 근무지의 상태정보는 PlaceInfo라는 xml파일에 SharedPreference를 이용해서 저장해준다.
        prefsEditor = mPrefs.edit(); // sharedPreference를 수정하기 위한 에디터를 연결해준다.
        gson = new Gson(); // 리스트를 통쨰로 저장하기 위한 gson 객체를 생성해준다.
        get_id = getIntent().getStringExtra("Place info_id");
        get_position = getIntent().getIntExtra("Place info_postion", 0);
        Log.i(getClass().toString(), "수정하기 위한 인덱스 : " + get_id + " , " + get_position);
        workingPlace_infos = get_Place_Info_data();

        if(isServicesOK()){ // 구글플레이 서비스가 작동하는지 확인
            init();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.pm_mapView);
        mapFragment.getMapAsync(this);


    }

    private MainWorkingPlace_Info get_Place_Info_data() {

        String json = mPrefs.getString(get_id, null);
        return gson.fromJson(json, MainWorkingPlace_Info.class);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void init(){
        latitude = Double.parseDouble(workingPlace_infos.Place_latitude);
        longitude = Double.parseDouble(workingPlace_infos.Place_longitude);
        mHour_st = Integer.valueOf(workingPlace_infos.Start_time_hour);
        mMinute_st = Integer.valueOf(workingPlace_infos.Start_time_min);
        mHour_et = Integer.valueOf(workingPlace_infos.End_time_hour);
        mMinute_et = Integer.valueOf(workingPlace_infos.End_time_min);



        st.setText(String.format("%02d:%02d", mHour_st, mMinute_st));
        et.setText(String.format("%02d:%02d",mHour_et,mMinute_et ));
        wt.setText(workingPlace_infos.Working_time+"시간 "+workingPlace_infos.Working_time_min+"분");
        wl.setText(getAddress(getApplicationContext(), latitude, longitude));
        working_name.setText(workingPlace_infos.Place_name);
        pay.setText(workingPlace_infos.Hour_pay);

        if(workingPlace_infos.week[0].equals("일"))
            week[0].setChecked(true);
        if(workingPlace_infos.week[1].equals("월"))
            week[1].setChecked(true);
        if(workingPlace_infos.week[2].equals("화"))
            week[2].setChecked(true);
        if(workingPlace_infos.week[3].equals("수"))
            week[3].setChecked(true);
        if(workingPlace_infos.week[4].equals("목"))
            week[4].setChecked(true);
        if(workingPlace_infos.week[5].equals("금"))
            week[5].setChecked(true);
        if(workingPlace_infos.week[6].equals("토"))
            week[6].setChecked(true);

        if(workingPlace_infos.when_notify[0]){
            rano.setChecked(true);
        }else if(workingPlace_infos.when_notify[1]){
            ra5.setChecked(true);
        }else if(workingPlace_infos.when_notify[2]){
            ra15.setChecked(true);
        }else if(workingPlace_infos.when_notify[3]){
            ra30.setChecked(true);
        }



        Button btnMap = (Button) findViewById(R.id.pm_map_in_add_button);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Place_modify.this, MapsActivity.class);
                startActivityForResult(intent, 1); // 지도 버튼 클릭시 넘어가는 이벤트, 결과값으로 근무지의 좌표를 얻어온다.
            }
        });



    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Place_modify.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Place_modify.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    protected void onClick_time_setup(View v){
        switch (v.getId()){
            case R.id.pm_start_time_setup:
                new TimePickerDialog(Place_modify.this, mTimeSetListener, mHour_st, mMinute_st, true).show();

                break;
            case R.id.pm_end_time_setup:
                new TimePickerDialog(Place_modify.this, mTimeSetListener2, mHour_et, mMinute_et, true).show();


                break;
        }

    }

    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour_st = hourOfDay;
            mMinute_st = minute;

            st.setText(String.format("%02d:%02d", mHour_st, mMinute_st));
            int working_min = Calculate_time(mHour_st, mMinute_st, mHour_et, mMinute_et);
            wt.setText(String.format("%d시간 %d분", working_min/60, working_min%60));

        }
    };

    TimePickerDialog.OnTimeSetListener mTimeSetListener2 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour_et = hourOfDay;
            mMinute_et = minute;

            et.setText(String.format("%02d:%02d", mHour_et, mMinute_et));
            int working_min = Calculate_time(mHour_st, mMinute_st, mHour_et, mMinute_et);
            wt.setText(String.format("%d시간 %d분", working_min/60, working_min%60));


        }
    };


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // 이부분 왜 자꾸 파란바다만 띄우는걸까
        Log.i(getClass().toString(), workingPlace_infos.Place_latitude + " , " + workingPlace_infos.Place_longitude);

        mMap.clear();
        LatLng sydney = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Place"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override // 지도에서 얻은 좌표를 받환받는 부분, startActivityForResult 의 결과값을 처리한다.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                latitude = data.getExtras().getDouble("latitude");
                longitude = data.getExtras().getDouble("longitude");
                wl.setText(getAddress(getApplicationContext(), latitude, longitude));
                Toast.makeText(this, latitude + " , " + longitude, Toast.LENGTH_SHORT).show();
                mMap.clear();

                LatLng save_location = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(save_location).title("저장된 장소"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(save_location));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        }
    }

    protected void onClick_pm_cancel(View v){ // 확인을 누르면 이전 액티비티로 이동
        finish();
    }


    public void onClick_pm_adding(View v){ // 수정버튼을 누르면 이전 액티비티로 이동, 이동 시 변경된 데이터 SharedPreference에 저장

        if(working_name.getText().toString().equals("") || pay.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "빈칸을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = getIntent();

            int working_min = Calculate_time(mHour_st, mMinute_st, mHour_et, mMinute_et);
            for(int i=0; i<7; i++){
                if(week[i].isChecked()){
                    week_checked[i] = (String) week[i].getText();
                }else {
                    week_checked[i] = "";
                }
            }

            // 정해진 시간 전에 알림을 주는 부분, 해당 기능은 어플이 종료되어도 실행된다.
            Calendar noti_cal = Calendar.getInstance();
            Log.i(getClass().toString(), String.valueOf(mHour_st +" 정해진 시간 " +  mMinute_st + "분"));
            noti_cal.set(Calendar.HOUR_OF_DAY, mHour_st); // 타임피커로 정한 근무시간을 세팅해준다. 세팅한 근무시간 n분전에 알림이 울리도록 하기 위함이다.
            noti_cal.set(Calendar.MINUTE, mMinute_st);
            Log.i(getClass().toString(), String.valueOf(noti_cal.get(Calendar.HOUR_OF_DAY) +" 세팅 시간 " +  noti_cal.get(Calendar.MINUTE) + "분"));


            Intent noti_intent2 = new Intent(getApplicationContext(), BroadcastN.class); // Broadcast를 실행하는 인텐트를 만들어준다.
            PendingIntent notiPending2; // 임시 인텐트인 PendingIntent를 만들어준다.

            if(rano.isChecked()){
                Log.i(getClass().toString(), "알림안함");
                workingPlace_infos.when_notify[0] = true; // 0번 : 알림안함

            }else if(ra5.isChecked()){
                workingPlace_infos.when_notify[1] = true; // 1번 : 5분전 알림
                Log.i(getClass().toString(), "5분전 알림");
                noti_intent2.putExtra("time", "5분전"); // 인텐트로 몇 분전에 알림을 하는지 보내준다. 이 값을 통해 노티피케이션의 내용을 바꿔준다.
                Log.i(getClass().toString(), "5분전 알림2");
                notiPending2 = PendingIntent.getBroadcast(getApplicationContext(), 4000, noti_intent2, PendingIntent.FLAG_UPDATE_CURRENT); // PendingIntent로 Broadcast를 실행하도록 만들어준다.
                Log.i(getClass().toString(), "5분전 알림3");
                malarmManager.set(AlarmManager.RTC_WAKEUP, noti_cal.getTimeInMillis()-5*60*1000, notiPending2); // 알람매니저를 통해 출근시간 n분전에 알람이 울리도록 설정해준다..
                Log.i(getClass().toString(), "5분전 알림4");
            }else if(ra15.isChecked()){
                workingPlace_infos.when_notify[2] = true;
                Log.i(getClass().toString(), "15분전 알림");
                noti_intent2.putExtra("time", "15분전");
                notiPending2 = PendingIntent.getBroadcast(getApplicationContext(), 4000, noti_intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                malarmManager.set(AlarmManager.RTC_WAKEUP, noti_cal.getTimeInMillis()-15*60*1000, notiPending2);

            }else if(ra30.isChecked()){
                workingPlace_infos.when_notify[3] = true;
                Log.i(getClass().toString(), "30분전 알림");
                noti_intent2.putExtra("time", "30분전");
                notiPending2 = PendingIntent.getBroadcast(getApplicationContext(), 4000, noti_intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                malarmManager.set(AlarmManager.RTC_WAKEUP, noti_cal.getTimeInMillis()-30*60*1000, notiPending2);
            }


            workingPlace_infos.Place_name = working_name.getText().toString();
            workingPlace_infos.Start_time_hour = String.valueOf(mHour_st);
            workingPlace_infos.Start_time_min = String.valueOf(mMinute_st);
            workingPlace_infos.End_time_hour = String.valueOf(mHour_et);
            workingPlace_infos.End_time_min =  String.valueOf(mMinute_et);
            workingPlace_infos.Hour_pay = pay.getText().toString();
            workingPlace_infos.Working_time = String.valueOf(working_min/60);
            workingPlace_infos.Working_time_min =  String.valueOf(working_min%60);
            workingPlace_infos.Place_latitude = String.valueOf(latitude);
            workingPlace_infos.Place_longitude =  String.valueOf(longitude);
            workingPlace_infos.week =  week_checked;


            Changed_Place_Info_toSharedPreference(workingPlace_infos);

            //intent.putExtra("replace_data", workingPlace_infos);

            setResult(RESULT_OK, intent);
            finish();
        }

    }

    private void Changed_Place_Info_toSharedPreference(MainWorkingPlace_Info workingPlace_infos) { // 수정을 통해 저장된 데이터를 바꿔주는 메소드

        String json = gson.toJson(workingPlace_infos); // 해당 객체를 json String으로 변환시켜준다.

        Log.i(getClass().toString(), "바꾸는 데이터 값 : " + get_id);

        prefsEditor.putString(get_id, json); // 어차피 해쉬맵구조이기 때문에 그냥 넣으면 치환된다.

        prefsEditor.commit(); // 해당 변화를 확정지어준다.
    }


}
