package com.example.TaskMonitor;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Alex on 16/03/2015.
 */
public class InformationScreen extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}