package com.wclw.argueseye.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper instance;
    private final static String DB_NAME = "ArgouesDb";

    private final static int DB_VERSION = 1;
    private final static String TABLE_NAME ="BlockList";
    private final static String SCAN_TABLE ="ScanHistory";

    private final static String ID_COL ="id";
    private final static String URL_COL="url";
    private final static String DATE_COL ="date";

    private final static String ID_COL_SCAN_TBL ="id";
    private final static String URL_COL_SCAN_TBL ="url";
    private final static String DATE_COL_SCAN_TBL ="date";



    public DatabaseHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String creationQuery = "CREATE TABLE "+TABLE_NAME+"("
                + ID_COL +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                + URL_COL +" TEXT,"
                + DATE_COL +" TEXT)";

        String scanHistoryTablequery = "CREATE TABLE "+ SCAN_TABLE+"("
                + ID_COL_SCAN_TBL +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                + URL_COL_SCAN_TBL +" TEXT,"
                + DATE_COL +" TEXT)";

        sqLiteDatabase.execSQL(creationQuery);
        sqLiteDatabase.execSQL(scanHistoryTablequery);
    }

    public void addNewItem(String url){
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = this.getWritableDatabase();
            String currentDate = LocalDateTime.now().toString();

            ContentValues contentValues = new ContentValues();

            contentValues.put(URL_COL, url);
            contentValues.put(DATE_COL, currentDate);

            sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
            Log.d("DatabaseHelper", "successfully saved");

        }catch (Exception e){
            Log.d("DatabaseHelper", e.getMessage());
        }
    }

    public Cursor getBlockList(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = null;

        try{

            return sqLiteDatabase.query(
                    TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    DATE_COL + " DESC"
            );

        }catch (Exception e){
            Log.d("DatabaseHelper","Error"+e.getMessage());
            return cursor;
        }
    }

    public boolean onBlockList(String url){
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        boolean found = false;
        try{
            sqLiteDatabase = this.getReadableDatabase();
            String qurry = "SELECT "+ DATE_COL +" FROM "+ TABLE_NAME +" WHERE url =?";
            cursor = sqLiteDatabase.rawQuery(qurry,new String[]{url});

            if(cursor != null && cursor.moveToFirst()){
                found = true;
            }

        }catch (Exception e){
            Log.d("DatabaseHelper",e.getMessage());
        }
        finally {
            if (cursor != null) cursor.close();
        }
        return found;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
