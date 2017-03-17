package com.akexorcist.googledirection.sample;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by DARK on 17/3/2560.
 */

public class MyManage {

    private Context context;
    private MyOpenHelper myOpenHelper;
    private SQLiteDatabase sqLiteDatabase;


    public MyManage(Context context) {
        this.context = context;

        myOpenHelper = new MyOpenHelper(context);
        sqLiteDatabase = myOpenHelper.getReadableDatabase();

    }

    public long addUser(String strUser, String strPassword) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("User", strUser);
        contentValues.put("Password", strPassword);


        return sqLiteDatabase.insert("userTABLE", null, contentValues);
    }



}   // Main Class
