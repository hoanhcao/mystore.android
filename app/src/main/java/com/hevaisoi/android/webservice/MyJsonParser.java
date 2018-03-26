package com.hevaisoi.android.webservice;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.hevaisoi.android.Constants;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.List;

/**
 * Created by ERP on 10/25/2016.
 */

public class MyJsonParser<T> {

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public List<T> parseJSON(String response, Type collectionType) {
        if (response == null) return null;
        List<T> result = null;
        Log.d(Constants.LOG_TAG, "Begin parsing from: " + response);
        GsonBuilder builder = new GsonBuilder()
                .serializeNulls()
                .setDateFormat(DateFormat.LONG)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting();

        Gson prdGson = builder.create();
        try {
            result = prdGson.fromJson(response, collectionType);
        } catch (JsonSyntaxException ex) {
            Log.e(Constants.LOG_TAG, "Error when parse json: " + ex.getMessage());
        }
        Log.d(Constants.LOG_TAG, "End parsing");
        return result;
    }
}

