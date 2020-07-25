package com.test.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class History_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<Day_Info> day_infos;
    History_Adapter(ArrayList<Day_Info> day_infos){
        this.day_infos = day_infos;
    }


    public class MyViewHolder2 extends RecyclerView.ViewHolder{
        TextView his_date;
        TextView his_start_time;
        TextView his_end_time;
        TextView his_working_time;
        TextView his_total_day_pay;
        TextView his_pay;
        CardView viewBackground, viewForeground;
        ImageView ivImage;
        TextView his_text;

        MyViewHolder2(View v){
            super(v);
            his_date = v.findViewById(R.id.his_date);
            his_start_time = v.findViewById(R.id.his_start_time);
            his_end_time = v.findViewById(R.id.his_end_time);
            his_working_time = v.findViewById(R.id.his_working_time);
            his_total_day_pay = v.findViewById(R.id.his_total_day_pay);
            his_pay = v.findViewById(R.id.his_pay);
            viewBackground = v.findViewById(R.id.back);
            viewForeground = v.findViewById(R.id.fore);
            ivImage = v.findViewById(R.id.preview);
            his_text = v.findViewById(R.id.pretext);
        }


    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row, parent, false);

        return new MyViewHolder2(v);

    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        History_Adapter.MyViewHolder2 myViewHolder = (History_Adapter.MyViewHolder2) holder;
        myViewHolder.his_date.setText(String.format("%d년 %02d월 %02d일 ", day_infos.get(position).year, day_infos.get(position).month, day_infos.get(position).day_of_month) + day_infos.get(position).day_of_week);
        myViewHolder.his_start_time.setText(String.format("%02d:%02d", day_infos.get(position).Day_start_time_hour, day_infos.get(position).Day_start_time_min));
        myViewHolder.his_end_time.setText(String.format("%02d:%02d", day_infos.get(position).Day_end_time_hour, day_infos.get(position).Day_end_time_min));
        myViewHolder.his_working_time.setText(String.format("%d시간 %d분", day_infos.get(position).Daily_total_time_min/60, day_infos.get(position).Daily_total_time_min%60));
        myViewHolder.his_total_day_pay.setText(String.valueOf(day_infos.get(position).Daily_total_pay));
        myViewHolder.his_pay.setText("시급 "+day_infos.get(position).hour_pay + "원");

        if(day_infos.get(position).daily_image_path != null){
            File file = new File(day_infos.get(position).daily_image_path);
            myViewHolder.ivImage.setImageURI(Uri.fromFile(file));
        }else {
            myViewHolder.ivImage.setImageResource(R.drawable.de);
        }
        /*if(day_infos.get(position).daily_image != null){
            Bitmap bmp = BitmapFactory.decodeByteArray(day_infos.get(position).daily_image, 0, day_infos.get(position).daily_image.length); // 인텐트로 넘겨받은 이미지 배열값을 저장
            myViewHolder.ivImage.setImageBitmap(bmp); // 이미지뷰에 표시해준다.
        }*/
        myViewHolder.his_text.setText(day_infos.get(position).diary);
    }

    @Override
    public int getItemCount() {
        return day_infos.size();
    }

 }

