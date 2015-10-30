package com.example.TaskMonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Alex on 07/02/2015.
 * Activity to graphically represent graphs
 */
public class GraphScreen {
    private UserInformation userInformation = null;
    private GraphView graph = null;
    private boolean monthlyView = false;
    private boolean weeklyView = false;
    private boolean randomMonitoringView = false;
    private boolean overallAverageView = false;
    private Calendar displayCalendar;
    private Calendar weeklyDisplayCalendar;
    private Calendar comparrisonDisplayCalendar;
    private Calendar tempCalendar;
    LineGraphSeries<DataPoint> series;
    DataPoint[] dataPoints;
    /*
    Broadcast receiver to listen for intents sent by the service containing the current UserInformation object.
     */
    BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
    Bundle tempB = intent.getExtras();
    userInformation = tempB.getParcelable("ui");
    Log.d("Graph Activity - PARCEL", userInformation.toString());


    tempCalendar= Calendar.getInstance();
    tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
    tempCalendar.set(Calendar.MINUTE, 0);
    tempCalendar.set(Calendar.SECOND, 0);

    displayCalendar = Calendar.getInstance();
    displayCalendar.set(Calendar.HOUR_OF_DAY, 0);
    displayCalendar.set(Calendar.MINUTE, 0);
    displayCalendar.set(Calendar.SECOND, 0);

    weeklyDisplayCalendar = Calendar.getInstance();
    weeklyDisplayCalendar.set(Calendar.HOUR_OF_DAY, 0);
    weeklyDisplayCalendar.set(Calendar.MINUTE, 0);
    weeklyDisplayCalendar.set(Calendar.SECOND, 0);

    comparrisonDisplayCalendar = weeklyDisplayCalendar;
    comparrisonDisplayCalendar.add(Calendar.DAY_OF_MONTH,1);

        TextView t = (TextView) findViewById(R.id.title);
        if(overallAverageView) {

            if (userInformation.getLastAddedElement().getAddictionStatus()==0) {
                t.setText("Wohoo! You have spent far less time on Facebook than the majority.");
            }
            else if (userInformation.getLastAddedElement().getAddictionStatus()==1) {
                t.setText("You have been using Facebook as much as any other normal person.");
            }
            else if (userInformation.getLastAddedElement().getAddictionStatus()==2) {
                t.setText("Today you have spent more time on Facebook than the majority. You should take a break.");
            }
            else if (userInformation.getLastAddedElement().getAddictionStatus()==3) {
                t.setText("You have spent considerably more time on Facebook than the majority. This might be a sign of serious addiction.");
            }

        }
        else if(randomMonitoringView){

        }





        //Build the Graph

        graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(GraphScreen.this)


