package com.example.TaskMonitor;

/**
 * Created by Alex on 28/02/2015.
 * Class to indicate the rank of a single hour.
 */
public class HourlyInformation {
    private int rank;
    private boolean isUpdatedToday;

    public HourlyInformation() {
        this.rank = 1;
        this.isUpdatedToday = true;
    }

    public HourlyInformation(int rank) {
        this.rank = rank;
        this.isUpdatedToday = true;
    }

    public boolean isUpdatedToday() {
        return isUpdatedToday;
    }

    public void setUpdatedToday(boolean isUpdatedToday) {
        this.isUpdatedToday = isUpdatedToday;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return " " + rank + " ";
    }
}
