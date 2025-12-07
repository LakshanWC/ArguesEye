package com.wclw.argueseye.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDateTime;

public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "ArgouesDb";
    private final static int DB_VERSION = 1;
    private final static String TABLE_NAME ="BlockList";

    private final static String ID_COL ="id";
    private final static String URL_COL="url";
    private final static String DATE_COL ="date";

    public DatabaseHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String creationQuery = "CREATE TABLE "+TABLE_NAME+"("
                + ID_COL +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                + URL_COL +" TEXT,"
                + DATE_COL +"TEXT)";

        sqLiteDatabase.execSQL(creationQuery);
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
        finally {
            if(sqLiteDatabase !=null) sqLiteDatabase.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
