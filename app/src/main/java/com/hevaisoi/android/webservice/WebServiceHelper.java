package com.hevaisoi.android.webservice;

import android.util.Base64;
import android.util.Log;

import com.hevaisoi.android.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//import org.apache.commons.codec.binary.Base64;

/**
 * Created by ERP on 11/1/2016.
 */

public class WebServiceHelper {
    private static WebServiceHelper instance;
    private HttpURLConnection clientConnecttion;

    private WebServiceHelper() {

    }

    public static WebServiceHelper getInstance() {
        if (instance == null) {
            instance = new WebServiceHelper();
        }
        return instance;
    }

    public String GetJsonFromUrl(String strUrl) {
        try {
            URL svUrl = new URL(strUrl);
            try {
                String userCredentials = "webApi:no@pass^^!";
                String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.DEFAULT); //new_flag String(new_flag Base64().encode(userCredentials.getBytes()));
                clientConnecttion = (HttpURLConnection) svUrl.openConnection();
                clientConnecttion.setRequestProperty("Authorization", basicAuth);
                clientConnecttion.setRequestMethod("GET");
                clientConnecttion.setRequestProperty("charset", "utf-8");

                int status = clientConnecttion.getResponseCode();

                Log.d(Constants.LOG_TAG, "Status: " + status);
                switch (status) {
                    case 201:
                    case 200:
                        BufferedReader jsonStream = new BufferedReader(new InputStreamReader(clientConnecttion.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = jsonStream.readLine()) != null) {
                            result.append(line);
                        }
                        jsonStream.close();
                        return result.toString();
                    default:
                        Log.e(Constants.LOG_TAG, "Response message: " + clientConnecttion.getResponseMessage());

                        BufferedReader errorStream = new BufferedReader(new InputStreamReader(clientConnecttion.getErrorStream()));
                        StringBuilder strErr = new StringBuilder();
                        String errorLine;
                        while ((errorLine = errorStream.readLine()) != null) {
                            strErr.append(errorLine);
                        }
                        errorStream.close();
                        Log.e(Constants.LOG_TAG, "Error: " + strErr.toString());
                        return null;
                }

            } catch (IOException e) {
                Log.e(Constants.LOG_TAG, "Error on GetJsonFromUrl:", e);
            } finally {
                clientConnecttion.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(Constants.LOG_TAG, "Error on GetJsonFromUrl:", e);
        }
        return null;
    }
}
