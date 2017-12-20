package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import models.AllDataClass;


/**
 * Created by wp2android on 7/12/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DBConstant.CREATE_TBL_USER);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
//                try {
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_USER + " ADD COLUMN " + DBConstant.TBL_USER.USER_STATE_ID + ";");
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_USER + " ADD COLUMN " + DBConstant.TBL_USER.USER_CITY_ID + ";");
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_USER + " ADD COLUMN " + DBConstant.TBL_USER.USER_VILLAGE_ID + ";");
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_USER + " ADD COLUMN " + DBConstant.TBL_USER.USER_CITY_NAME + ";");
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_USER + " ADD COLUMN " + DBConstant.TBL_USER.USER_VILLAGE_NAME + ";");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                // we want both updates, so no break statement here...
            case 2:
//                try {
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_COMMENTS + " ADD COLUMN " + DBConstant.TBL_COMMENTS.COM_CITY_ID + ";");
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_COMMENTS + " ADD COLUMN " + DBConstant.TBL_COMMENTS.COM_VILLAGE_ID + ";");
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_COMMENTS + " ADD COLUMN " + DBConstant.TBL_COMMENTS.COM_CITY_NAME + ";");
//                    db.execSQL("ALTER TABLE " + DBConstant.TABLE_NAME.TBL_COMMENTS + " ADD COLUMN " + DBConstant.TBL_COMMENTS.COM_VILLAGE_NAME + ";");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

        }
    }

    /* INSERT IN USER TABLE*/
    public long insertUserInfo(SQLiteDatabase db, AllDataClass allDataClass) {
        ContentValues cv = new ContentValues();
        cv.put(DBConstant.TBL_USER.ID, allDataClass.getId());
        cv.put(DBConstant.TBL_USER.NAME, allDataClass.getName());
        cv.put(DBConstant.TBL_USER.DESCRIPTION, allDataClass.getDescription());
        cv.put(DBConstant.TBL_USER.CREATED_AT, allDataClass.getCreated_at());
        cv.put(DBConstant.TBL_USER.IS_UPDATED, allDataClass.getIs_Updated());


        if (!allDataClass.getId().equals("") && checkPostIdExist(allDataClass.getId())) {
            return db.update(DBConstant.TABLE_NAME.TBL_USER, cv, DBConstant.TBL_USER.ID + "=?", new String[]{allDataClass.getId()});
        } else {
            long insterUserInfo = db.insert(DBConstant.TABLE_NAME.TBL_USER, null, cv);
            Log.e("TBL_USER :- ", "" + insterUserInfo);
            return insterUserInfo;
        }
    }

    public ArrayList<AllDataClass> getDataForSync(SQLiteDatabase db) {
        ArrayList<AllDataClass> arrayList = new ArrayList<>();
        Cursor cursor = null;
        cursor = db.query(DBConstant.TABLE_NAME.TBL_USER, DBConstant.COLUMNS_TBL_USER, DBConstant.TBL_USER.IS_UPDATED + "=?", new String[]{"0"}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.ID));
                    String name = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.NAME));
                    String description = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.DESCRIPTION));
                    String createdDate = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.CREATED_AT));
                    String is_updated = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.IS_UPDATED));

                    AllDataClass allDataClass = new AllDataClass();
                    allDataClass.setId(id);
                    allDataClass.setName(name);
                    allDataClass.setDescription(description);
                    allDataClass.setCreated_at(createdDate);
                    allDataClass.setIs_Updated(is_updated);

                    arrayList.add(allDataClass);
                } while (cursor.moveToNext());
            }
        }

        return arrayList;
    }

    public void updateRecordsForSync(SQLiteDatabase db) {
//        ContentValues cv = new ContentValues();
//        cv.put(DBConstant.TBL_USER.IS_UPDATED, "1");
//        int update = db.update(DBConstant.TABLE_NAME.TBL_USER, cv, DBConstant.TBL_USER.IS_UPDATED + "=?", new String[]{"0"});

        db.delete(DBConstant.TABLE_NAME.TBL_USER, DBConstant.TBL_USER.IS_UPDATED + "=?", new String[]{"0"});
    }


    public Cursor getUserInfo(SQLiteDatabase db) {
        return db.query(DBConstant.TABLE_NAME.TBL_USER, DBConstant.COLUMNS_TBL_USER, null, null, null, null, null);
    }


    public Cursor getAppVersion(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT AppVersion FROM tblUser ;", null);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return cursor;
    }

    //check Post is exists in Post Table
    public boolean checkPostIdExist(String postId) {
        boolean isExists = false;
        Cursor cursor = DBConstant.sqliteDatabase.rawQuery("select * from " + "tblUser"
                + " where " + DBConstant.TBL_USER.ID + " = '" + postId + "' ;", null);

        if (cursor != null && cursor.getCount() > 0)
            isExists = true;
        else
            isExists = false;

        if (cursor != null && !cursor.isClosed())
            cursor.close();
        return isExists;
    }

    public Cursor getAllData(SQLiteDatabase db, String postId) {
        //   return db.query(DBConstant.TABLE_NAME.TBL_USER, DBConstant.COLUMNS_TBL_USER, null, null, null, null,null);
        Cursor cursor = DBConstant.sqliteDatabase.rawQuery("select * from " + "tblUser;", null);

        return cursor;

    }
}
