package com.test.myapplication;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.hotspot2.omadm.PpsMoParser;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


/*
* 아이템 밀어서 삭제, 프레임 레이아웃 참고 : https://www.androidhive.info/2017/09/android-recyclerview-swipe-delete-undo-using-itemtouchhelper/
*
*
*
* */

public class Working_history extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    RecyclerView mRecyclerView; // 리사이클러뷰 선언
    RecyclerView.LayoutManager mLayoutManager; // 레이아웃 매니저 선언
    ArrayList<Day_Info> day_info = new ArrayList<>(); // 근무하는 요일에 대한 정보를 담는 객체, 요일별로 시간이 다를 수 있으므로 요일별의 객체를 만들어준다.
    ArrayList<Day_Info> day_info_montly = new ArrayList<>(); // 월마다의 정보를 담을 임시 객체
    Index_day_info day_index = new Index_day_info();
    private MainWorkingPlace_Info Place_info; // 이 클래스 객체는 해당 근무에 대한 전체적인 정보를 담고 있다.
    private GestureDetector gestureDetector;
    TextView his_month_pay, his_month_time, Month;
    int his_month_total_time=0;
    int his_month_total_pay=0;
    int hYear;
    int hMonth;
    History_Adapter history_adapter = new History_Adapter(day_info_montly);
    LinearLayout linearLayout;
    TextView empty_history;
    private static final String TAG = "WorkingHistory";


    SharedPreferences mPrefs, nPrefs;
    SharedPreferences.Editor prefsEditor, Editor;
    private Gson gson;
    private String key = "PlaceInfoData";

    String Place_id;
    int Place_position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_working_history);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true); // 액션바에 뒤로가기 버튼 삽입부분
        actionBar.setHomeButtonEnabled(true);



        //TextView ar = (TextView)findViewById(R.id.autorecord);
        his_month_pay = (TextView)findViewById(R.id.his_month_pay);
        his_month_time = (TextView)findViewById(R.id.his_month_time);
        Month = (TextView)findViewById(R.id.Month);
        empty_history = findViewById(R.id.empty_history);

        //SharedPreference 관련 코드
        mPrefs = getSharedPreferences("PlaceInfo", MODE_PRIVATE); //현재 근무지의 상태정보는 PlaceInfo라는 xml파일에 SharedPreference를 이용해서 저장해준다.
        prefsEditor = mPrefs.edit(); // sharedPreference를 수정하기 위한 에디터를 연결해준다.
        gson = new Gson(); // 리스트를 통쨰로 저장하기 위한 gson 객체를 생성해준다.

        Place_id = getIntent().getStringExtra("Place id");
        nPrefs = getSharedPreferences(Place_id, MODE_PRIVATE);
        Editor  = nPrefs.edit();

        Place_position = getIntent().getIntExtra("Place_postion", 0);
        Log.i(getClass().toString(), "받아온 아이템 아이디값 : " + Place_id);

        //Place_info = (MainWorkingPlace_Info)getIntent().getSerializableExtra("Place info");
        Place_info = get_Place_data();
        /*String result = "";
        for(int i=0; i<7; i++){
            result += Place_info.week[i];
        }

        ar.setText("매 주 [" + result + "] 요일, " +  Place_info.Start_time_hour +"시"+ Place_info.Start_time_min+"분부터 " +
        Place_info.End_time_hour +"시" + Place_info.End_time_min + "분까지 자동으로 근무 시간이 기록되고 있습니다.");*/

        mRecyclerView = findViewById(R.id.his_recycler); // xml 상의 recyclerview를 표시해줄 레이아웃의 아이디값을 연결.
        mRecyclerView.setHasFixedSize(true); // ???
        mLayoutManager = new LinearLayoutManager(this); // 레이아웃 매니저를 생성해준다.
        mRecyclerView.setLayoutManager(mLayoutManager); // 레이아웃 매니저와 리사이클러뷰를 연결해준다.
        mRecyclerView.addOnItemTouchListener(onItemTouchListener); // 아이템을 터치했을때의 동작을 구현? 선언? 정해준다.


        //삭제 스와이프 동작 관련
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);


        //제스쳐 디텍터 : 사용자의 동작과 관련된 부분인듯
        gestureDetector = new GestureDetector(getApplicationContext(),new GestureDetector.SimpleOnGestureListener() {

            //누르고 뗄 때 한번만 인식하도록 하기위해서
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        //mRecyclerView.addOnItemTouchListener(onItemTouchListener); // 왜 두개나 적었을까


        init();
        if(savedInstanceState != null){ // 생명주기와 관련된 부분, 만약 액티비티가 파괴되서 재생성될때 저장된 데이터가 있으면 불러온다.
            ArrayList<Day_Info> data = (ArrayList<Day_Info>) savedInstanceState.getSerializable("dayinfo");
            day_info = data;
            Display_recyclerView();
        }

        ImageButton btn = (ImageButton) findViewById( R.id.his_pre ); //-> pre 버튼 설정, 이 버튼을 누르면 지난달로 이동한다.
        btn.setOnClickListener( new ImageButton.OnClickListener(){

            public void onClick(View v) {

            hMonth--;
            if(hMonth == 0){ // 0월이 되면 지난해의 12월로 넘어가도록 설정하는 부분
                hMonth = 12;
                hYear--;
            }
            Display_recyclerView();
            }
        });

        btn = (ImageButton) findViewById( R.id.his_next );// ->next 버튼 설정, 이 버튼을 누르면 다음달로 이동한다.
        btn.setOnClickListener( new ImageButton.OnClickListener(){ // 이미지 버튼을 클릭했을때의 동작을 구현해준다.

            public void onClick(View v) {

                hMonth++;
                if(hMonth == 13){ // 13월이 되면 내년의 1월이 되도록 설정하는 부분
                    hMonth = 1;
                    hYear++;
                }
            Display_recyclerView();

            }
        });



    }

    private MainWorkingPlace_Info get_Place_data() {

        String json = mPrefs.getString(Place_id, null);
        return gson.fromJson(json, MainWorkingPlace_Info.class);
    }

    public void init(){
        hYear = Calendar.getInstance().get(Calendar.YEAR); //->년도
        hMonth = Calendar.getInstance().get(Calendar.MONTH)+1; //->달 수

       /* if(Place_info.save_day_info != null)
            day_info = Place_info.save_day_info;*/

        setTitle(Place_info.Place_name);

        Display_recyclerView();
    }

    //리사이클러 뷰의 한 라인을 클릭시 동작하는 이벤트
    RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerView.OnItemTouchListener() {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            //손으로 터치한 곳의 좌표를 토대로 해당 Item의 View를 가져옴
            View childView = rv.findChildViewUnder(e.getX(),e.getY());

            //터치한 곳의 View가 RecyclerView 안의 아이템이고 그 아이템의 View가 null이 아니라
            //정확한 Item의 View를 가져왔고, gestureDetector에서 한번만 누르면 true를 넘기게 구현했으니
            //한번만 눌려서 그 값이 true가 넘어왔다면
            if(childView != null && gestureDetector.onTouchEvent(e)){

                //현재 터치된 곳의 position을 가져오고
                int currentPosition = rv.getChildAdapterPosition(childView);
                //해당 위치의 Data를 가져옴
                Day_Info daily_info = day_info_montly.get(currentPosition);
                Intent intent = new Intent(getApplicationContext(), History_popup.class);
                //ImageView imve = findViewById(R.id.preview);
               // ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, imve, getString(R.string.profile_element));
                //day_info_montly.get(currentPosition).total_positon = currentPosition;
                intent.putExtra("day_info_id", daily_info.Day_Id);
                intent.putExtra("Place_id", Place_id);
                startActivityForResult(intent, 2);
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    public void onClick_Month(View v){ // 월 버튼을 누르면 요번달로 돌아가도록 설정

        init();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){ // 결과가 잘 나왔을 경우
            if(requestCode == 1){ // 받은 코드가 1이라면, 직접입력 버튼을 눌렀을때의 결과
               /* day_info.add(new Day_Info(data.getIntExtra("year", 2018), data.getIntExtra("month", 7), data.getIntExtra("day_of_month", 23),
                        data.getIntExtra("STH", 0), data.getIntExtra("STM", 0), data.getIntExtra("ETH", 0), data.getIntExtra("ETM", 0),
                        data.getIntExtra("pay", 0), data.getStringExtra("day_of_week"),
                        data.getByteArrayExtra("image"), data.getStringExtra("diary"),
                        day_info.size())); // 근무기록을 추가할때마다 포지션을 저장해주는 부분, 각 기록들은 동일한 포지션값을 가지지 않는다.*/


               // set_Index_day_info();


            } else if(requestCode == 2){ // 요일별 근무 기록을 눌렀을때의 결과
                /*day_info.set(data.getIntExtra("postion", 0), new Day_Info(data.getIntExtra("year", 2018), data.getIntExtra("month", 7), data.getIntExtra("day_of_month", 23),
                        data.getIntExtra("STH", 0), data.getIntExtra("STM", 0), data.getIntExtra("ETH", 0), data.getIntExtra("ETM", 0),
                        data.getIntExtra("pay", 0), data.getStringExtra("day_of_week"),
                        data.getByteArrayExtra("image"), data.getStringExtra("diary"),
                        data.getIntExtra("postion", 0)));*/
                //Day_Info tmp_day = (Day_Info) data.getSerializableExtra("day_modi_info");
                //day_info.set(tmp_day.total_positon, tmp_day);

                //Display_recyclerView();


            }else if(requestCode == 3){
                Place_info = get_Place_data();
                setTitle(Place_info.Place_name);

                // 캘린더 버튼을 누르고 돌아온 경우
                //day_info = (ArrayList<Day_Info>) data.getSerializableExtra("day_info");
                //Display_recyclerView();

            }else if(requestCode == 100){ // 근무지 수정버튼을 누른경우
                Place_info = get_Place_data();
                setTitle(Place_info.Place_name);
            }
        }else if(resultCode == 3){ // 캘린더 액티비티에서 근무지삭제 버튼을 눌렀을때 처리하는 부분
            Intent del_intent = getIntent();
            del_intent.putExtra("delete_position", Place_id);
            setResult(3, del_intent);
            finish();
        }


    }
    private void set_Index_day_info() { // 저장된 인덱스값을 불러오는 메소드
        if (nPrefs == null) {

        }else {
            if(day_index.index.size()==0){
                Log.i(getClass().toString(), "리스트 크기가 0인 경우 : " );

                String index_son = nPrefs.getString("Index", null); // 기존에 저장되어있는 인덱스값을 불러와서

                if(index_son == null){
                    Log.i("근무기록", "저장된 인덱스가 없는 경우");

                }else {
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
                    day_info.clear();
                    for (int i = 0; i < day_index.index.size(); i++) { // 저장된 데이터 전부를 리스트에 넣어주는 작업을 한다.
                        String json = nPrefs.getString(day_index.index.get(i), null);
                        Day_Info tmp = gson.fromJson(json, Day_Info.class);
                        day_info.add(tmp);
                    }
                }

            }else {
                Log.i(getClass().toString(), "리스트 크기가 0이 아닌 경우 : ");


                    Log.i("근무기록", "기록이 추가되는 경우");
                    index_sort(); // 불러온 인덱스를 날짜순으로 정렬해준다. 정렬된 인덱스를 다시 저장해준다.
                    day_info.clear();
                    for (int i = 0; i < day_index.index.size(); i++) { // 저장된 데이터 전부를 리스트에 넣어주는 작업을 한다.
                        String json = nPrefs.getString(day_index.index.get(i), null);
                        Day_Info tmp = gson.fromJson(json, Day_Info.class);
                        day_info.add(tmp);
                    }

            }
            Display_recyclerView();

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

    private void calcul_montly_pay_time(){
        int tmp=0;
        int tmp2=0;
        for(int i=0; i<day_info.size(); i++){
            if(day_info.get(i).year == hYear && day_info.get(i).month == hMonth) {
                tmp += day_info.get(i).Daily_total_time_min;
                tmp2 += day_info.get(i).Daily_total_pay;
            }
        }
        his_month_total_time = tmp;
        his_month_total_pay = tmp2;
    }


    public void Display_recyclerView(){ // 리사이클러뷰를 표시하기 위한 부분, 기록이 추가될때마다 호출한다.

        // 이 부분은 기록이 추가될때마다 업데이트를 해줘야 하는 부분
        Month.setText(String.format("%d년 %d월", hYear, hMonth));
        calcul_montly_pay_time();
        his_month_pay.setText(String.format("이번 달 급여 : %d원", his_month_total_pay));
        his_month_time.setText(String.format("이번 달 누적 시간 : %d시간 %d분", his_month_total_time/60, his_month_total_time%60));


        //정렬을 위한 부분, 매번 기록이 추가될때마다 정렬은 한다. 왜냐하면 사용자 마음대로 날짜를 추가할 수 있기 때문에
        //어떤 날짜를 추가하더라도 정렬하기 위해, 연도 -> 달 -> 일 순으로 오름차순 정렬한다.
        /*Collections.sort(day_info, new Comparator<Day_Info>() { // 연도순으로 정렬을 하기 위한 부분
            @Override
            public int compare(Day_Info o1, Day_Info o2) {
                if(o1.year > o2.year){
                    return 1;
                }else if(o1.year < o2.year){
                    return -1;
                }else {
                    return 0;
                }
            }
        });

        Collections.sort(day_info, new Comparator<Day_Info>() { // 월순으로 정렬을 하기 위한 부분
            @Override
            public int compare(Day_Info o1, Day_Info o2) {
                if(o1.month > o2.month){
                    return 1;
                }else if(o1.month < o2.month){
                    return -1;
                }else {
                    return 0;
                }
            }
        });

        Collections.sort(day_info, new Comparator<Day_Info>() { // 날짜순으로 정렬을 하기 위한 부분
            @Override
            public int compare(Day_Info o1, Day_Info o2) {
                if(o1.day_of_month > o2.day_of_month){
                    return 1;
                }else if(o1.day_of_month < o2.day_of_month){
                    return -1;
                }else {
                    return 0;
                }
            }
        });*/

        /*for(int i=0; i< day_info.size(); i++){
            day_info.get(i).total_positon = i; // 정렬된 리스트의 포지션값을 다시 정해준다. 제일 과거의 날짜부터 미래의 날짜 순으로 포지션을 정한다.
        }*/

        day_info_montly.clear();
        String day_form = String.format("%04d%02d", hYear, hMonth);
        Log.i("WorkingHistory", "비교할 날짜 : " + day_form);

        for(int i=0; i<day_index.index.size(); i++){ // 해당 월의 근무기록만 보여주기 위해 임시객체에 데이터를 담는다.
            String day_index_compare = day_index.index.get(i).substring(0, 6);
            Log.i("WorkingHistory", "비교될 날짜 : " + day_index_compare);
            if(day_index_compare.equals(day_form)){
                day_info_montly.add(day_info.get(i));
            }
        }

        if(day_info_montly.size() == 0){
            empty_history.setText("아직 기록이 없습니다.\n상단의 + 버튼을 통해 직접 입력할수 있습니다.");
            empty_history.setGravity(Gravity.CENTER);
        }else {
            empty_history.setText("");

        }

        mRecyclerView.setAdapter(history_adapter);
        //history_adapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<Day_Info> data = day_info;
        outState.putSerializable("dayinfo", data);

    }


    //기록삭제
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, final int position) {
        Log.i("근무기록", "onSwiped 호출");
        final Day_Info deletedItem = day_info_montly.get(viewHolder.getAdapterPosition());

        if(direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT ) {
            final int delete_index = removeItem(viewHolder.getAdapterPosition());

            Snackbar.make(getWindow().getDecorView().getRootView(), "혹시 잘못 지우셨나요?", Snackbar.LENGTH_LONG).setActionTextColor(Color.parseColor("#FF0000"))
                    .setAction("복구", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restoreItem(deletedItem, delete_index);
                }
            }).show();
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public int removeItem(int position) {
        Log.i("근무기록", "기록삭제 포지션 : " + position);
        String delete_history_id = day_info_montly.get(position).Day_Id;

        int delete_index=0;
        for(; delete_index<day_index.index.size(); delete_index++){
            if(day_index.index.get(delete_index).equals(delete_history_id)){
                day_index.index.remove(delete_index);
                day_info.remove(delete_index);
                break;
            }
        }

        JSONArray jsonArray = new JSONArray(); // JsonArray를 만들어준 뒤, 현재 남아있는 아이디값을 다시 저장
        for(int j=0; j<day_index.index.size(); j++){
            jsonArray.put(day_index.index.get(j));
        }
        Editor.putString("Index", jsonArray.toString()); // 저장된 인덱스값 교체
        Editor.remove(delete_history_id); // 저장된 데이터상에서 해당 키값을 가진 데이터 삭제
        Editor.commit();
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        history_adapter.notifyItemRemoved(position);
        Display_recyclerView();
        return delete_index;
    }

    public void restoreItem(Day_Info item, int position) {
        day_index.index.add(position, item.Day_Id);
        day_info.add(position, item);

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < day_index.index.size(); i++) {
            jsonArray.put(day_index.index.get(i));
        }

        String json = gson.toJson(item);

        Editor.putString(item.Day_Id, json);
        Editor.putString("Index", jsonArray.toString());
        Editor.commit();


        // notify item added by position
        history_adapter.notifyItemInserted(position);
        Display_recyclerView();
    }


    //메뉴


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = getIntent();
                //Place_info.save_day_info = day_info;
                //intent.putExtra("result_place_data", Place_info);
                Log.i(getClass().toString(), String.valueOf(Place_id + "번째 포지션 보냄"));
                intent.putExtra("changed_data_id", Place_id);
                intent.putExtra("changed_data_postion", Place_position);
                setResult(RESULT_OK, intent);
                Intent in = new Intent(getApplicationContext(), MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(in);
                finish();
                //overridePendingTransition(R.anim.not_move_activity, R.anim.rightout_activity);
                break;
            case R.id.action_add2: // 기록추가 버튼
                Intent addint = new Intent(getApplicationContext(), Inputdata.class);
                addint.putExtra("Place id", Place_id);
                startActivityForResult(addint, 1);
                break;
            case R.id.action_calendar: // 캘린더 버튼
                Intent calIntent = new Intent(getApplicationContext(), Calendar_history.class);
                calIntent.putExtra("Place_id", Place_id);
                calIntent.putExtra("Place_postion", Place_position);
                startActivityForResult(calIntent, 3);
                break;
            case R.id.place_modi_button: // 근무지 수정 버튼
                Intent modiIntent = new Intent(getApplicationContext(), Place_modify.class);
                modiIntent.putExtra("Place info_id", Place_id);
                modiIntent.putExtra("Place info_position", Place_position);
                Log.i(getClass().toString(), "수정하는 곳에 보내는 리스트 포지션 : " + Place_id);
                startActivityForResult(modiIntent, 100);

                break;
            case R.id.place_delete_button: // 근무지 삭제 버튼
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(Working_history.this);
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
                                Intent del_intent = getIntent();
                                del_intent.putExtra("delete_position", Place_position);
                                setResult(3, del_intent);
                                finish();
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();
                break;
        }

        return true;
    }




    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {

        super.onStop();
        Log.i(TAG, "onStop");
        //onSaveData();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {

        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResum");
        day_index.index.clear();
        set_Index_day_info();

    }

    @Override
    protected void onRestart() {

        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    public void onClick_month_info(View view) {
        if(day_info_montly.size() !=0) {
            Intent intent = new Intent(getApplicationContext(), Month_statistic.class);
            intent.putExtra("Place_id", Place_id);
            String YearMonth = String.format("%04d%02d", hYear, hMonth);
            intent.putExtra("YearMonth", YearMonth);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() { // 뒤로가기 버튼을 누를때의 이벤트 처리
        super.onBackPressed();
        Intent intent = getIntent();
        //Place_info.save_day_info = day_info;
        //intent.putExtra("result_place_data", Place_info);
        Log.i(getClass().toString(), String.valueOf(Place_id + "번째 포지션 보냄"));
        intent.putExtra("changed_data_id", Place_id);
        intent.putExtra("changed_data_postion", Place_position);
        setResult(RESULT_OK, intent);
        Intent in = new Intent(getApplicationContext(), MainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(in);
        finish();

    }
}
