package com.websarva.wings.android.bestflightshot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SpecialDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "specialdata.db";
    private static final int DATABASE_VERSION = 1;

    public SpecialDatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE specialdata (");
        sb.append("id INTEGER PRIMARY KEY ,");
        sb.append("type TEXT,");
        sb.append("airline TEXT,");
        sb.append("time TEXT,");
        sb.append("info TEXT");
        sb.append(")");
        String sql = sb.toString();

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
