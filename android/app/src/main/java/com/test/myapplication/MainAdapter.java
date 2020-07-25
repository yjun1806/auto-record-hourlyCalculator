package com.test.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;


/*
* 리사이클러뷰 실행 순서
* I/MainAdapter: getItemCount : 뷰의 개수를 가져와서
* I/MainAdapter: onCreateViewHolder : xml 레이아웃을 가져오고
* I/MainAdapter: ViewHolder : 그 레이아웃 내의 뷰를 코드상으로 연결
                 onBindViewHolder : 코드상으로 연결된 뷰의 데이터를 넣어준다.
*
* */
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperCallback.OnItemMoveListenr {

    @Override
    public boolean onItemMove(int fromPostion, int toPostion) {
        Collections.swap(workingPlaceInfo, fromPostion, toPostion);
        Collections.swap(MainActivity.index_place.index, fromPostion, toPostion);

        Log.i("MainAdapter", "onItemMove");

        notifyItemMoved(fromPostion, toPostion);
        return false;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView working_name;
        TextView start_time;
        TextView end_time;
        TextView hour_pay;
        TextView working_time;
        TextView week;

        MyViewHolder(View v){
            super(v);
            Log.i("MainAdapter", "ViewHolder");
            working_name = v.findViewById(R.id.Working_name_main1);
            start_time = v.findViewById(R.id.start_time);
            end_time = v.findViewById(R.id.end_time);
            hour_pay = v.findViewById(R.id.hour_pay);
            working_time = v.findViewById(R.id.working_time);
            week = v.findViewById(R.id.week);
        }
    }


    private ArrayList<MainWorkingPlace_Info> workingPlaceInfo;
    MainAdapter(ArrayList<MainWorkingPlace_Info> workingPlaceInfo){
        this.workingPlaceInfo = workingPlaceInfo;
    }

    @NonNull
    @Override // 리사이클러뷰의 행을 표시하는데 사용되는 레이아웃 xml을 가져오는 역할
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("MainAdapter", "onCreateViewHolder");

        // 레이아웃 인플레이터를 사용해 xml을 View에 저장
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_main_row, parent, false);

        return new MyViewHolder(v);

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.i("MainAdapter", "onBindViewHolder");
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        myViewHolder.working_name.setText(workingPlaceInfo.get(position).Place_name); // 근무지 이름을 표시
        myViewHolder.start_time.setText(String.format("%02d:%02d", Integer.valueOf(workingPlaceInfo.get(position).Start_time_hour),
                Integer.valueOf(workingPlaceInfo.get(position).Start_time_min))); // 시작시간 표시

        myViewHolder.end_time.setText(String.format("%02d:%02d", Integer.valueOf(workingPlaceInfo.get(position).End_time_hour),
                Integer.valueOf(workingPlaceInfo.get(position).End_time_min))); // 끝나느 시간 표시
        myViewHolder.hour_pay.setText(workingPlaceInfo.get(position).Hour_pay); // 시급 표시

        myViewHolder.working_time.setText(workingPlaceInfo.get(position).Working_time + "시간 " + workingPlaceInfo.get(position).Working_time_min + "분"); // 근무시간 표시

        String result = "";
        for(int i=0; i<7; i++){
            if(workingPlaceInfo.get(position).week[i].equals("")){

            } else
                result += (workingPlaceInfo.get(position).week[i] + " ");
        }
        myViewHolder.week.setText("근무 요일 : " + result);


    }

    @Override
    public int getItemCount() {
        Log.i("MainAdapter", "getItemCount");
        return workingPlaceInfo.size();
    }
}
