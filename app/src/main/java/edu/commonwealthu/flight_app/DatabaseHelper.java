package edu.commonwealthu.flight_app;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private ArrayList<String> data;
    public static final String FLIGHT_TABLE = "FLIGHT_TABLE";
    public static final String COLUMN_FLIGHT_NUMBER     = "FLIGHT_NUMBER";
    public static final String COLUMN_DEPARTURE_CTRY    = "DEPARTURE_CTRY";
    public static final String COLUMN_ARRIVAL_CTRY      = "ARRIVAL_CTRY";
    public static final String COLUMN_DEPARTURE_AIRPORT = "DEPARTURE_AIRPORT";
    public static final String COLUMN_ARRIVAL_AIRPORT   = "ARRIVAL_AIRPORT";
    public static final String COLUMN_DEPARTURE_TERM    = "DEPARTURE_TERM";
    public static final String COLUMN_ARRIVAL_TERM      = "ARRIVAL_TERM";
    public static final String COLUMN_DEPARTURE_DATE    = "DEPARTURE_DATE";
    public static final String COLUMN_ARRIVAL_DATE      = "ARRIVAL_DATE";
    public static final String COLUMN_DEPARTURE_TIME    = "DEPARTURE_TIME";
    public static final String COLUMN_ARRIVAL_TIME      = "ARRIVAL_TIME";

    public DatabaseHelper(Context context, ArrayList<String> data) {
        super(context, "Flights.db", null, 1);
        this.data=data; //ArrayList that is used to transfer data
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //table creation use variables for easy modification
        String createTableStatement=
                "CREATE TABLE " + FLIGHT_TABLE +
                        "(" +
                              COLUMN_FLIGHT_NUMBER + " PRIMARY KEY, " +
                              COLUMN_DEPARTURE_CTRY    + " STRING, " +
                              COLUMN_ARRIVAL_CTRY      + " STRING, " +
                              COLUMN_DEPARTURE_AIRPORT + " STRING, " +
                              COLUMN_ARRIVAL_AIRPORT   + " STRING, " +
                              COLUMN_DEPARTURE_TERM    + " STRING, " +
                              COLUMN_ARRIVAL_TERM      + " STRING, " +
                              COLUMN_DEPARTURE_DATE    + " STRING, " +
                              COLUMN_ARRIVAL_DATE      + " STRING, " +
                              COLUMN_DEPARTURE_TIME    + " STRING, " +
                              COLUMN_ARRIVAL_TIME      + " STRING" +
                        ") ";

        db.execSQL(createTableStatement);
    }


    /**
     * Drops table and makes new one to keep functionality with newer versions
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists FLIGHT_TABLE");
    }

    /**
     * add a flight into the database
     * @return bool on if succeeded on adding flight to database
     */
    public boolean addFlightData()
    {
        for (int i = 0; i < data.size(); i++) {
            Log.d("TAG", "addFlightData: " + data.get(i));
        };

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        int i = 0;    //for loop used to save typing each index out
            while (i < data.size() - 1) {
                cv.put(COLUMN_FLIGHT_NUMBER, data.get(i++));
                cv.put(COLUMN_DEPARTURE_CTRY, data.get(i++));
                cv.put(COLUMN_ARRIVAL_CTRY, data.get(i++));
                cv.put(COLUMN_DEPARTURE_AIRPORT, data.get(i++));
                cv.put(COLUMN_ARRIVAL_AIRPORT, data.get(i++));
                cv.put(COLUMN_DEPARTURE_TERM, data.get(i++));
                cv.put(COLUMN_ARRIVAL_TERM, data.get(i++));;
                cv.put(COLUMN_DEPARTURE_DATE, data.get(i++));
                cv.put(COLUMN_ARRIVAL_DATE, data.get(i++));
                cv.put(COLUMN_DEPARTURE_TIME, data.get(i++));
                cv.put(COLUMN_ARRIVAL_TIME, data.get(i++));
            }

            long result = db.insert("FLIGHT_TABLE", null, cv);
            db.close();

        // Log the result of the insert
        if (result == -1) {
            Log.e("TAG", "Insert failed at index " + (i-1));
            return false;  // Return false if insertion fails
        }
        return true;
    }

    /**
     * retrieve data from database
     * @return
     */
    public String[][] returnData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor =
                db.rawQuery("SELECT * FROM " + FLIGHT_TABLE, null);
        String[][] tableData = new String[cursor.getCount()][cursor.getColumnCount()];

        int rowIndex=0;

        if (cursor.moveToFirst()) {
            do {
                for (int colIndex = 0; colIndex < cursor.getColumnCount(); colIndex++) {
                    tableData[rowIndex][colIndex] = cursor.getString(colIndex);
                }
                rowIndex++;
            } while (cursor.moveToNext());
        }

        cursor.close(); // Always close the cursor to free resources
        return tableData;
    }


}
