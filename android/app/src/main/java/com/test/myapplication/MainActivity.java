package com.test.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    RecyclerView mRecyclerView; // 리사이클러뷰 선언
    RecyclerView.LayoutManager mLayoutManager; // 레이아웃 매니저 선언
    private GestureDetector gestureDetector; // 다양한 터치 이벤트를 처리하는 클래스, 길게누르기, 두번누르기 등등..

    private BackPressCloseHandler backPressCloseHandler; // 뒤로가기 버튼을 눌렀을때 핸들러
    static ArrayList<MainWorkingPlace_Info> workingPlaceInfo = new ArrayList<>(); // 근무장소에 관련된 배열
    static Index_place index_place = new Index_place();
    private static final String TAG = "MainActivtiy";
    TextView empty_text;


    MainAdapter mainAdapter = new MainAdapter(workingPlaceInfo); // 리사이클러뷰를 처리할 어댑터를 생성해준다. 이 어댑터의 데이터는 workingPlaceInfo로 구성된다.


    SharedPreferences mPrefs;
    SharedPreferences.Editor prefsEditor;
    private Gson gson;
    private String key = "PlaceInfoData";

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 액티비티가 시작되면 제일 먼저 실행되는 부분
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");
        setTitle("자동 출퇴근 기록 시급계산기"); // 타이틀명을 정해준다.
        empty_text = (TextView)findViewById(R.id.empty_Text);



        //SharedPreference 관련 코드
        mPrefs = getSharedPreferences("PlaceInfo", MODE_PRIVATE); //현재 근무지의 상태정보는 PlaceInfo라는 xml파일에 SharedPreference를 이용해서 저장해준다.
        prefsEditor = mPrefs.edit(); // sharedPreference를 수정하기 위한 에디터를 연결해준다.
        gson = new Gson(); // 리스트를 통쨰로 저장하기 위한 gson 객체를 생성해준다.

        //init();

        //리사이클러뷰 관련 코드
        mRecyclerView = findViewById(R.id.main_recycler); // xml상에 만들어둔 리사이클러뷰를 코드상으로 연결시켜준다.
        mRecyclerView.setHasFixedSize(true); //리사이클러뷰의 크기와 관련된 부분, 어댑터를 변경해도 리사이클러뷰의 크기에 영향을 주지 않으려면 true
        mLayoutManager = new LinearLayoutManager(this); // 레이아웃 매니저는 리사이클러뷰 내부의 뷰 아이템들의 position을 결정한다.
        mRecyclerView.setLayoutManager(mLayoutManager);

        gestureDetector = new GestureDetector(getApplicationContext(),new GestureDetector.SimpleOnGestureListener() {

            //누르고 뗄 때 한번만 인식하도록 하기위해서
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        mRecyclerView.addOnItemTouchListener(onItemTouchListener); // 리사이클러뷰 내부의 아이템을 터치했을때의 동작을 추가해준다.

        backPressCloseHandler = new BackPressCloseHandler(this); // 뒤로가기 버튼을 눌렀을때의 동작


        /*
        * 리사이클러뷰의 이동, 스와이프 동작과 관련된 클래스 => ItemTouchHelper
        * Callback 클래스와 RecyclerView 클래스와 함께 사용된다.
        * onMove, onSwipe 메소드가 있다.
        * */
        ItemTouchHelperCallback mCallback = new ItemTouchHelperCallback(mainAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(mCallback); // ItemToucHelper은 리사이클러뷰에 드래그앤 드롭 지원을 추가하는 클래스, Callback 클래스와 함께 작동한다.
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);



        // 저장된 상태를 불러오는 코드, Destroy 된 후에 다시 onCreate 되었을때 사용된다.

        if(savedInstanceState != null && workingPlaceInfo.size() != 0){
            ArrayList<MainWorkingPlace_Info> data = (ArrayList<MainWorkingPlace_Info>) savedInstanceState.getSerializable("SAVEDDATA");
            workingPlaceInfo = data;
            mRecyclerView.setAdapter(mainAdapter);
        }
    }

   /* private void init() {
        String json = mPrefs.getString("Index", null).;
        Place_ID = gson.fromJson(json, ArrayList<String>);
    }*/

    RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerView.OnItemTouchListener() {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            //손으로 터치한 곳의 좌표를 토대로 해당 Item의 View를 가져옴
            View childView = rv.findChildViewUnder(e.getX(),e.getY());

            //터치한 곳의 View가 RecyclerView 안의 아이템이고 그 아이템의 View가 null이 아니라
            //정확한 Item의 View를 가져왔고, gestureDetector에서 한번만 누르면 true를 넘기게 구현했으니
            //한번만 눌려서 그 값이 true가 넘어왔다면
            if(childView != null && gestureDetector.onTouchEvent(e)){
                Log.i(getClass().toString(), "Touch Event!!!");
                //현재 터치된 곳의 position을 가져오고
                int currentPosition = rv.getChildAdapterPosition(childView);
                //해당 위치의 Data를 가져옴
                //MainWorkingPlace_Info currentItem = workingPlaceInfo.get(currentPosition);
                //workingPlaceInfo.get(currentPosition).position = currentPosition;
                String Place_id = index_place.index.get(currentPosition);
                Log.i(getClass().toString(), String.valueOf(currentPosition + "번 아이템 클릭 " + workingPlaceInfo.get(currentPosition).position + "들어감"));
                Intent intent = new Intent(getApplicationContext(), Working_history.class);
                intent.putExtra("Place_postion", currentPosition);
                intent.putExtra("Place id", Place_id); // 리스트의 포지션 값을 보내준다. 해당 포지션값으로 저장된 데이터를 탐색하기 위함
                startActivityForResult(intent, 2);
                //overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity); // 액티비티 전환 애니메이션
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

    protected void onClick_place_add(View v){ // 근무지추가 버튼을 누르면 근무지추가 액티비티로 이동
        Intent intent = new Intent(getApplicationContext(), Place_add.class);
        startActivityForResult(intent, 1);

    }

@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){ // startActivityForResult의 결과값을 받으면 처리하는 부분
        super.onActivityResult(requestCode, resultCode, data); // 부모클래스와 관련됨

        if(resultCode == RESULT_OK){ // 결과가 잘 나왔을 경우
            if(requestCode == 1) { // 받은 코드가 1이라면, 근무지 추가 리퀘스트
                Toast.makeText(getApplicationContext(), "근무지가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                Display_Place_Info();

                //workingPlaceInfo.add((MainWorkingPlace_Info) data.getSerializableExtra("workingPlace_info"));

                //mRecyclerView.setAdapter(mainAdapter);
            } else if(requestCode == 2){
                Log.i(getClass().toString(), String.valueOf(data.getExtras().getInt("changed_data_index") + "번째 포지션 돌려받음"));
                MainWorkingPlace_Info changed_info = get_Changed_data(data.getStringExtra("changed_data_id"));
                workingPlaceInfo.set(data.getExtras().getInt("changed_data_postion"), changed_info);
                mRecyclerView.setAdapter(mainAdapter);
            }
        }else if(resultCode == 3){ // 근무지 삭제와 관련된 결과코드
            removeItem(data.getIntExtra("delete_position", 0));

        } else if(resultCode == 4){ // 근무지 수정과 관련된 결과코드

        }

    }

    private MainWorkingPlace_Info get_Changed_data(String position) {

        String json = mPrefs.getString(position, null);
        MainWorkingPlace_Info tmp = gson.fromJson(json, MainWorkingPlace_Info.class);
        return tmp;

    }

    protected void onClick_setup(View v){ // 설정버튼을 누르면 설정화면으로 이동
        Intent intent = new Intent(getApplicationContext(), Setup.class);
        startActivity(intent);
    }


    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();
    }

    ///// 데이터 저장

    protected void Save_Place_Id(){

        Log.i(getClass().toString(), "세이브할때 아이디 개수 : " + String.valueOf(index_place.index.size()));

        if(index_place.index.size() != 0){ // 저장할 데이터가 있을때 저장하도록 한다.

            JSONArray jsonArray = new JSONArray(); // JsonArray를 만들어준 뒤, 현재 남아있는 아이디값을 다시 저장
            for(int i=0; i<index_place.index.size(); i++){
                jsonArray.put(index_place.index.get(i));
            }
            prefsEditor.putString("Index", jsonArray.toString()); // 저장된 인덱스값 교체
            prefsEditor.commit();

        }
    }

    protected void Display_Place_Info(){

        if(mPrefs == null){ // 저장된 데이터가 아무것도 없는 경우
        //아무일도 하지 않는다.
            Log.i(getClass().toString(), "저장된 데이터가 없다!!!");
        }else { // 그렇지 않은 경우, 데이터를 가져와서 리사이클러뷰에 표시해준다.
            Log.i(getClass().toString(), "복구할때 리스트 크기 " + String.valueOf(workingPlaceInfo.size()));

            if(index_place.index.size() == 0) { // 액티비티가 파괴되었거나 처음 데이터를 추가하는 경우엔 사이즈가 0이 될테니
                Log.i(getClass().toString(), "리스트 크기가 0인 경우");

                String index_son = mPrefs.getString("Index", null); // 기존에 저장되어있는 인덱스값을 불러와서


                if(index_son == null){

                }else {
                    try {
                        JSONArray jsonArray = new JSONArray(index_son);
                        for(int i=0; i<jsonArray.length(); i++){
                            String data = jsonArray.optString(i);
                            index_place.index.add(data);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < index_place.index.size(); i++) { // 저장된 데이터 전부를 리스트에 넣어주는 작업을 한다.
                        String json = mPrefs.getString(index_place.index.get(i), null);
                        MainWorkingPlace_Info tmp = gson.fromJson(json, MainWorkingPlace_Info.class);
                        workingPlaceInfo.add(tmp);
                    }
                }

            }else { // 리스트가 남아 있는데, 데이터가 추가 되는 경우 값 1개만 추가되도록, 즉 앱종료후 재실행 상태가 아닌 아이템 추가일때
                Log.i(getClass().toString(), "리스트 크기가 0이 아닌 경우");

                if(workingPlaceInfo.size() == index_place.index.size()){ //만약 리스트 개수와 저장된 데이터 개수가 같은 경우엔 데이터 추가가 없는 것

                }else {
                    int newdata_index = index_place.index.size() -1;
                    String id_key = index_place.index.get(newdata_index);
                    String json = mPrefs.getString(id_key, null);
                    //String json2 = mPrefs.getString("Index", null);
                    //Log.i("JSON!!!!!!", json2);
                    MainWorkingPlace_Info tmp = gson.fromJson(json, MainWorkingPlace_Info.class);
                    workingPlaceInfo.add(tmp);
                }
            }
        }
        mRecyclerView.setAdapter(mainAdapter);

    }

    //////////


    @Override
    protected void onStart() {

        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        Save_Place_Id();
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
        //Save_Place_Info();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResum");
        Display_Place_Info();

        if(workingPlaceInfo.size() == 0){
            empty_text.setText("추가된 근무지가 없습니다.\n근무지 추가 버튼을 눌러 근무지를 추가해 주세요.");
            empty_text.setGravity(Gravity.CENTER);
        }else {
            empty_text.setText("");
        }





    }

    @Override
    protected void onRestart() {

        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSAVEINSTANCESTATE");
        ArrayList<MainWorkingPlace_Info> data = workingPlaceInfo;
        outState.putSerializable("SAVEDDATA", data);
    }

    public void removeItem(int position) {
        workingPlaceInfo.remove(position);// 근무지 리사이클러뷰 리스트에서 삭제
        String tmp_index = index_place.index.get(position); // 저장된 데이터 삭제를 위해 해당 키값 임시 저장
        index_place.index.remove(position); // 키값을 저장하고 있는 리스트에서 해당 키값 삭제
        JSONArray jsonArray = new JSONArray(); // JsonArray를 만들어준 뒤, 현재 남아있는 아이디값을 다시 저장
        for(int i=0; i<index_place.index.size(); i++){
            jsonArray.put(index_place.index.get(i));
        }
        SharedPreferences nPref = getSharedPreferences(tmp_index, MODE_PRIVATE);
        SharedPreferences.Editor editor = nPref.edit();
        editor.clear();
        editor.commit();
        String filePath = getApplicationContext().getFilesDir().getParent()+"/shared_prefs/"+tmp_index+".xml";
        File deletePrefFile = new File(filePath );
        deletePrefFile.delete();

        prefsEditor.putString("Index", jsonArray.toString()); // 저장된 인덱스값 교체
        prefsEditor.remove(tmp_index); // 저장된 데이터상에서 해당 키값을 가진 데이터 삭제
        prefsEditor.commit();
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        mainAdapter.notifyItemRemoved(position);
        mRecyclerView.setAdapter(mainAdapter);
    }

}
