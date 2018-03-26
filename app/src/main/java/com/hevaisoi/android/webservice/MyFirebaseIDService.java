package com.hevaisoi.android.webservice;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hevaisoi.android.Constants;

/**
 * Created by ERP on 9/18/2017.
 */

public class MyFirebaseIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(Constants.LOG_TAG, "Refresh token: " + refreshToken);
        sendRegistrationToServer(refreshToken);
    }

    private void sendRegistrationToServer(String token) {

    }
}
