package com.test.myapplication;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import org.json.JSONArray;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Place_add extends AppCompatActivity implements OnMapReadyCallback{
    private static final String TAG = "Place_Add";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public double latitude;
    public double longitude;
    private GoogleMap mMap;
    TextView st, et, wt, wl;
    EditText working_name, pay;
    public String[] week_checked = new String[7]; // 요일 체크 부분, 체크되면 1 아니면 0 인덱스는 0 : 일, 1: 월, 2: 화 3: 수 4: 목 5: 금 6: 토
    int mHour_st, mMinute_st, mHour_et, mMinute_et;
    RadioButton ra5, ra15, ra30, rano;

    boolean[] when_notify = new boolean[4];

    private AlarmManager alarmManager;


    SharedPreferences mPrefs;
    SharedPreferences.Editor prefsEditor;
    private Gson gson;
    private String key = "PlaceInfoData";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_add);
        Log.i(getClass().toString(), "onCreate");
        setTitle("근무지 추가");
        st = (TextView) findViewById(R.id.working_start_time);
        et = (TextView) findViewById(R.id.working_end_time);
        wt = (TextView) findViewById(R.id.working_time_cal);
        wl = (TextView) findViewById(R.id.working_locate);
        working_name = (EditText)findViewById(R.id.working_name);
        pay = (EditText)findViewById(R.id.hour_pay);
        ra5 = findViewById(R.id.pa_5m);
        ra15 = findViewById(R.id.pa_15m);
        ra30 = findViewById(R.id.pa_30m);
        rano = findViewById(R.id.pa_no);

        wl.setText(getAddress(getApplicationContext(), latitude, longitude));

        alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        //SharedPreference 관련 코드
        mPrefs = getSharedPreferences("PlaceInfo", MODE_PRIVATE); //현재 근무지의 상태정보는 PlaceInfo라는 xml파일에 SharedPreference를 이용해서 저장해준다.
        prefsEditor = mPrefs.edit(); // sharedPreference를 수정하기 위한 에디터를 연결해준다.
        gson = new Gson(); // 리스트를 통쨰로 저장하기 위한 gson 객체를 생성해준다.



        //Calendar cal = new GregorianCalendar();
        //mHour_st = cal.get(Calendar.HOUR_OF_DAY);
        //mMinute_et = cal.get(Calendar.MINUTE);

        if(isServicesOK()){ // 구글플레이 서비스가 작동하는지 확인
            init();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);


    }

    protected void onClick_time_setup(View v){
        switch (v.getId()){
            case R.id.start_time_setup:
                new TimePickerDialog(Place_add.this, mTimeSetListener, mHour_st, mMinute_st, true).show();

                break;
            case R.id.end_time_setup:
                new TimePickerDialog(Place_add.this, mTimeSetListener2, mHour_et, mMinute_et, true).show();


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


    protected void onClick_cancel(View v){ // 확인을 누르면 이전 액티비티로 이동
        finish();
    }


    protected void onClick_adding(View v){ // 근무지 추가버튼을 누르면 SharedPreference에 저장하도록 구현한다.

        if(working_name.getText().toString().equals("") || pay.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "빈칸을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = getIntent();

            int working_min = Calculate_time(mHour_st, mMinute_st, mHour_et, mMinute_et);
            CheckBox[] week = new CheckBox[7];
            week[0] = (CheckBox)findViewById(R.id.sunday); // 추가 버튼을 누르는 순간 선택되어있는 체크박스의 데이터가 넘어가도록 하는 부분.
            week[1] = (CheckBox)findViewById(R.id.monday);
            week[2] = (CheckBox)findViewById(R.id.tuesday);
            week[3] = (CheckBox)findViewById(R.id.wednesday);
            week[4] = (CheckBox)findViewById(R.id.thursday);
            week[5] = (CheckBox)findViewById(R.id.friday);
            week[6] = (CheckBox)findViewById(R.id.saturday);

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


            Intent noti_intent = new Intent(getApplicationContext(), BroadcastN.class); // Broadcast를 실행하는 인텐트를 만들어준다.
            PendingIntent notiPending; // 임시 인텐트인 PendingIntent를 만들어준다.


            // 근무 몇분전에 알림을 할것인지 체크한 부분을 파악한다. 기본은 알림안함
            if(rano.isChecked()){
                Log.i(getClass().toString(), "알림안함");
                when_notify[0] = true; // 0번 : 알림안함

            }else if(ra5.isChecked()){
                when_notify[1] = true; // 1번 : 5분전 알림
                Log.i(getClass().toString(), "5분전 알림");
                noti_intent.putExtra("time", "5분전"); // 인텐트로 몇 분전에 알림을 하는지 보내준다. 이 값을 통해 노티피케이션의 내용을 바꿔준다.
                notiPending = PendingIntent.getBroadcast(getApplicationContext(), 5000, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT); // PendingIntent로 Broadcast를 실행하도록 만들어준다.
                alarmManager.set(AlarmManager.RTC_WAKEUP, noti_cal.getTimeInMillis()-5*60*1000, notiPending); // 알람매니저를 통해 출근시간 n분전에 알람이 울리도록 설정해준다.
            }else if(ra15.isChecked()){
                when_notify[2] = true;
                Log.i(getClass().toString(), "15분전 알림");
                noti_intent.putExtra("time", "15분전");
                notiPending = PendingIntent.getBroadcast(getApplicationContext(), 5000, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, noti_cal.getTimeInMillis()-15*60*1000, notiPending);

            }else if(ra30.isChecked()){
                when_notify[3] = true;
                Log.i(getClass().toString(), "30분전 알림");
                noti_intent.putExtra("time", "30분전");
                notiPending = PendingIntent.getBroadcast(getApplicationContext(), 5000, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, noti_cal.getTimeInMillis()-30*60*1000, notiPending);
            }






            //현재 입력받은 데이터들(근무지명, 시급, 근무장소좌표 등등..)을 workingPlace_info 객체에 담아준다.
            MainWorkingPlace_Info workingPlace_info = new MainWorkingPlace_Info(); // 임시적으로 데이터들을 담을 객체
            workingPlace_info.Place_name = working_name.getText().toString();
            workingPlace_info.Start_time_hour = String.valueOf(mHour_st);
            workingPlace_info.Start_time_min = String.valueOf(mMinute_st);
            workingPlace_info.End_time_hour = String.valueOf(mHour_et);
            workingPlace_info.End_time_min = String.valueOf(mMinute_et);
            workingPlace_info.Hour_pay = pay.getText().toString();
            workingPlace_info.Working_time = String.valueOf(working_min/60);
            workingPlace_info.Working_time_min = String.valueOf(working_min%60);
            workingPlace_info.Place_latitude = String.valueOf(latitude);
            workingPlace_info.Place_longitude = String.valueOf(longitude);
            workingPlace_info.week = week_checked;
            workingPlace_info.when_notify = when_notify;

            //정해 놓은 출근 시간 30분 전부터 GPS 탐색을 위한 서비스 호출부분
            Calendar gps_cal = Calendar.getInstance();
            gps_cal.set(Calendar.HOUR_OF_DAY, mHour_st);
            gps_cal.set(Calendar.MINUTE, mMinute_st);
            gps_cal.add(Calendar.MINUTE, -30);

            Intent gps_intent = new Intent(getApplicationContext(), Repeating_GPS_search.class);
            gps_intent.putExtra("Start_Hour", mHour_st);
            gps_intent.putExtra("Start_Min", mMinute_st);
            gps_intent.putExtra("PlaceId", workingPlace_info.IDNumber);
            gps_intent.putExtra("Saved_latitude", workingPlace_info.Place_latitude);
            gps_intent.putExtra("Saved_logitude", workingPlace_info.Place_longitude);
            gps_intent.putExtra("Search_place_name", workingPlace_info.Place_name);

            AlarmManager alarmManager1 =  (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 333, gps_intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager1.set(AlarmManager.RTC_WAKEUP, gps_cal.getTimeInMillis(), pendingIntent);

            /*Intent gps_intent = new Intent(getApplicationContext(), GPS_service.class);
            gps_intent.putExtra("PlaceId", workingPlace_info.IDNumber);
            gps_intent.putExtra("Saved_latitude", workingPlace_info.Place_latitude);
            gps_intent.putExtra("Saved_logitude", workingPlace_info.Place_longitude);
            AlarmManager alarmManager1 =  (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 333, gps_intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager1.setInexactRepeating(AlarmManager.RTC_WAKEUP, gps_cal.getTimeInMillis(), 1000*60*60*24, pendingIntent);*/


            //intent.putExtra("workingPlace_info", workingPlace_info);

            Save_Place_Info_toSharedPreference(workingPlace_info);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    private void Save_Place_Info_toSharedPreference(MainWorkingPlace_Info workingPlace_info) {
        Log.i(getClass().toString(), "데이터 저장");

        String json = gson.toJson(workingPlace_info); // 해당 객체를 json String으로 변환시켜준다.
        MainActivity.index_place.index.add(workingPlace_info.IDNumber); // 생성된 객체의 아이디값을 저장시켜준다.

        /*int Index = 0;
        if(mPrefs.getInt("Size", 0) == 0){ // 저장된 근무지가 하나도 없는 경우
            prefsEditor.putInt("Size", 1);

        }else {
            Index = mPrefs.getInt("Size", 0); //Place_Info라는 파일에 얼마만큼의 데이터가 담겨있는지 확인하기 위해 사이즈값을 가져온다.
            prefsEditor.putInt("Size", Index+1);

        }*/

        /*int Index = mPrefs.getInt("Size", 0); //Place_Info라는 파일에 얼마만큼의 데이터가 담겨있는지 확인하기 위해 사이즈값을 가져온다.
        prefsEditor.putInt("Size", Index+1);*/
        Log.i(getClass().toString(), "저장시 리스트크기 : " + MainActivity.index_place.index.size());


        /*if(MainActivity.index_place.index_place == null && mPrefs != null){ // 인덱스객체가 0이란 소리는, 새로생성되었거나 앱을 종료시켰다 다시켰을 경우.
            String json3 = mPrefs.getString("Index", null); // 기존에 저장되어있는 인덱스값을 불러와서
            MainActivity.index_place = gson.fromJson(json3, Index_place.class); // 다시 저장시켜준다.

        }*/

        for(int h = 0; h<MainActivity.index_place.index.size(); h++){
            Log.i("Index : ", String.format("INDEX %d %s", h, MainActivity.index_place.index.get(h)));
        }
        // 만약 사이즈가 3이라면 , 저장된 근무지가 3개란 소리이며, 여기에서 저장버튼을 누르게 되면 근무지 4번에 위치하도록 해야한다.
       // prefsEditor.putString(key+Index, json); // 아이디값을 키값으로 지정, 아이디값은 한번 정하면 바꾸지 않는다.

        JSONArray jsonArray = new JSONArray(); // 저장된 인덱스값을 배열형태로 바꾸기 위한 부분
        for(int i=0; i<MainActivity.index_place.index.size(); i++){
            jsonArray.put(MainActivity.index_place.index.get(i));
        }


        prefsEditor.putString(workingPlace_info.IDNumber, json);
        prefsEditor.putString("Index", jsonArray.toString());
        prefsEditor.commit(); // 해당 변화를 확정지어준다.

    }


    public static int Calculate_time(int Start_Hour, int Start_Min, int End_Hour, int End_Min){
        int Hour=0;
        int Min = 0;
        if(End_Hour >= Start_Hour){
            Hour = End_Hour - Start_Hour;
            Min = End_Min - Start_Min;
        }else {
            Hour = End_Hour + (24-Start_Hour);
            Min = End_Min - Start_Min;
        }
        return Hour*60 + Min;
    }


    /*protected void onClick_map(View v){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivityForResult(intent, 1);
    }*/

    //////////////////
    private void init(){
        Button btnMap = (Button) findViewById(R.id.map_in_add_button);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Place_add.this, MapsActivity.class);
                startActivityForResult(intent, 1); // 장소추가 클릭시 넘어가는 이벤트, 결과값으로 근무지의 좌표를 얻어온다.
            }
        });
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



    protected boolean isRouteDisplayed(){
        return false;
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Place_add.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Place_add.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(getClass().toString(), "OnMapReady");
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }


    @Override // 화면 회전시 데이터가 사라지지 않도록 유지해준다.
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }


    //위도와 경도값을 받아와서 주소로 변환해주는 메소드
    public static String getAddress(Context mContext, double lat, double lng){
        String nowAddress = "현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try{
            //if(geocoder != null){
                address = geocoder.getFromLocation(lat, lng, 1); // 해당 위도와 경도값의 주소를 리스트에 넣는다. 현재는 주소값이 1개가 들어간다.

                if(address != null && address.size() > 0)
                    nowAddress = address.get(0).getAddressLine(0);
            //}

        }catch (IOException e){
           // Toast.makeText(baseContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return nowAddress;
    }

}
