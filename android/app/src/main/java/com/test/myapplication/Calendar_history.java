package com.test.myapplication;

import android.annotation.SuppressLint;
import android.app.ActionBar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.widget.ImageButton;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class Calendar_history extends AppCompatActivity {
    ArrayList day_number, day_pay_vet, day_button; //
    int firstDay; // 매달 1일이 무슨 요일에 시작하는지 담기 위한 변수

    int totDays; // 한달이 몇일인지 정하는 변수, 28~31일까지
    int iYear; // 표시할 연도를 저장할 변수
    int iMonth; // 표시할 월을 저장할 변수

    ArrayList<Day_Info> Cal_dayinfo = new ArrayList<>(); // 이전 액티비티로 넘겨받은 day_info 클래스를 담을 객체변수
    MainWorkingPlace_Info Place_info = new MainWorkingPlace_Info(); // 이전 액티비티로 넘겨받은 근무지에 대한 정보를 담을 객체 변수

    SharedPreferences nPrefs, mPrefs;
    SharedPreferences.Editor prefsEditor, Editor;
    Index_day_info day_index = new Index_day_info();
    private Gson gson;
    private String Place_id;
    int Place_position;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_history);

        //SharedPreference 관련 코드
        mPrefs = getSharedPreferences("PlaceInfo", MODE_PRIVATE); //현재 근무지의 상태정보는 PlaceInfo라는 xml파일에 SharedPreference를 이용해서 저장해준다.
        prefsEditor = mPrefs.edit(); // sharedPreference를 수정하기 위한 에디터를 연결해준다.
        gson = new Gson(); // 리스트를 통쨰로 저장하기 위한 gson 객체를 생성해준다.

        Place_id = getIntent().getStringExtra("Place_id");
        nPrefs = getSharedPreferences(Place_id, MODE_PRIVATE);
        Editor  = nPrefs.edit();

        Log.i(getClass().toString(), "받아온 아이템 아이디값 : " + Place_id);

        // 이전 액티비티로 넘겨받은 데이터를 저장해주는 부분

        Place_info = get_Place_data();
        Place_position = getIntent().getIntExtra("Place_postion", 0);


        //초기 객체 생성부분
        day_number = new ArrayList();
        day_pay_vet = new ArrayList();
        LinearLayout Month = (LinearLayout) findViewById( R.id.table ); // xml상에서 달력을 표시할 부분의 아이디값을 얻어온다.

        //액션바 설정 부분, 뒤로가기 버튼을 만들어준다.
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        //너비관련 정보? 선언 부분
        LinearLayout.LayoutParams params_width = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT);
        //높이관련
        LinearLayout.LayoutParams params_height = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        params_width.weight = 1.0f; // 너비가중치를 설정해준다. 1로 해주면 각 뷰들의 너비가 같은 비율을 가지게 된다.
        params_width.width = 0; // 너비를 0으로 해줘야 가중치 적용이 제대로 된다.
        params_width.bottomMargin = 1; // 마진을 준 이유는, 선을 나타내기 위함.
        params_width.topMargin = 1;
        params_width.leftMargin = 1;
        params_width.rightMargin = 1;
        params_height.weight = 1.0f;


        //레이아웃 생성 부분 6*7의 표를 만드는 형식
        for(int i = 0 ; i < 6 ; i++ ){ // 한달은 최대 6주까지 가능하므로, 6주치를 생성해준다.
            LinearLayout week_line = new LinearLayout( this ); // LinearLayout으로 한 주를 나타난다.
            week_line.setLayoutParams(params_height); //

            for(int j = 0 ; j < 7 ; j++ ){ // 요일을 설정하는 부분, 일주일은 7일이므로 7개를 만들어 준다.
                final LinearLayout oneday = new LinearLayout(this); // 하루를 Vertical타입의 LinearLayout으로 만들어준다.
                TextView day = new TextView( this ); // 하루의 LinearLayout안에 들어갈 텍스트뷰, 날짜를 나타낸다.
                TextView day_pay = new TextView(this); // 그 날의 일급을 나타낸다.
                oneday.setOrientation(LinearLayout.VERTICAL); //
                oneday.setBackgroundColor(Color.WHITE);
                oneday.setLayoutParams(params_width);
                oneday.setGravity(Gravity.END);


                if( j == 0 )
                    day.setTextColor( Color.RED ); // 일요일은 빨간색
                else if( j == 6 )
                    day.setTextColor( Color.parseColor("#2f2fff") );//   토요일을 파란색
                else  // 그 외에는 검정색
                    day.setTextColor( Color.BLACK );

                day_pay.setTextColor(Color.parseColor("#ff7a00"));
                day.setGravity(Gravity.TOP | Gravity.START);
                day_pay.setGravity(Gravity.BOTTOM | Gravity.CENTER);
                day.setLayoutParams(params_height);
                day_pay.setLayoutParams(params_height);


                //레이아웃 생성을 위해 자식 뷰를 부모뷰에 넣어준다.
                oneday.addView(day); // oneday라는 리니어레이아웃에 day와day_pay라는 2개의 텍스트뷰 자식을 넣어준다.
                oneday.addView(day_pay);
                week_line.addView( oneday ); // week_line이라는 리니어레이아웃에 oneday라는 자식뷰를 넣어준다.


                day_number.add( day ); // 여기에 들어간 텍스트뷰들은 주소값이 들어가는 듯, 그래서 get을 통해 텍스트뷰에 접근이 가능
                day_pay_vet.add(day_pay);
            }
            Month.addView( week_line );
        }



        ImageButton btn = (ImageButton) findViewById( R.id.pre ); //-> pre 버튼 설정
        btn.setOnClickListener( new ImageButton.OnClickListener(){

            public void onClick(View v) {

                iMonth--;
                if(iMonth == 0){
                    iMonth = 12;
                    iYear--;
                }
                setCalendar( iYear, iMonth );

            }
        });

        btn = (ImageButton) findViewById( R.id.next );// ->next 버튼 설정
        btn.setOnClickListener( new ImageButton.OnClickListener(){

            public void onClick(View v) {

                iMonth++;
                if(iMonth == 13){
                    iMonth = 1;
                    iYear++;
                }
                setCalendar( iYear, iMonth );

            }
        });

        //캘린더 날짜 터치시 동작을 처리하는 부분
        Month.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int day_x = v.getWidth()/7; // 한칸의 너비를 정하는 부분, 달력의 너비를 7로 나누면 1칸의 너비
                int day_y = v.getHeight()/6; // 한칸의 높이를 정하는 부분, 달력의 높이를 6으로 나누면 1칸의 높이
                int codi_x = (int) (event.getX()/day_x); // 터치시 발생한 x좌표를 처리한다. x좌표는 너비값에 대응한다.
                int codi_y = (int) (event.getY()/day_y); // 터치시 발생한 y좌표를 처리한다. y좌표는 높이값에 대응한다.

                int xy = codi_y*7 + codi_x; // 해당 캘린더의 인덱스번호를 알아내기 위한 부분.

                /*
                * 예를 들어 가로 700, 세로 600짜리 달력이 있으면 달력 1칸에 해당하는 크기는 100*100이 된다.
                * 터치 입력시의 좌표값이 (150, 120)이라면 해당 좌표는 (1, 1)에 해당(인덱스 번호 0번 부터)
                * (0, 0) ~ (0, 6)까지 1개의 줄에 7번까지 있으므로 1차원 배열이라 생각한다면 (1, 1)에 해당하는 인덱스번호는
                * 8번이다.
                * */

                if(((TextView)day_number.get(xy)).getText().toString().equals("")){
                    // 만약 터치한 부분에 날짜 데이터가 없으면 아무처리 안함.
                }else {
                    if(   ((TextView)day_pay_vet.get(xy)).getText().toString().equals("")){
                        // 만약 터치한 부분에 일급에 대한 데이터가 없으면 아무처리 안함
                    }else { // 터치한 부분에 데이터가 있으면, 새로운 팝업을 띄워준다.
                        Intent intent = new Intent(getApplicationContext(), Popup_day_info.class);
                        intent.putExtra("day", ((TextView) day_number.get(xy)).getText().toString());
                        intent.putExtra("month", iMonth);
                        intent.putExtra("year", iYear);
                        intent.putExtra("day_list", Cal_dayinfo);
                        startActivity(intent);
                    }
                }
                return false;
            }
        });
    }

    private MainWorkingPlace_Info get_Place_data() {

        String json = mPrefs.getString(Place_id, null);
        return gson.fromJson(json, MainWorkingPlace_Info.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        day_index.index.clear();
        get_day_info();
        init();
    }

    private void get_day_info() {
        if (nPrefs == null) {

        } else {
            if (day_index.index.size() == 0) {
                Log.i(getClass().toString(), "리스트 크기가 0인 경우 : ");

                String index_son = nPrefs.getString("Index", null); // 기존에 저장되어있는 인덱스값을 불러와서

                if (index_son == null) {
                    Log.i("근무기록", "저장된 인덱스가 없는 경우");

                } else {
                    Log.i("근무기록", "저장된 인덱스가 있는 경우");

                    try {
                        JSONArray jsonArray = new JSONArray(index_son);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String index_data = jsonArray.optString(i);
                            day_index.index.add(index_data);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    index_sort(); // 불러온 인덱스를 날짜순으로 정렬해준다. 정렬된 인덱스를 다시 저장해준다.
                    Cal_dayinfo.clear();
                    for (int i = 0; i < day_index.index.size(); i++) { // 저장된 데이터 전부를 리스트에 넣어주는 작업을 한다.
                        String json = nPrefs.getString(day_index.index.get(i), null);
                        Day_Info tmp = gson.fromJson(json, Day_Info.class);
                        Cal_dayinfo.add(tmp);
                    }
                }

            } else {
                Log.i(getClass().toString(), "리스트 크기가 0이 아닌 경우 : ");


                Log.i("근무기록", "기록이 추가되는 경우");
                index_sort(); // 불러온 인덱스를 날짜순으로 정렬해준다. 정렬된 인덱스를 다시 저장해준다.
                Cal_dayinfo.clear();
                for (int i = 0; i < day_index.index.size(); i++) { // 저장된 데이터 전부를 리스트에 넣어주는 작업을 한다.
                    String json = nPrefs.getString(day_index.index.get(i), null);
                    Day_Info tmp = gson.fromJson(json, Day_Info.class);
                    Cal_dayinfo.add(tmp);
                }

            }

        }
    }
        private void index_sort() { // 인덱스를 빠른 날짜순으로 정렬해주는 메소드
            Log.i("근무기록 : ", "정렬을 위한 인덱스 크기 " + day_index.index.size());

            if(day_index.index.size() > 1) { // 크기가 2이상인 경우에만 정렬
                Collections.sort(day_index.index, new Comparator<String>() { // 정렬을 해준뒤 정렬된 데이터를 다시 저장한다.
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });

                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < day_index.index.size(); i++) {
                    jsonArray.put(day_index.index.get(i));
                }

                Editor.putString("Index", jsonArray.toString());
                Editor.commit();
                Log.i("근무기록 : ", "정렬된 데이터 " + jsonArray.toString());
            }

        }


    private void setCalendar( int year, int month ){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year); // cal에 연도, 월, 일을 넣어준 후 그날이 무슨 요일인지 정한다.
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        firstDay = cal.get(Calendar.DAY_OF_WEEK)-1; // 1 : 일, 2: 월 ~ 7: 토, 첫 주의 1일이 무슨 요일인지 정하는 부분 그 부분부터 채워나가기 시작함

        totDays = 31; // 기본 한달의 수는 31일로 정해 놓고 시작.
        for( int i = 29 ; i <= 32 ; i++ ){
            //date.setDate( i );
            cal.set(Calendar.DAY_OF_MONTH, i); // 29, 30, 31, 32까지의 숫자를 넣어서
            if( cal.get(Calendar.DAY_OF_MONTH) == 1 ){    // ->달이 30일인지, 31일인지 설정
                totDays = i - 1;
                break;
            }
        }

        Log.i("mylog", firstDay + " " + totDays );


        TextView tvToday = (TextView)findViewById( R.id.today ); //->년,월,날짜설정
        tvToday.setText(iYear + "년 " + iMonth + "월");



        for(int i = 0; i < day_number.size() ; i++ ){ // 초기화 해주는 부분
            ((TextView) day_number.get( i )).setText(""); // 일 초기화
            ((TextView) day_pay_vet.get(i)).setText(""); // 일급 초기화
        }

        int iDate = 1;
        for(int i = firstDay, k=0; i<firstDay+totDays; i++, k++){
            int tmp = 0;
            ((TextView) day_number.get( i )).setText( String.valueOf( iDate++ ) );

            for(int j=0; j<Cal_dayinfo.size(); j++){
                if(k == Cal_dayinfo.get(j).day_of_month-1 && iMonth == Cal_dayinfo.get(j).month && iYear == Cal_dayinfo.get(j).year){
                    tmp += Cal_dayinfo.get(j).Daily_total_pay;
                    ((TextView)day_pay_vet.get(i)).setText(String.valueOf(tmp) + "원");
                }
            }
        }

    }


    public void init(){
        setTitle(Place_info.Place_name);

        // 초기 데이터를 세팅해주는 부분, 이 부분은 현재 연도와 월을 저장해준다.
        Calendar cal = Calendar.getInstance(); // 캘린더 객체를 하나 만든다. 캘린더 클래스를 이용하면 현재 연,월,일을 알아낼 수 있다. 직접적인 생성은 안되고 getInstance를 통해 생성해야한다.
        iYear = cal.get(Calendar.YEAR); // 현재의 연도를 저장한다.
        iMonth = cal.get(Calendar.MONTH)+1; // 현재의 월을 저장한다. 월의 시작은 0부터이므로 +1을 해줘야 현재월이 나온다.
        setCalendar( iYear, iMonth );

    }

    public void onClick_cal_month(View v){

        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                /*Cal_dayinfo.add(new Day_Info(data.getIntExtra("year", 2018), data.getIntExtra("month", 7), data.getIntExtra("day_of_month", 23),
                        data.getIntExtra("STH", 0), data.getIntExtra("STM", 0), data.getIntExtra("ETH", 0), data.getIntExtra("ETM", 0),
                        data.getIntExtra("pay", 0), data.getStringExtra("day_of_week"),
                        data.getByteArrayExtra("image"), data.getStringExtra("diary"),
                        Cal_dayinfo.size()));*/

                //setCalendar(iYear, iMonth);
            }else if(requestCode == 100){
                Place_info = get_Place_data();
                setTitle(Place_info.Place_name);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu2, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 뒤로가기 버튼을 눌렀을시의 동작
                Intent intent = getIntent();
                intent.putExtra("day_info", Cal_dayinfo);
                setResult(RESULT_OK, intent);
                finish();
                break;

            case R.id.action_add:
                Intent addint = new Intent(getApplicationContext(), Inputdata.class);
                addint.putExtra("Place id", Place_id);
                startActivityForResult(addint, 1);
                break;

            case R.id.place_modi_button2:
                Intent modiIntent = new Intent(getApplicationContext(), Place_modify.class);
                modiIntent.putExtra("Place info_id", Place_id);
                modiIntent.putExtra("Place info_position", Place_position);
                Log.i(getClass().toString(), "수정하는 곳에 보내는 리스트 포지션 : " + Place_id);
                startActivityForResult(modiIntent, 100);
                break;

            case R.id.place_delete_button2:
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(Calendar_history.this);
                alert_confirm.setMessage("정말로 근무지를 삭제하시겠습니까?\n삭제후 복구할 수 없습니다.").setCancelable(false).setPositiveButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // '취소'
                                return;
                            }
                        }).setNegativeButton("삭제",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // '삭제'
                                Intent rel_intent = getIntent();
                                rel_intent.putExtra("delete_position", Place_position);
                                setResult(3, rel_intent); // 삭제코드 3
                                finish();
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();
                break;
        }

        return true;
    }


}
