package com.mrdevilex.smssaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sms_saver.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "sms";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "sender TEXT," +
            "body TEXT," +
            "date INTEGER," +
            "UNIQUE(sender, body, date))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void insertSms(SmsModel sms) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("sender", sms.getSender());
        cv.put("body", sms.getBody());
        cv.put("date", sms.getDate());
        db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public List<SmsModel> getAllSms() {
        List<SmsModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT sender, body, date FROM " + TABLE +
            " ORDER BY date DESC", null);
        while (cursor.moveToNext()) {
            list.add(new SmsModel(cursor.getString(0), cursor.getString(1), cursor.getLong(2)));
        }
        cursor.close();
        db.close();
        return list;
    }
}
