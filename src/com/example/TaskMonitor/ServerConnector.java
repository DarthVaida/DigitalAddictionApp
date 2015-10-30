package com.example.TaskMonitor;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
/**
 * Created by Alex on 04/03/2015.
 * Class to connect to the server
 */
public class ServerConnector {

/*
Send an Http request to the server and return the server's output as a JSONObject
 */
    public JSONObject GetAverage(int facebookUsage)
    {
        /*
        Http request with appended parameter. The facebookUsagae parameter represents today's usage time.
        This value will be added to the server database and the overall average will be provided.
         */
        String url = "http://192.168.0.7/getAverage.php?number="+facebookUsage;
        HttpEntity addictionHttpEntity = null;
        /*
        Get the http response
         */
        try
        {
            DefaultHttpClient addictionServerHttpClient = new DefaultHttpClient();
            HttpGet addicionHttpGet = new HttpGet(url);
            HttpResponse addictionHttpResponse = addictionServerHttpClient.execute(addicionHttpGet);
            addictionHttpEntity = addictionHttpResponse.getEntity();


        } catch (ClientProtocolException e) {
            e.printStackTrace();       
        } catch (IOException e) {
            e.printStackTrace();
        }     
        JSONObject jsonObject = null;

        if (addictionHttpEntity != null) {
            try {
                String addictionResponse = EntityUtils.toString(addictionHttpEntity);
                Log.d("Entity Response  : ", addictionResponse);
                jsonObject = new JSONObject(addictionResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;


    }
}
