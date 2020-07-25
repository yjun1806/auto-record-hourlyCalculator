package com.test.myapplication;

import java.io.Serializable;
import java.util.ArrayList;

public class MainWorkingPlace_Info implements Serializable {
    public String IDNumber;

    public String Place_name; // 근무 명
    public String Hour_pay; // 시급
    public String Start_time_hour; // 시작시간
    public String Start_time_min; // 시작시간, 분

    public String End_time_hour; // 종료시간
    public String End_time_min; // 종료시간, 분

    public String Working_time; // 근무시간
    public String Working_time_min;

    public String Place_latitude; // 근무 장소 위도
    public String Place_longitude; // 근무 장소 경도

    String[] week;

    //ArrayList<Day_Info> save_day_info = null;
    int position;

    boolean[] when_notify;



    public MainWorkingPlace_Info(){
        IDNumber = String.valueOf(System.currentTimeMillis()); // 중복된 아이디가 생성이 안되도록 하기 위함
    }


    public MainWorkingPlace_Info(String Place_name, String Start_time, String Start_time_min,
                                 String End_time, String End_time_min,
                                 String Hour_pay, String Working_time, String Working_time_min,
                                 String place_latitude, String place_longitude, String[] week){
        this.Place_name = Place_name;
        this.Hour_pay = Hour_pay;
        this.Start_time_hour = Start_time;
        this.Start_time_min = Start_time_min;
        this.End_time_hour = End_time;
        this.End_time_min = End_time_min;
        this.Working_time = Working_time;
        this.Working_time_min = Working_time_min;
        this.Place_latitude = place_latitude;
        this.Place_longitude = place_longitude;
        this.week = week;

    }



}
