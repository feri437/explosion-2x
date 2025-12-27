package com.gb90.smart2x;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Db extends SQLiteOpenHelper {
    private static final String DB_NAME = "gb90.db";
    private static final int DB_VER = 1;

    public Db(Context ctx){
        super(ctx, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS multipliers ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "ts INTEGER NOT NULL,"+
                "value REAL NOT NULL"+
                ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ts ON multipliers(ts)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no-op
    }

    public void insert(double v, long ts){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO multipliers(ts,value) VALUES(?,?)", new Object[]{ts, v});
    }

    public void clearAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM multipliers");
    }

    public List<Double> lastN(int n){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT value FROM multipliers ORDER BY id DESC LIMIT ?", new String[]{String.valueOf(n)});
        List<Double> out = new ArrayList<>();
        try{
            while(c.moveToNext()) out.add(c.getDouble(0));
        } finally { c.close(); }
        return out; // newest first
    }

    public long count(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM multipliers", null);
        try{
            if(c.moveToFirst()) return c.getLong(0);
            return 0;
        } finally { c.close(); }
    }
}
