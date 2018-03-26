package com.hevaisoi.android.databases;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.hevaisoi.android.Constants;

/**
 * Created by ERP on 6/12/2017.
 */

public final class HairTable {
    public static String TABLE_NAME = "hair";

    public static class HairTableColumns implements BaseColumns {
        public static final String FILENAME = "file_name";
        public static final String DESCRIPTION = "description";
        public static final String WIDTHSCALE = "width_scale";
        public static final String HEIGHTSCALE = "height_scale";
        public static final String XSCALE = "x_scale";
        public static final String YSCALE = "y_scale";
    }

    /* private void insertHairEntity() {
         SQLiteDatabase db = sqlHelper.getWritableDatabase();

         Log.d("HeVaiSoi", String.format("Stored db version: %1$d, current version: %2$d", db.getVersion(), DataConstant.DATABASE_VERSION));

         if (db.getVersion() != DataConstant.DATABASE_VERSION) {
             HairDAO hairDAO = new_flag HairDAO(db);
             int deleted = hairDAO.deleteAll();
             Log.d("HeVaiSoi", String.format("Deleted hair entities: %1$d", deleted));

             //Initial data for Hair entity
             HairModel model = new_flag HairModel();
             model.setFileName("sort_circle_hair");
             model.setWidth(173);
             model.setHeight(171);
             model.setWidthScale(0.6f);
             model.setHeightScale(0.17f);
             model.setxScale(0.19);
             model.setyScale(0.34);

             hairDAO.save(model);
         }
     }*/
    private static void initialHairEntity(SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(HairTable.HairTableColumns.FILENAME, "red_circle_hair");
        contentValues.put(HairTableColumns.DESCRIPTION, "Khánh đỏ");
        contentValues.put(HairTable.HairTableColumns.WIDTHSCALE, 0.58f);
        contentValues.put(HairTable.HairTableColumns.HEIGHTSCALE, 0.15f);
        contentValues.put(HairTable.HairTableColumns.XSCALE, 0.23);
        contentValues.put(HairTable.HairTableColumns.YSCALE, 0.50);
        long id = db.insert(HairTable.TABLE_NAME, null, contentValues);
        Log.d(Constants.LOG_TAG, "Inserted hair: " + id + "-" + "red_circle_hair");

        contentValues.clear();
        contentValues.put(HairTable.HairTableColumns.FILENAME, "man_flower_hair");
        contentValues.put(HairTableColumns.DESCRIPTION, "Mấn kết hoa");
        contentValues.put(HairTable.HairTableColumns.WIDTHSCALE, 0.41f);
        contentValues.put(HairTable.HairTableColumns.HEIGHTSCALE, 0.11f);
        contentValues.put(HairTable.HairTableColumns.XSCALE, 0.12);
        contentValues.put(HairTable.HairTableColumns.YSCALE, 0.26);
        id = db.insert(HairTable.TABLE_NAME, null, contentValues);
        Log.d(Constants.LOG_TAG, "Inserted hair: " + id + "-" + "man_flower_hair");

        contentValues.clear();
        contentValues.put(HairTable.HairTableColumns.FILENAME, "sort_circle_hair");
        contentValues.put(HairTableColumns.DESCRIPTION, "Khánh trắng");
        contentValues.put(HairTable.HairTableColumns.WIDTHSCALE, 0.6f);
        contentValues.put(HairTable.HairTableColumns.HEIGHTSCALE, 0.17f);
        contentValues.put(HairTable.HairTableColumns.XSCALE, 0.19);
        contentValues.put(HairTable.HairTableColumns.YSCALE, 0.34);
        id = db.insert(HairTable.TABLE_NAME, null, contentValues);
        Log.d(Constants.LOG_TAG, "Inserted hair: " + id + "-" + "sort_circle_hair");

        contentValues.clear();
        contentValues.put(HairTable.HairTableColumns.FILENAME, "long_hair_crown");
        contentValues.put(HairTableColumns.DESCRIPTION, "Tóc dài");
        contentValues.put(HairTable.HairTableColumns.WIDTHSCALE, 0.44f);
        contentValues.put(HairTable.HairTableColumns.HEIGHTSCALE, 0.31f);
        contentValues.put(HairTable.HairTableColumns.XSCALE, 0.04);
        contentValues.put(HairTable.HairTableColumns.YSCALE, 0.10);
        id = db.insert(HairTable.TABLE_NAME, null, contentValues);
        Log.d(Constants.LOG_TAG, "Inserted hair: " + id + "-" + "long_hair_crown");

        contentValues.clear();
        contentValues.put(HairTable.HairTableColumns.FILENAME, "normal_hair");
        contentValues.put(HairTableColumns.DESCRIPTION, "Tóc ngắn");
        contentValues.put(HairTable.HairTableColumns.WIDTHSCALE, 0.46f);
        contentValues.put(HairTable.HairTableColumns.HEIGHTSCALE, 0.159f);
        contentValues.put(HairTable.HairTableColumns.XSCALE, 0.09);
        contentValues.put(HairTable.HairTableColumns.YSCALE, 0.09);
        id = db.insert(HairTable.TABLE_NAME, null, contentValues);
        Log.d(Constants.LOG_TAG, "Inserted hair: " + id + "-" + "normal_hair");

        contentValues.clear();
        contentValues.put(HairTable.HairTableColumns.FILENAME, "long_hair_flower");
        contentValues.put(HairTableColumns.DESCRIPTION, "Tóc kết hoa");
        contentValues.put(HairTable.HairTableColumns.WIDTHSCALE, 0.37f);
        contentValues.put(HairTable.HairTableColumns.HEIGHTSCALE, 0.26f);
        contentValues.put(HairTable.HairTableColumns.XSCALE, 0.1);
        contentValues.put(HairTable.HairTableColumns.YSCALE, 0.08);
        id = db.insert(HairTable.TABLE_NAME, null, contentValues);
        Log.d(Constants.LOG_TAG, "Inserted hair: " + id + "-" + "long_hair_flower");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public static void onCreate(SQLiteDatabase db) {
        try {
            StringBuilder sqlCreate = new StringBuilder();
            sqlCreate.append("CREATE TABLE " + HairTable.TABLE_NAME + "( ");
            sqlCreate.append(HairTableColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
            sqlCreate.append(HairTableColumns.FILENAME + " TEXT, ");
            sqlCreate.append(HairTableColumns.DESCRIPTION + " TEXT, ");
            sqlCreate.append(HairTableColumns.WIDTHSCALE + " REAL, ");
            sqlCreate.append(HairTableColumns.HEIGHTSCALE + " REAL, ");
            sqlCreate.append(HairTableColumns.XSCALE + " REAL, ");
            sqlCreate.append(HairTableColumns.YSCALE + " REAL ");
            sqlCreate.append(");");
            Log.d(Constants.LOG_TAG, "Create query: " + sqlCreate.toString());
            db.execSQL(sqlCreate.toString());

            initialHairEntity(db);
        } catch (Exception ex) {
            Log.e(Constants.LOG_TAG, "Cannot initialized for Hair entity");
        } finally {
        }
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
            HairTable.onCreate(db);
        } catch (Exception ex) {
            Log.e(Constants.LOG_TAG, ex.getMessage());
        } finally {
        }

    }
}
