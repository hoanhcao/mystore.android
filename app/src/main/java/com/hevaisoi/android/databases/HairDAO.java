package com.hevaisoi.android.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hevaisoi.android.Constants;
import com.hevaisoi.android.model.HairModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ERP on 6/12/2017.
 */

public class HairDAO implements BaseDAO<HairModel> {
    private SQLiteDatabase db;
//    private SQLiteStatement insertStatement;

//    private static String INSERT = "insert into " +
//            HairTable.TABLE_NAME + "(" +
//            HairTable.HairTableColumns.FILENAME + ", " +
//            HairTable.HairTableColumns.DESCRIPTION + ", " +
//            HairTable.HairTableColumns.WIDTHSCALE + ", " +
//            HairTable.HairTableColumns.HEIGHTSCALE + ", " +
//            HairTable.HairTableColumns.XSCALE + ", " +
//            HairTable.HairTableColumns.YSCALE + ")" +
//            "VALUES(?,?,?,?,?,?);";

    public HairDAO(SQLiteDatabase db) {
        this.db = db;
//        insertStatement = db.compileStatement(INSERT);
    }

    @Override
    public long save(HairModel obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(HairTable.HairTableColumns.FILENAME, obj.getFileName());
        contentValues.put(HairTable.HairTableColumns.DESCRIPTION, obj.getDescription());
        contentValues.put(HairTable.HairTableColumns.WIDTHSCALE, obj.getWidthScale());
        contentValues.put(HairTable.HairTableColumns.HEIGHTSCALE, obj.getHeightScale());
        contentValues.put(HairTable.HairTableColumns.XSCALE, obj.getxScale());
        contentValues.put(HairTable.HairTableColumns.YSCALE, obj.getyScale());

        long id = db.insert(HairTable.TABLE_NAME, null, contentValues);
        return id;
    }

    @Override
    public void update(HairModel obj) {

    }

    @Override
    public void delete(HairModel obj) {

    }

    @Override
    public HairModel get(long id) {
        Log.d(Constants.LOG_TAG, "Begin get data for Id: " + id );
        HairModel model = null;
        Cursor cursor = db.query(HairTable.TABLE_NAME, new String[]{
                        HairTable.HairTableColumns._ID,
                        HairTable.HairTableColumns.FILENAME,
                        HairTable.HairTableColumns.DESCRIPTION,
                        HairTable.HairTableColumns.WIDTHSCALE,
                        HairTable.HairTableColumns.HEIGHTSCALE,
                        HairTable.HairTableColumns.XSCALE,
                        HairTable.HairTableColumns.YSCALE},
                HairTable.HairTableColumns._ID + "=?",
                new String[]{String.valueOf(id)},null, null, null);
        cursor.moveToFirst();
        model = buildModelFromCursor(cursor);
        if (!cursor.isClosed()) {
            cursor.close();
        }
        Log.d(Constants.LOG_TAG, "End get data for Id: " + id );
        return model;
    }

    @Override
    protected void finalize() throws Throwable {
        if (db!=null && db.isOpen()){
            db.close();
        }
        super.finalize();
    }

    @Override
    public HairModel getFirst() {
        HairModel model = null;
       try{
           Cursor c = db.query(HairTable.TABLE_NAME,
                   new String[]{"min(" + HairTable.HairTableColumns._ID + ")"}, null, null,
                   null, null, null);
           c.moveToFirst();
           int rowID = c.getInt(0);
           Log.d(Constants.LOG_TAG, "Max id was got: " + rowID);
           model = get(rowID);
       }finally {

       }

        return model;
    }

    @Override
    public List<HairModel> getAll() {
        List<HairModel> models = new ArrayList<>();
        Cursor cursor = db.query(HairTable.TABLE_NAME, new String[]{
                        HairTable.HairTableColumns._ID,
                        HairTable.HairTableColumns.FILENAME,
                        HairTable.HairTableColumns.DESCRIPTION,
                        HairTable.HairTableColumns.WIDTHSCALE,
                        HairTable.HairTableColumns.HEIGHTSCALE,
                        HairTable.HairTableColumns.XSCALE,
                        HairTable.HairTableColumns.YSCALE},
                null,null,null, null, null);
//        Log.d(Constants.LOG_TAG, "Query getAll return: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                HairModel model = buildModelFromCursor(cursor);
                if (model != null) {
                    models.add(model);
                }
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return models;
    }

    public int deleteAll() {
        return db.delete(HairTable.TABLE_NAME, null, null);
    }

    private HairModel buildModelFromCursor(Cursor cursor) {
        HairModel model = null;
        if (cursor != null) {
            model = new HairModel();
            model.setId(cursor.getInt(0));
            model.setFileName(cursor.getString(1));
            model.setDescription(cursor.getString(2));
            model.setWidthScale(cursor.getFloat(3));
            model.setHeightScale(cursor.getFloat(4));
            model.setxScale(cursor.getDouble(5));
            model.setyScale(cursor.getDouble(6));
        }
        return model;
    }
}
