package com.test.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

//http://ghj1001020.tistory.com/311


public class AdapterSpinner extends BaseAdapter {
    Context context;
    ArrayList<MainWorkingPlace_Info> data;
    LayoutInflater inflater;

    public AdapterSpinner(Context context, ArrayList<MainWorkingPlace_Info> place_index) {
        this.context = context;
        this.data = place_index;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(data != null) return data.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.spinner_normal, parent, false);
        }

        if(data != null){
            String text = data.get(position).Place_name;
            ((TextView)convertView.findViewById(R.id.spinnerText)).setText(text);
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView== null){
            convertView = inflater.inflate(R.layout.spinner_dropdown, parent, false);
        }

        String text = data.get(position).Place_name;
        ((TextView)convertView.findViewById(R.id.spinnerText)).setText(text);

        return convertView;
    }
}
