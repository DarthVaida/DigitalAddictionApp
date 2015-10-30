package com.example.TaskMonitor;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
/**
 * Created by Alex on 07/03/2015.
 * Activity to display the main DashBoard
 */
public class HomeScreen extends Activity {



    private UserInformation userInformation = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Activity", "************ ACTIVITY STARTED *************");
        setContentView(R.layout.main);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Activity", "************ ON START EXECUTED *************");
        startService(new Intent(this, MainService.class));
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:

                return true;
            case R.id.action_settings:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
/*
Create the buttons
 */
    public void randomButton(View view) {
        Intent intent = new Intent(this, GraphScreen.class);
        Bundle extrasBundle = new Bundle();
        extrasBundle.putBoolean("random_view", true);
        extrasBundle.putBoolean("average_view", false);
        intent.putExtras(extrasBundle);
        HomeScreen.this.startActivity(intent);
    }

    public void averageButton(View view) {
        Intent intent = new Intent(this, GraphScreen.class);
        Bundle extrasBundle = new Bundle();
        extrasBundle.putBoolean("random_view", false);
        extrasBundle.putBoolean("average_view", true);
        intent.putExtras(extrasBundle);
        HomeScreen.this.startActivity(intent);
    }

    public void informationButton(View view) {
        Intent intent = new Intent(this, InformationScreen.class);
        HomeScreen.this.startActivity(intent);
    }
    public void profileButton(View view) {
        Intent intent = new Intent(this, ProfileScreen.class);
        HomeScreen.this.startActivity(intent);
    }


    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
}







