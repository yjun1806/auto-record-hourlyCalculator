package com.test.myapplication;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

class MyxAxisValueFormatter implements IAxisValueFormatter {
    private String[] month;

    public MyxAxisValueFormatter(String[] month) {
        this.month = month;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return month[(int)value];
    }

}
