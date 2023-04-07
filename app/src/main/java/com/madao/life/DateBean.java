package com.madao.life;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

public class DateBean {
    protected int year = 2000;
    protected int month = 1;
    protected int day = 1;

    public void fromString(String s) {
        var dateArr = s.split("/");
        if (dateArr.length != 3) {
            Log.e("DateBean", "fromString error value: " + s);
        } else {
            year = Integer.parseInt(dateArr[0]);
            month = Integer.parseInt(dateArr[1]);
            day = Integer.parseInt(dateArr[2]);
        }
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format("%d/%d/%d", year, month, day);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

}
