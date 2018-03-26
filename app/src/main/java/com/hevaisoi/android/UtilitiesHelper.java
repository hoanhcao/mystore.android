package com.hevaisoi.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ERP on 6/28/2017.
 */

public class UtilitiesHelper {
    public static AdRequest getAdmobRequest() {
        return new AdRequest.Builder()
                /// TODO: Need remove code test when release
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("5942D8A674554949D75CFA9E935441D6")
                .addTestDevice("E62478D191DB907463136069ADBFD903")
                .build();
    }
    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void showMessage(Context context, String strMessage) {
        Toast myToast = Toast.makeText(context,
                strMessage,
                Toast.LENGTH_LONG);
        myToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        myToast.show();
    }

    public static Bitmap retrieveBitmap(String urlString) {
        Log.d(Constants.LOG_TAG, "making HTTP trip for image:" + urlString);
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlString);
            // NOTE, be careful about just doing "url.openStream()"
            // it's a shortcut for openConnection().getInputStream() and doesn't set timeouts
            // (the defaults are "infinite" so it will wait forever if endpoint server is down)
            // do it properly with a few more lines of code . . .
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            bitmap = BitmapFactory.decodeStream(conn.getInputStream());
        } catch (MalformedURLException e) {
            Log.e(Constants.LOG_TAG, "Exception loading image, malformed URL", e);
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Exception loading image, IO error", e);
        }
        return bitmap;
    }
}
