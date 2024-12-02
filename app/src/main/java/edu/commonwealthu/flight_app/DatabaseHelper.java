package edu.commonwealthu.flight_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;

//called first time access database.
public class DatabaseHelper extends SQLiteOpenHelper {
    private ArrayList<String> data;

    public DatabaseHelper(Context context, ArrayList<String> data) {
        super(context, "Flights.db", null, 1);
        this.data=data; //ArrayList that is used to transfer data
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement="CREATE TABLE FLIGHT_TABLE (FLIGHT_NUMBER STRING " +
                "PRIMARY KEY AUTOINCREMENT, departureAirport STRING, departureTime " +
                "STRING, arrivalAirport STRING, arrivalTime, STRING)";

        db.execSQL(createTableStatement);
    }

    //if app is updated help older versions still be operable
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists FLIGHT_TABLE");
    }

    public boolean insertFlightData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //for loop used to save typing each index out
        for (int i = 0; i < data.size(); i++) {
            contentValues.put("FLIGHT_NUMBER",   data.get(i++));
            contentValues.put("departureAirport",data.get(i++));
            contentValues.put("departureTime",   data.get(i++));
            contentValues.put("arrivalAirport",  data.get(i++));
            contentValues.put("arrivalTime",     data.get(i));
        }
        long result = db.insert("FLIGHT_TABLE", null, contentValues);
        if(result ==-1) return false;
        else {return true;}
    }

    public boolean updateFlightData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //for loop used to save typing each index out
        for (int i = 1; i < data.size(); i++) {
            contentValues.put("departureAirport",data.get(i++));
            contentValues.put("departureTime",   data.get(i++));
            contentValues.put("arrivalAirport",  data.get(i++));
            contentValues.put("arrivalTime",     data.get(i));
        }
        Cursor cursor = db.rawQuery("Select * from FLIGHT_TABLE where FLIGHT_NUMBER=?",
                new String[]{data.get(0)});

        if(cursor.getCount()>0) {
            long result = db.update("FLIGHT_TABLE", contentValues, "FLIGHT_NUMBER=?",
                    new String[]{data.get(0)}); //get name
            if (result == -1) return false;
            else {
                return true;
            }
        } else {return false;}
    }

}
