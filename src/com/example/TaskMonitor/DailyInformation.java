package com.example.TaskMonitor;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Alex on 05/02/2015.
 * Class to hold daily information about the monitored usage and state of addiction.
 * Each object of this class will contain a reference to a CustomDate object that indicates the date.
 */
public class DailyInformation implements Serializable, Parcelable {

    /*
    Public fields that indicate the state of addiction
     */
    public final int NO_ADDICTION = 0;
    public final int NEUTRAL = 1;
    public final int POSSIBLE_ADDICTION = 2;
    public final int ADDICTION = 3;
    /*
   Continuously monitored usage.
    */
    private int constantlyMonitoredDailyUsage = 0;
    /*
   Randomly monitored usage.
    */
    private int randomlyMonitoredDailyUsage = 0;
    private int averageDailyUsage = 0;
    private CustomDate customDate;

    public DailyInformation(int constantlyMonitoredDailyUsage, int randomlyMonitoredDailyUsage, CustomDate customDate) {
        this.constantlyMonitoredDailyUsage = constantlyMonitoredDailyUsage;
        this.randomlyMonitoredDailyUsage = randomlyMonitoredDailyUsage;
        this.customDate = customDate;
    }

    public DailyInformation(int constantlyMonitoredDailyUsage, int randomlyMonitoredDailyUsage, int averageDailyUsage, CustomDate customDate) {
        this.constantlyMonitoredDailyUsage = constantlyMonitoredDailyUsage;
        this.randomlyMonitoredDailyUsage = randomlyMonitoredDailyUsage;
        this.averageDailyUsage = averageDailyUsage;
        this.customDate = customDate;
    }

    public DailyInformation(Parcel in) {
        readFromParcel(in);
    }

    public int getRandomlyMonitoredDailyUsage() {
        return randomlyMonitoredDailyUsage;
    }

    public void setRandomlyMonitoredDailyUsage(int randomlyMonitoredDailyUsage) {
        this.randomlyMonitoredDailyUsage = randomlyMonitoredDailyUsage;
    }

    public int getConstantlyMonitoredDailyUsage() {

        return constantlyMonitoredDailyUsage;
    }

    public void setConstantlyMonitoredDailyUsage(int constantlyMonitoredDailyUsage) {

        this.constantlyMonitoredDailyUsage = constantlyMonitoredDailyUsage;
    }

    public void addConstantlyMonitoredDailyUsage(int constantlyMonitoredDailyUsage) {

        this.constantlyMonitoredDailyUsage = this.constantlyMonitoredDailyUsage + constantlyMonitoredDailyUsage;
    }

    public void addRandomlyMonitoredDailyUsage(int randomlyMonitoredDailyUsage) {

        this.randomlyMonitoredDailyUsage = this.randomlyMonitoredDailyUsage + randomlyMonitoredDailyUsage;
    }

    public CustomDate getCustomDate() {
        return customDate;
    }

    public void setCustomDate(CustomDate customDate) {
        this.customDate = customDate;
    }

    public int getAddictionStatus() {
        //No addiction
        if (usagePercentageOfAverage() < 75) {
            return NO_ADDICTION;
        }
        //Average user
        else if (usagePercentageOfAverage() >= 75 && usagePercentageOfAverage() < 125) {
            return NEUTRAL;
        }
        //Possible addiction
        else if (usagePercentageOfAverage() >= 125 && usagePercentageOfAverage() < 200) {
            return POSSIBLE_ADDICTION;
        }
        //Addict
        else return ADDICTION;
    }

    public int usagePercentageOfAverage() {
        return (constantlyMonitoredDailyUsage * 100) / averageDailyUsage;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public DailyInformation createFromParcel(Parcel in) {
            return new DailyInformation(in);
        }

        public DailyInformation[] newArray(int size) {
            return new DailyInformation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(constantlyMonitoredDailyUsage);
        dest.writeInt(randomlyMonitoredDailyUsage);
        dest.writeInt(averageDailyUsage);
        dest.writeParcelable(customDate, flags);
    }

    private void readFromParcel(Parcel in) {
        constantlyMonitoredDailyUsage = in.readInt();
        randomlyMonitoredDailyUsage = in.readInt();
        averageDailyUsage = in.readInt();
        customDate = in.readParcelable(CustomDate.class.getClassLoader());
    }

    public int getAverageDailyUsage() {
        return averageDailyUsage;
    }

    public void setAverageDailyUsage(int averageDailyUsage) {
        this.averageDailyUsage = averageDailyUsage;
    }

    @Override
    public String toString() {
        return ("\n" + "Date: " + customDate.toString() + " - Usage: " + constantlyMonitoredDailyUsage + " - Random: " + randomlyMonitoredDailyUsage + " - Average: " + averageDailyUsage);
    }


}
