package com.hevaisoi.android.webservice;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hevaisoi.android.Constants;

/**
 * Created by ERP on 9/18/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(Constants.LOG_TAG, "From: " + remoteMessage.getFrom());
        Log.d(Constants.LOG_TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        super.onMessageReceived(remoteMessage);
    }

}