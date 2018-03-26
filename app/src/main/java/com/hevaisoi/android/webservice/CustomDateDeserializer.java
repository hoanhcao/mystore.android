package com.hevaisoi.android.webservice;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ERP on 10/27/2016.
 */

public class CustomDateDeserializer implements JsonDeserializer<Date> {
    private final String VN_FORMAT = "yyMMddHHmmssSZ";

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String jsonString = json.getAsJsonPrimitive().getAsString();
        jsonString = jsonString.replaceAll("^/Date\\(" , "");
        jsonString = jsonString.substring(0, jsonString.length()-2);

        SimpleDateFormat dateFormat = new SimpleDateFormat(VN_FORMAT);
        try {
            return dateFormat.parse(jsonString);
        } catch (ParseException e) {
            Log.e("CustomDateDeserializer","Error",e);
        }
        return Calendar.getInstance().getTime();
    }
}
