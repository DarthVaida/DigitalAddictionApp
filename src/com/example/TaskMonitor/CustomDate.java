package com.example.TaskMonitor;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Alex on 05/02/2015.
 * Class to represent dates in a simple day-month-year format.
 */
public class CustomDate implements Serializable, Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CustomDate createFromParcel(Parcel in) {
            return new CustomDate(in);
        }

        public CustomDate[] newArray(int size) {
            return new CustomDate[size];
        }
    };
    private int day;
    private int month;
    private int year;

    public CustomDate() {
        Calendar calendar = Calendar.getInstance();
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.year = calendar.get(Calendar.YEAR);
    }

    public CustomDate(int day1, int month1, int year1) {

        day = day1;
        month = month1;
        year = year1;
    }

    public CustomDate(Parcel in) {
        readFromParcel(in);
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {

        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    /*
    Retrun the month of this CustomDate object in the format of Calendar.Month private field format.
     */
    public int getMonthConstant() {

        int m;
        switch (month) {

            case 1:
                m = Calendar.JANUARY;
                break;
            case 2:
                m = Calendar.FEBRUARY;
                break;
            case 3:
                m = Calendar.MARCH;
                break;
            case 4:
                m = Calendar.APRIL;
                break;
            case 5:
                m = Calendar.MAY;
                break;
            case 6:
                m = Calendar.JUNE;
                break;
            case 7:
                m = Calendar.JULY;
                break;
            case 8:
                m = Calendar.AUGUST;
                break;
            case 9:
                m = Calendar.SEPTEMBER;
                break;
            case 10:
                m = Calendar.OCTOBER;
                break;
            case 11:
                m = Calendar.NOVEMBER;
                break;
            case 12:
                m = Calendar.DECEMBER;
                break;
            default:
                m = 0;
                break;
        }
        return m;

    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean checkEquality(int x, int y, int z) {
        return day == x && month == y + 1 && year == z;
    }

    @Override
    public String toString() {
        return day + "-" + month + "-" + year;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(day);
        dest.writeInt(month);
        dest.writeInt(year);
    }

    private void readFromParcel(Parcel in) {
        day = in.readInt();
        month = in.readInt();
        year = in.readInt();
    }
}
