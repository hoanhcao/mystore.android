package com.hevaisoi.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ERP on 10/5/2017.
 */

public abstract class BaseActivity extends AppCompatActivity{
//    private SinchService.SinchServiceInterface mSinchServiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Intent intent = new Intent(this, SinchService.class);
        getApplicationContext().bindService(intent, this, BIND_AUTO_CREATE);
        Log.d(Constants.LOG_TAG, "Sinch service created");*/
    }

    /*@Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = (SinchService.SinchServiceInterface) iBinder;
            onServiceConnected();
            Log.d(Constants.LOG_TAG, "Sinch service connected");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = null;
            onServiceDisconnected();
            Log.d(Constants.LOG_TAG, "Sinch service disconnected");
        }
    }

    protected void onServiceConnected() {
        // for subclasses
    }

    protected void onServiceDisconnected() {
        // for subclasses
    }

    protected SinchService.SinchServiceInterface getSinchServiceInterface() {
        return mSinchServiceInterface;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
    }

    */
}
