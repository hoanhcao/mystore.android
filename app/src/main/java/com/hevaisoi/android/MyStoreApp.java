package com.hevaisoi.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.hevaisoi.android.webservice.WebServiceHelper;

/**
 * Created by ERP on 10/25/2016.
 */

public class MyStoreApp extends Application {
    private ConnectivityManager cMgr;
    private SharedPreferences preferences;
    private byte[] photoBytes;

    public byte[] getPhotoBytes() {
        return photoBytes;
    }

    public void setPhotoBytes(byte[] photoBytes) {
        this.photoBytes = photoBytes;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.cMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        preferences = getSharedPreferences(Constants.PCK_NAME, Context.MODE_PRIVATE);
        if (connectionPresent()) {
            new GetProductCatalogAsycn().execute();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public boolean connectionPresent() {
        NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
        if ((netInfo != null) && (netInfo.getState() != null)) {
            return netInfo.getState().equals(NetworkInfo.State.CONNECTED);
        }
        return false;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    private class GetProductCatalogAsycn extends AsyncTask<Integer, Void, String> {
        @Override
        protected void onPreExecute() {
            Log.d(Constants.LOG_TAG, "Begin get Product catalog json");
        }

        @Override
        protected String doInBackground(Integer... args) {
            WebServiceHelper wsHelper = WebServiceHelper.getInstance();
            String strJson = wsHelper.GetJsonFromUrl(Constants.GET_CATALOG_URL);
            return strJson;
        }

        @Override
        protected void onPostExecute(String strCatalog) {
            getPreferences().edit()
                       .putString(Constants.CAT_JSON_CATCHED_NAME, strCatalog)
                       .commit();
        }
    }
}
