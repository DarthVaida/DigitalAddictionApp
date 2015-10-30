package com.example.TaskMonitor;

import android.app.*;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Created by Alex on 25/11/2014.
 * Service to run in the background and monitor Facebook usage.
 * The service also reads and saves a UserInformation object to a local file.
 */
public class MainService extends Service {
    /*
    The Facebook activities that should be monitored.
     */
    public final String MONITORED_ACTIVITY_1 = "com.facebook.katana.activity.FbMainTabFrameworkActivity";
    public final String MONITORED_ACTIVITY_2 = "com.facebook.katana.LoginActivity";
    public final String MONITORED_ACTIVITY_3 = "com.facebook.messenger.neue.MainActivity";
    /*
    Number of daily random checks to be performed.
     */
    public final int NUMBER_OF_DAILY_CHECKS = 1000;
    final Handler continuousMonitoringHandler = new Handler();
    //Not used atm. To be deleted
    final Handler randomlyMonitoringHandler = new Handler();
    /*
    The frequency of pinging the user with pop-up messages.
     */
    public int WARNING_TIME_1 = 1800;
    public int WARNING_TIME_2 = 900;
    public int WARNING_TIME_3 = 300;
    /*
    Field that holds the amount of constantly monitored time in seconds
     */
    public int constantCounter = 0;
    /*
    Field that holds the amount of randomly monitored time in seconds
     */
    public int randomCounter = 0;
    /*
    The random generator
     */
    Random random;
    /*
    The UserInformation object.
     */
    UserInformation userInformation = null;
    private boolean randomCheckIsOn = false;
    /*
    Time of the next check
     */
    private Calendar alarmTime;
    /*
    Currect time
     */
    private Calendar currentTime;
    /*
    Index to indicate what the current scheduled random check is.
    */
    private int currentRandomIndex = 0;
    private AlarmManager alarmMgr;
    /*
    Runnable object to be run by a thread and perform period checks every 10 seconds.
     */
    final Runnable continuousMonitoringRunnable = new Runnable() {
        public void run() {

            try {
                while (true) {
                    /*
                    Check if the screen is on.
                     */
                    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                    if (powerManager.isScreenOn()) {
                        /*
                        Get the running task and activity
                         */
                        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        final List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                        ComponentName componentInfo = taskInfo.get(0).topActivity;
                        componentInfo.getPackageName();
                        continuousMonitoringHandler.post(new Runnable() {
                                                             @Override
                                                             public void run() {
                                                                 /*
                                                                 Check if Facebook is running
                                                                  */
                                                                 if (taskInfo.get(0).topActivity.getClassName().equals(MONITORED_ACTIVITY_1) || taskInfo.get(0).topActivity.getClassName().equals(MONITORED_ACTIVITY_2) || taskInfo.get(0).topActivity.getClassName().equals(MONITORED_ACTIVITY_3)) {
                                                                     constantCounter = constantCounter + 10;
                                                                     /*
                                                                     Provide messages based on the addiction status
                                                                      */
                                                                     if (userInformation.getLastAddedElement().getAddictionStatus() == 1) {
                                                                         if (constantCounter % WARNING_TIME_1 == 0) {
                                                                             Toast.makeText(getApplicationContext(), "You should consider taking a break", Toast.LENGTH_LONG).show();
                                                                             Log.d("Service", "You have been using Facebook for: " + constantCounter + " seconds. Consider taking a break");
                                                                         }
                                                                     } else if (userInformation.getLastAddedElement().getAddictionStatus() == 2) {
                                                                         if (constantCounter % WARNING_TIME_2 == 0) {
                                                                             Toast.makeText(getApplicationContext(), "You have been on Facebook more than the average person. Consider taking a break", Toast.LENGTH_LONG).show();
                                                                             Log.d("Service", "You have been using Facebook for: " + constantCounter + " seconds. Consider taking a break");
                                                                         }
                                                                     } else if (userInformation.getLastAddedElement().getAddictionStatus() == 3) {
                                                                         if (constantCounter % WARNING_TIME_3 == 0) {
                                                                             Toast.makeText(getApplicationContext(), "You might be suffering from digital addiction. You should take a break!", Toast.LENGTH_LONG).show();
                                                                             Log.d("Service", "You have been using Facebook for: " + constantCounter + " seconds. Consider taking a break");
                                                                         }
                                                                     }
                                                                     /*
                                                                     Perform the random check
                                                                      */
                                                                     if (randomCheckIsOn) {
                                                                         randomCounter = randomCounter + 10;
                                                                         /*
                                                                         Rank the hour.
                                                                          */
                                                                         userInformation.incrementRank(currentTime.get(Calendar.HOUR_OF_DAY));
                                                                     }


                                                                 } else {
                                                                     randomCheckIsOn = false;
                                                                 }
                                                             }
                                                         }
                        );


                        Thread.sleep(10000L);
                    }
                }
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
        }
    };
    /*
    Thread to run the runnable
     */
    private Thread continuousMonitoringThread = new Thread(continuousMonitoringRunnable);
    /*
    Broadcast receiver that is registered when connection is not possible.
    It listens for connection change intents and attempts to connect to the server when the connection is available.
     */
    BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();


            try {
                if (networkInfo.isConnected()) {
                    unregisterReceiver(connectivityReceiver);
                    new ConnectTask().execute(new ServerConnector());
                }
            } catch (NullPointerException e) {
                Log.d("Connectivity Receiver", "Not connected!");
            }

        }

    };
    /*
    Broadcast receiver that listens for Save intents
    Upon receipt it connects to the server and then saves the new information to a file
     */
    BroadcastReceiver saveReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            try {
                if (networkInfo.isConnected()) {

                    new ConnectTask().execute(new ServerConnector());


                }
            } catch (NullPointerException e) {
                IntentFilter saveIntentFilter = new IntentFilter(
                        "android.net.conn.CONNECTIVITY_CHANGE");
                registerReceiver(connectivityReceiver, saveIntentFilter);
            }
            //Save the UserInformation object to a file.
            saveToFile();
        }
    };

    /*
    Broadcast receiver that listens to Graph intents sent by the GraphScreen
    Upon receipt it provides a Service_to_graph intent containing the current UserInformation object
     */
    BroadcastReceiver graphReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            userInformation.addDailyInformation(constantCounter, randomCounter);
            Intent i = new Intent("service_to_graph").putExtra("ui", (Parcelable) userInformation);
            sendBroadcast(i);
        }
    };
    /*
    Broadcast receiver that listens to Profile intents sent by the GraphScreen
    Upon receipt it provides a Service_to_profile intent containing the current UserInformation object
         */
    BroadcastReceiver profileReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            userInformation.addDailyInformation(constantCounter, randomCounter);
            Intent i = new Intent("service_to_profile");
            Bundle extrasBundle = new Bundle();
            extrasBundle.putInt("index", currentRandomIndex);
            extrasBundle.putParcelable("ui", userInformation);
            i.putExtras(extrasBundle);
            sendBroadcast(i);
        }
    };
    /*
    Broadcast receiver that listens to Check intents
    Upon receipt it performs a random check and schedules the next one
     */
    BroadcastReceiver checkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Service Check Broadcast Receiver", "Received");
            //Check if Facebook is running and increase the rank of the hour slot when the user is online.
            randomCheckIsOn = true;
            Log.d("Service Check Broadcast Receiver", " " + randomCheckIsOn);
            //Set the next Alarm according to the randomized schedule.
            if (currentRandomIndex == NUMBER_OF_DAILY_CHECKS - 1) {
            //Reached the end of today's schedule. Create new randomized schedule for tomorrow.
                Log.d("Service ", "Reached the end of the schedule");
                currentRandomIndex = 0;
                userInformation.setRandomizedSchedule(createRandomizedSchedule());
                alarmTime.add(Calendar.DAY_OF_MONTH, 1);

            } else {
                currentRandomIndex++;
            }

            int intHours = (int) userInformation.getRandomizedSchedule()[currentRandomIndex];
            double doubleMinutes = ((userInformation.getRandomizedSchedule()[currentRandomIndex] - intHours) * 60);
            int intMinutes = (int) doubleMinutes;
            int intSeconds = (int) ((doubleMinutes - intMinutes) * 60);


            alarmTime.set(Calendar.HOUR_OF_DAY, intHours);
            alarmTime.set(Calendar.MINUTE, intMinutes);
            alarmTime.set(Calendar.SECOND, intSeconds);

            /*
            Send a Check intent to perform the next random check at the specified time.
             */
            Intent i = new Intent("CheckIntent");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainService.this, 1, i, 0);
            alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);

        }
    };



    @Override
    public void onCreate() {
        super.onCreate();
        try {
            /*
             * Read the userInformation object from the file. The first time the application will run, the file will not exist
             * and the constantCounter will remain 0.
             */
            FileInputStream fileInputStream = openFileInput("userinformation");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            userInformation = (UserInformation) objectInputStream.readObject();
            currentTime = Calendar.getInstance();
            /*
              Check if the last entry in the file corresponds to today's date. If yes, then keep incrementing the constantCounter.
              Otherwise, this means that monitoring has not started for today, therefore start from 0.
             */
        } catch (IOException e) {
            Log.d("Service - FILE LOAD", "File not found. Creating an empty UserInformation object");
           /*
           Create a new empty UserInformation object.
           Create a new average checking schedule, with a normal distribution around 12:00 PM
            */
            userInformation = new UserInformation(0, 0);
            userInformation.setRandomizedSchedule(createRandomizedSchedule());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /* Start a thread to continuously monitor Facebook activity. */
        continuousMonitoringThread.start();
        startForeground(NOTIFICATION_ID.FOREGROUND_SERVICE,
                getCompatNotification());

        /* Register receiver for Facebook check intents. The service sends intents to itseld that will run this code
        NUMBER_OF_DAILY_CHECKS times per day in order to check if the user is online.
        If found online, the hour slot when the event occurred will receive an increment in ranking. */
        IntentFilter checkIntentFilter = new IntentFilter(
                "CheckIntent");
        this.registerReceiver(checkReceiver, checkIntentFilter);
        IntentFilter graphIntentFilter = new IntentFilter(
                "graph_to_service");
        this.registerReceiver(graphReceiver, graphIntentFilter);

        IntentFilter profileIntentFilter = new IntentFilter(
                "profile_to_service");
        this.registerReceiver(profileReceiver, profileIntentFilter);

        /*
        Schedule first alarm
         */
        Intent i = new Intent("CheckIntent");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainService.this, 1, i, 0);
        alarmTime = Calendar.getInstance();
        currentTime = Calendar.getInstance();
        Boolean testBoolean = false;
        for (int iterator = 0; iterator < 100; iterator++) {

            int intHours = (int) userInformation.getRandomizedSchedule()[iterator];
            double doubleMinutes = ((userInformation.getRandomizedSchedule()[iterator] - intHours) * 60);
            int intMinutes = (int) doubleMinutes;
            int intSeconds = (int) ((doubleMinutes - intMinutes) * 60);


            alarmTime.set(Calendar.HOUR_OF_DAY, (intHours));
            alarmTime.set(Calendar.MINUTE, intMinutes);
            alarmTime.set(Calendar.SECOND, intSeconds);


            if (alarmTime.after(currentTime)) {
                if (!testBoolean) {
                    alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

                    alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
                    Log.d("Service ", "Initial Alarm scheduled for: " + userInformation.getRandomizedSchedule()[iterator] + " " + intHours + ": " + intMinutes + ": " + intSeconds);
                    currentRandomIndex = iterator;
                    Log.d("Service - random index= ", currentRandomIndex + "");
                    testBoolean = true;
                } else {
                    Log.d("Service - next schedule ", +intHours + ": " + intMinutes + ": " + intSeconds);
                }

            }
        }
        /* Register receiver for save intents. The Service sends intents that will run this code once a day
        to save the UserInformantion object to a file. */
        IntentFilter saveIntentFilter = new IntentFilter(
                "SaveIntent");
        this.registerReceiver(saveReceiver, saveIntentFilter);
        /*
        Send a daily intent to the running service to save the monitored data to a file.
        */
        Intent saveIntent = new Intent("SaveIntent");
        PendingIntent savePendingIntent = PendingIntent.getBroadcast(MainService.this, 0, saveIntent, 0);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 23);

        //Set a random time slot between 23:40 - 23:55 to connect to the server and save user information.
        //The 5 minute interval from 23:55 - 00:00 is allowed to handle connection timeouts.
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            c.set(Calendar.MINUTE, 55 - random.nextInt() * 15);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d("SERVICE - RANDOM GENERATOR", "No such algorithm");
            c.set(Calendar.MINUTE, 50);

        }

        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, savePendingIntent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Notification getCompatNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle("Facebook Monitor").setTicker("Facebook monitoring has started").setWhen(System.currentTimeMillis());
        Intent startIntent = new Intent(getApplicationContext(), HomeScreen.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 101, startIntent, 0);
        builder.setContentIntent(contentIntent);

        return builder.build();
    }

    @Override
    public void onDestroy() {
        this.saveToFile();
        continuousMonitoringThread.interrupt();
        unregisterReceiver(graphReceiver);
        unregisterReceiver(connectivityReceiver);
        unregisterReceiver(saveReceiver);
        unregisterReceiver(checkReceiver);
        unregisterReceiver(profileReceiver);
        super.onDestroy();


        Log.d("Service", "********* Destroyed *********");
    }

    public void saveToFile() {
        try {
            FileOutputStream fileOutputStream = openFileOutput("userinformation", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            try {
                userInformation.addDailyInformation(constantCounter, randomCounter);
            } catch (NullPointerException e) {
                e.printStackTrace();
                //Not used. To be deleted.
                Log.d("Service - FILE SAVE", "First Time Saving. Creating new UserInformation Object");
                userInformation = new UserInformation(constantCounter, randomCounter);
            }
            objectOutputStream.writeObject(userInformation);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        constantCounter = 0;
        randomCounter = 0;
        Log.d("Service - FILE SAVE", userInformation.toString());
    }

    private double[] createRandomizedSchedule() {
        double[] randomizedArray = new double[100];
        int startingIndex = 0;
        int endIndex = 0;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d("RANDOM GENERATOR", "No such algorithm");
        }
        try {
        /* List of hours sorted by rank. Contains a minimum of 1 element and a maximum of 5.
           If the rank of the last hour is equal to the rank of another hour,
           the maximum size will increase in order to contain all of the hours with the same rank.*/
            ArrayList<Integer> hoursWithTopRanks = userInformation.getHoursWithTopRanks();
        /*
        Temporary reference to the current hashmap containing the hours and their ranks.
         */
            HashMap<Integer, HourlyInformation> tempHash = userInformation.getHashmapOfRanks();
            //calculate sum of all ranks
            int sum = getSumOf(hoursWithTopRanks, tempHash);


            int numberOfGaussianChecks = NUMBER_OF_DAILY_CHECKS * 90 / 100;
            int stdd;
            try {
                stdd = 4 / hoursWithTopRanks.size();
            } catch (ArithmeticException e) {
                stdd = 4;
            }
            //assign percentages

            for (int i = 0; i < hoursWithTopRanks.size(); i++) {
                endIndex = endIndex + (tempHash.get(hoursWithTopRanks.get(i)).getRank()) * numberOfGaussianChecks / sum;
                for (int j = startingIndex; j < endIndex; j++) {
                    randomizedArray[j] = this.getGaussian(hoursWithTopRanks.get(i), stdd);
                }
                startingIndex = endIndex;

            }
        } catch (NullPointerException e) {
            Log.d("RANDOM GENERATOR", "First time creating the average schedule");
            for (int j = endIndex; j < NUMBER_OF_DAILY_CHECKS; j++) {
                randomizedArray[j] = this.getUniform(0, 24);
            }
        }
    //Sort the array
        Arrays.sort(randomizedArray);
        return randomizedArray;
    }
    //Get the next random number in a sequence of Gaussian distributed numbers
    private double getGaussian(double mean, double standardDeviation) {
        double temp = mean + random.nextGaussian() * standardDeviation;
        while (temp >= 24) {
            temp = temp - 24;
        }
        while (temp < 0) {
            temp = temp + 24;
        }
        return temp;
    }
    //Get the next random number in a sequence of uniformly distributed numbers
    private double getUniform(double min, double max) {
        double temp = min + random.nextDouble() * (max - min);
        while (temp >= 24) {
            temp = temp - 24;
        }
        while (temp < 0) {
            temp = temp + 24;
        }
        return temp;

    }

    private int getSumOf(ArrayList<Integer> arrayList, HashMap<Integer, HourlyInformation> hashMap) {
        int sum = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            sum = sum + hashMap.get(arrayList.get(i)).getRank();
        }
        return sum;
    }


    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    /*
    Inner Class to connect to the server
     */
    private class ConnectTask extends AsyncTask<ServerConnector, Long, JSONObject> {


        @Override
        protected JSONObject doInBackground(ServerConnector... params) {


            return params[0].GetAverage(constantCounter);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                userInformation.getLastAddedElement().setAverageDailyUsage(jsonObject.getInt("AVG(Seconds)"));
                Log.d("Connect Task", "The Average Facebook Usage for today is: " + userInformation.getLastAddedElement().getAverageDailyUsage());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.d("Connect Task", "Server unavailable");
            }


        }


    }


}
