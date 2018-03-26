package com.hevaisoi.android.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ERP on 6/9/2017.
 */

public class AppDataBaseHelper extends SQLiteOpenHelper {
    public AppDataBaseHelper(Context context){
        super(context, DataConstant.DATABASE_NAME, null, DataConstant.DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HairTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HairTable.onUpgrade(db, oldVersion, newVersion);
    }
}

