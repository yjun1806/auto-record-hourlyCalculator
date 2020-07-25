package com.test.myapplication;

import android.net.Uri;

import java.io.Serializable;

public class Day_Info implements Serializable{
    int year; // 날짜
    int month;
    int day_of_month;
    String day_of_week;
    int Day_start_time_hour; // 시작 시간
    int Day_start_time_min; // 시작 분
    int Day_end_time_hour; // 끝나는 시간
    int Day_end_time_min; // 끝나는 분
    int hour_pay;
    int Daily_total_time_min; // 오늘 근무한 시간(분단위)
    int Daily_total_pay; // 오늘 일당
    int total_positon = 0;

    //byte[] daily_image;
    String daily_image_path;
    String diary;

    String Day_Id;


    public Day_Info(){
    }


    public Day_Info(int year,int month, int day_of_month,
                    int day_start_time_hour, int day_start_time_min,
                    int day_end_time_hour, int day_end_time_min, int hour_pay, String day_of_week,
                    byte[] daily_image, String diary,
                    int total_positon){
        this.year = year;
        this.month = month;
        this.day_of_month = day_of_month;
        this.Day_start_time_hour = day_start_time_hour;
        this.Day_start_time_min = day_start_time_min;
        this.Day_end_time_hour = day_end_time_hour;
        this.Day_end_time_min = day_end_time_min;
        this.hour_pay = hour_pay;
        this.day_of_week = day_of_week;
        //this.daily_image = daily_image;
        this.diary = diary;
        this.total_positon = total_positon;

        Calculate();
    }

    public void Calculate(){
        int Hour;
        int Min;
        if(Day_end_time_hour >= Day_start_time_hour){
            Hour = Day_end_time_hour - Day_start_time_hour;
            Min = Day_end_time_min - Day_start_time_min;
        }else {
            Hour = Day_end_time_hour + (24-Day_start_time_hour);
            Min = Day_end_time_min - Day_start_time_min;
        }
        Daily_total_time_min = Hour * 60 + Min;
        Daily_total_pay = Hour*hour_pay +  (Daily_total_time_min%60)*(hour_pay/60);
    }
}
