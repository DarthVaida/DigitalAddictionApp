package com.example.TaskMonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.ProgressWheel.ProgressWheel;

/**
 * Created by Alex on 16/03/2015.
 * Activity to display the user profile
 */

public class ProfileScreen extends Activity {
    /*
      Sample code provided with the ProgresWheel Open Source library. Author: Todd Davies.
      I do not own this code.
      Link to gitHub code: https://github.com/Todd-Davies/ProgressWheel/blob/master/src/com/todddavies/components/progressbar/main.java
      Link to gitHub project: https://github.com/Todd-Davies/ProgressWheel
     */
    boolean running;
    int progress = 0;
    UserInformation userInformation;
    int scoreInRadian;
    int score;
    //Determines the speed at which the wheel is drawn.
    int speed;


      ProgressWheel progressWheel;
      final Runnable runnable = new Runnable() {
        public void run() {
            running = true;
            while(progress<= scoreInRadian) {
                progressWheel.incrementProgress();
                progress++;
                try {
                    Thread.sleep(speed);
                    progressWheel.setText("Your score is: "+progressWheel.getPercentualProgress());

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            running = false;

        }
    };

    BroadcastReceiver uiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle tempB = intent.getExtras();
            userInformation = tempB.getParcelable("ui");
            int   currentRandomIndex = tempB.getInt("index");

            TextView continuousCounterText = (TextView) findViewById(R.id.continuousText);
            continuousCounterText.setText(secondsToString(userInformation.getLastAddedElement().getConstantlyMonitoredDailyUsage()));
            TextView averageCounterText = (TextView) findViewById(R.id.averageText);
            averageCounterText.setText(secondsToString(userInformation.getLastAddedElement().getAverageDailyUsage()));
            TextView totalText = (TextView) findViewById(R.id.totalText);
            totalText.setText(secondsToString(userInformation.getTotalUsage()));

            int intHours = (int) userInformation.getRandomizedSchedule()[currentRandomIndex];
            double doubleMinutes = ((userInformation.getRandomizedSchedule()[currentRandomIndex] - intHours) * 60);
            int intMinutes = (int) doubleMinutes;
            int intSeconds = (int) ((doubleMinutes - intMinutes) * 60);
            TextView nextAlarmCounterText = (TextView) findViewById(R.id.alarmText);
            nextAlarmCounterText.setText(intHours+":"+intMinutes+":"+intSeconds);


            /*
            Calculate the user score
             */
            score = userInformation.getLastAddedElement().usagePercentageOfAverage()/2;
            if(score<=25){
                speed=15;
            }
            else if(score>25 &&score<=50){
                speed=10;
            }
            else if (score>50 && score<100){
                speed = 5;
            }

            if(score<=100)
                scoreInRadian = (score*360)/100;
            else
            scoreInRadian=360;
            if(!running) {
                progress = 0;
                progressWheel.resetCount();
                Thread t = new Thread(runnable);
                t.start();

        }}};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressWheel = (ProgressWheel) findViewById(R.id.pw_spinner);
        progressWheel.setProgress(0);
        IntentFilter intentFilter = new IntentFilter(
                "service_to_profile");
        this.registerReceiver(uiReceiver, intentFilter);
        Log.d("ProfileScreen Activity", "ON START EXECUTED");
        Intent i = new Intent("profile_to_service");
        sendBroadcast(i);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(uiReceiver);
    }
/*
Convert seconds to a printable String time-format
 */
    public String secondsToString(int value){

        if (value<60){

            return ""+value;
        }
        else if (value>60 && value < 3600) {

            int m =  value / 60;
            int s =  value % 60;
            return m + ":" + s;

        } else if (value > 3600 && value<86400) {
            int h =  value / 3600;
            int m = (value % 3600) / 60;
            int s = (value % 3600) % 60;

            return h + ":" + m + ":" + s;
    }
        else if (value>86400){
            int d = value / 86400;
            int h = (value%86400) / 3600;
            int m = ((value%86400) % 3600) / 60;
            int s = (((value%86400) % 3600)) % 60;
            return d+":"+h + ":" + m + ":" + s;
        }
        else return "";
}}