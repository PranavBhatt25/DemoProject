package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBConstant {

    /* DATABASE_VERSION=1 is  old version
    DATABASE_VERSION=2 is new version
     */
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DemoProject.sqlite";
    public static DBHelper dbHelper = null;
    public static SQLiteDatabase sqliteDatabase = null;

    public static void openDatabase(Context context) {
        try {
            if (dbHelper == null && sqliteDatabase == null) {
                dbHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
                sqliteDatabase = dbHelper.getWritableDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeDatabase() {
        if (dbHelper != null)
            dbHelper.close();

        if (sqliteDatabase != null)
            sqliteDatabase.close();

        dbHelper = null;
        sqliteDatabase = null;
    }

    public static final class TABLE_NAME {
        public static final String TBL_USER = "tblUser";
    }

    /*USER TABLE*/
    public static final class TBL_USER {
        public static final String ID = "Id";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String CREATED_AT = "created_at";
        public static final String IS_UPDATED = "is_updated";
    }

    public static final String CREATE_TBL_USER = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME.TBL_USER + "("
            + TBL_USER.ID + " TEXT NOT NULL, "
            + TBL_USER.NAME + " TEXT NOT NULL, "
            + TBL_USER.DESCRIPTION + " TEXT NOT NULL, "
            + TBL_USER.CREATED_AT + " TEXT NOT NULL, "
            + TBL_USER.IS_UPDATED + " TEXT NOT NULL );";

    public static final String[] COLUMNS_TBL_USER = new String[]{
            TBL_USER.ID, TBL_USER.NAME,
            TBL_USER.DESCRIPTION, TBL_USER.CREATED_AT,
            TBL_USER.IS_UPDATED,};/*USER TABLE*/
}
