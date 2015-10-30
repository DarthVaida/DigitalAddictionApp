package com.example.TaskMonitor;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.*;

/*
Created by Alex on 10/02/2015.
Class to represent the internal database. A single UserInformation object will contain all data stored by the app.
The Class contains:
- an Array of DailyInformation objects containing daily data usage.
- a hashmap of the hours of the day and their ranks
- an array of 1000 double values from which the checking schedule is derived

 */
public class UserInformation implements Serializable, Parcelable {

    private ArrayList<DailyInformation> arrayOfDays = new ArrayList<DailyInformation>();
    private HashMap<Integer, HourlyInformation> hashmapOfRanks = new HashMap<Integer, HourlyInformation>();
    private double[] randomizedSchedule = new double[1000];


    public UserInformation(int constantCounter, int randomCounter) {

        arrayOfDays.add(new DailyInformation(constantCounter, randomCounter, new CustomDate()));
    }

    public UserInformation(Parcel in) {
        readFromParcel(in);
    }

    public HashMap<Integer, HourlyInformation> getHashmapOfRanks() {
        return hashmapOfRanks;
    }

    public void setHashmapOfRanks(HashMap<Integer, HourlyInformation> hashmapOfRanks) {
        this.hashmapOfRanks = hashmapOfRanks;
    }
/*
// increment rank by 2
// set increment flag
 */
    public void incrementRank(int hour) {
        try {
            if (hashmapOfRanks.get(hour).isUpdatedToday() == false)
                hashmapOfRanks.put(hour, new HourlyInformation(hashmapOfRanks.get(hour).getRank() + 2));


        } catch (NullPointerException e) {
            Log.d("RAND", "Entry not found. Creating new one!   1");
            hashmapOfRanks.put(hour, new HourlyInformation());
        }

    }

    public double[] getRandomizedSchedule() {
        return randomizedSchedule;
    }

    public void setRandomizedSchedule(double[] randomizedSchedule) {
        this.randomizedSchedule = randomizedSchedule;
    }

    public ArrayList<DailyInformation> getArrayOfDays() {
        return arrayOfDays;

    }
/*
Add a new DailyInformation object to the array. This method is called at the begining of a new day.
 */
    public void addDailyInformation(int constantCounter, int randomCounter) {
        Calendar currentTime = Calendar.getInstance();


        if (this.getLastAddedElement().getCustomDate().checkEquality(currentTime.get(Calendar.DAY_OF_MONTH), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.YEAR))) {
            Log.d("CALENDAR COMPARISON", "They are equal");
            try {
                arrayOfDays.get(arrayOfDays.size() - 1).setConstantlyMonitoredDailyUsage(constantCounter);
                arrayOfDays.get(arrayOfDays.size() - 1).setRandomlyMonitoredDailyUsage(randomCounter);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            arrayOfDays.add(new DailyInformation(constantCounter, randomCounter, new CustomDate()));
            arrayOfDays.get(arrayOfDays.size() - 1).setAverageDailyUsage(arrayOfDays.get(arrayOfDays.size() - 2).getAverageDailyUsage());
        }
    }

    public DailyInformation getLastAddedElement() {
        return arrayOfDays.get(arrayOfDays.size() - 1);

    }

    public int getTotalUsage() {
        int sum = 0;
        for (DailyInformation day : arrayOfDays) {
            sum = sum + day.getConstantlyMonitoredDailyUsage();
        }
        return sum;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UserInformation createFromParcel(Parcel in) {
            return new UserInformation(in);
        }

        public UserInformation[] newArray(int size) {
            return new UserInformation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(arrayOfDays);
        dest.writeDoubleArray(randomizedSchedule);
    }

    private void readFromParcel(Parcel in) {
        in.readTypedList(arrayOfDays, DailyInformation.CREATOR);
        in.readDoubleArray(randomizedSchedule);

    }

    /*
    Returns the hours with the top 5 highest ranks, sorted by rank.
     */
    public ArrayList<Integer> getHoursWithTopRanks() {
        ArrayList<Integer> hoursWithTopRanks = new ArrayList<Integer>();
        ArrayList<Integer> sortedHoursByRank = new ArrayList<Integer>();
        for (Map.Entry<Integer, HourlyInformation> entry : hashmapOfRanks.entrySet()) {
            sortedHoursByRank.add(entry.getKey());
        }
        Collections.sort(sortedHoursByRank, new RankComparator());
        int i = 0;
        while (i < sortedHoursByRank.size()) {

            if (i < 5)
                hoursWithTopRanks.set(i, sortedHoursByRank.get(i));
            else {
                if (sortedHoursByRank.get(i) == sortedHoursByRank.get(i - 1)) {
                    hoursWithTopRanks.set(i, sortedHoursByRank.get(i));
                }
            }
            i++;
        }
        return hoursWithTopRanks;
    }


    @Override
    public String toString() {
        return arrayOfDays.toString();
    }


    private class RankComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            if (hashmapOfRanks.get(lhs).getRank() > hashmapOfRanks.get(rhs).getRank())
                return lhs;
            else return rhs;
        }
    }
}