        {
            /*
            Convert seconds into friendly time format
             */
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {

                    return super.formatLabel(value, isValueX);
                } else {

                    if (value < 3600) {

                        int m = (int) value / 60;
                        int s = (int) value % 60;
                        return String.format("%02d:%02d", m,s);

                    } else if (value >= 3600) {
                        int h = (int) value / 3600;
                        int m = (int) (value % 3600) / 60;
                        int s = (int) (value % 3600) % 60;

                        return String.format("%d:%02d:%02d",h, m,s);
                    } else {
                        return super.formatLabel(value, isValueX);
                    }
                }

            }
        });

        graph.getGridLabelRenderer().setGridColor(Color.rgb(150, 150, 150));
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        drawMonthlyGraph();

    }
};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.graph);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extrasBundle = getIntent().getExtras();

        randomMonitoringView = extrasBundle.getBoolean("random_view");
        overallAverageView = extrasBundle.getBoolean("average_view");




    }

    public void weeklyButton(View view) {
        monthlyView = false;
        weeklyView = true;
        setBoundaries();
    }

    public void monthlyButton(View view) {

        monthlyView = true;
        weeklyView = false;
        setBoundaries();
    }

    public void previousButton(View view) {


        if (monthlyView) {
            displayCalendar.add(Calendar.MONTH, -1);
        }
        if (weeklyView) {
            weeklyDisplayCalendar.add(Calendar.DAY_OF_MONTH, -7);
        }
        setBoundaries();
    }



    public void nextButton(View view) {

        if (monthlyView && (displayCalendar.get(Calendar.MONTH) != tempCalendar.get(Calendar.MONTH))) {
            displayCalendar.add(Calendar.MONTH, 1);
            setBoundaries();
        }


        if (weeklyView && (weeklyDisplayCalendar.get(Calendar.WEEK_OF_YEAR) != (tempCalendar.get(Calendar.WEEK_OF_YEAR)+1) )) {
            weeklyDisplayCalendar.add(Calendar.DAY_OF_MONTH, 7);
            setBoundaries();
        }

    }

    //Only called once, when the activity is created.
    public void drawMonthlyGraph() {
        weeklyView = false;
        monthlyView = true;
        setBoundaries();
        addUserInformation();
    }
    /*
    Set the boundaries of the data to display. This method is called to update the view and make the graph dynamic.
     */
    private void setBoundaries() {
        graph.getViewport().setMinY(0);
        if (monthlyView) {
            //Set minimum X boundary
            displayCalendar.set(Calendar.DAY_OF_MONTH, displayCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            Date d0 = displayCalendar.getTime();
            graph.getViewport().setMinX(d0.getTime());
            //Set maximum x boundary
            displayCalendar.set(Calendar.DAY_OF_MONTH, displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date dFinal = displayCalendar.getTime();
            Log.d("Activity GRAPH", "Maximum is " + dFinal.toString());
            graph.getViewport().setMaxX(dFinal.getTime());

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setBackgroundColor(Color.WHITE);

        } else if (weeklyView) {
            //Set minimum X boundary
            Date d0;
            while (weeklyDisplayCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                weeklyDisplayCalendar.add(Calendar.DAY_OF_WEEK, -1);

            }
            if (weeklyDisplayCalendar.get(Calendar.DAY_OF_MONTH) == userInformation.getLastAddedElement().getCustomDate().getDay()) {
                weeklyDisplayCalendar.add(Calendar.DAY_OF_WEEK, -1);

                d0 = weeklyDisplayCalendar.getTime();
                Log.d("Activity GRAPH", "Minimum is " + d0.toString());
                graph.getViewport().setMinX(d0.getTime());
                weeklyDisplayCalendar.add(Calendar.DAY_OF_WEEK, 1);
            } else {
                d0 = weeklyDisplayCalendar.getTime();
                Log.d("Activity GRAPH", "Minimum is " + d0.toString());
                graph.getViewport().setMinX(d0.getTime());
            }

            weeklyDisplayCalendar.add(Calendar.DAY_OF_MONTH, 6);
            Date dFinal = weeklyDisplayCalendar.getTime();
            Log.d("Activity GRAPH", "Maximum is " + dFinal.toString());
            graph.getViewport().setMaxX(dFinal.getTime());

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setBackgroundColor(Color.WHITE);

        }

       try{
           series.resetData(dataPoints);
       }
       catch(NullPointerException e){

       }
    }

    private void addUserInformation() {
        Calendar calendar = Calendar.getInstance();


        dataPoints = new DataPoint[userInformation.getArrayOfDays().size()];


        int i = 0;
        if (randomMonitoringView) {
            //Add randomly monitored data to the graph.
            DataPoint[] randomDataPoints = new DataPoint[userInformation.getArrayOfDays().size() ];
            for (DailyInformation day : userInformation.getArrayOfDays()) {
                calendar.set(day.getCustomDate().getYear(), day.getCustomDate().getMonthConstant(), day.getCustomDate().getDay(), 0, 0, 0);
                Date d = calendar.getTime();
                dataPoints[i] = new DataPoint(d, day.getConstantlyMonitoredDailyUsage());
                randomDataPoints[i] = new DataPoint(d, day.getRandomlyMonitoredDailyUsage());
                i++;
            }
            LineGraphSeries<DataPoint> randomSeries = new LineGraphSeries<DataPoint>(randomDataPoints);

            randomSeries.setTitle("Randomly Monitored Usage");
            randomSeries.setThickness(4);
            randomSeries.setColor(Color.rgb(2, 142, 81));
            randomSeries.setDrawDataPoints(true);
            randomSeries.setDataPointsRadius(8);
            graph.addSeries(randomSeries);
        } else if (overallAverageView) {
            //Add overall average monitored data to the graph.
            DataPoint[] averageDataPoints = new DataPoint[userInformation.getArrayOfDays().size()];
            for (DailyInformation day : userInformation.getArrayOfDays()) {
                calendar.set(day.getCustomDate().getYear(), day.getCustomDate().getMonthConstant(), day.getCustomDate().getDay(), 0, 0, 0);
                Date d = calendar.getTime();
                dataPoints[i] = new DataPoint(d, day.getConstantlyMonitoredDailyUsage());
                averageDataPoints[i] = new DataPoint(d, day.getAverageDailyUsage());
                i++;
            }
            LineGraphSeries<DataPoint> averageSeries = new LineGraphSeries<DataPoint>(averageDataPoints);

            averageSeries.setTitle("Average across all users");
            averageSeries.setThickness(4);
            averageSeries.setColor(Color.rgb(254, 189, 3));
            averageSeries.setDrawDataPoints(true);
            averageSeries.setDataPointsRadius(8);
            graph.addSeries(averageSeries);


        }


        series = new LineGraphSeries<DataPoint>(dataPoints);
        series.setTitle("Your Facebook usage");
        series.setThickness(4);
        series.setColor(Color.rgb(42, 42, 42));
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(8);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(
                "service_to_graph");
        this.registerReceiver(serviceReceiver, intentFilter);
        Log.d("Graph Activity", "ON START EXECUTED");
        Intent i = new Intent("graph_to_service");
        sendBroadcast(i);


    }


    protected void onStop() {
        // TODO Auto-generated method stub
        unregisterReceiver(serviceReceiver);
        super.onStop();
    }
}