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
 *  sets up Flight database and handles all database creation and accessing of database
 *  if database is not on this machine yet sets it up with already exist doesn't recreate
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private ArrayList<String> data;
    private int colCount;
    private static final String FLIGHT_TABLE = "FLIGHT_TABLE";
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
    public static final String COLUMN_DEPARTURE_IATA    = "DEPARTURE_IATA";
    public static final String COLUMN_ARRIVAL_IATA      = "ARRIVAL_IATA";

    /**
     * constructor
     * takes in the current context and ArrayList of data
     * @param context the current context of the application
     * @param data    ArrayList of data to store
     */
    public DatabaseHelper(Context context, ArrayList<String> data) {
        super(context, "Flights.db", null, 1);
        this.data=data; //ArrayList that is used to transfer data
    }

    /**
     * second constructor only for reading data
     * not inherently meant to add data to the database
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, "Flights.db", null, 1);
        this.data=data; //ArrayList that is used to transfer data
    }

    /**
     * creates the table with the following columns with flight number as the key
     * @param db the database being created
     */
    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase db) {
        //table creation use variables for easy modification
        Log.d("TAG", "onCreate: creating table");
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
                              COLUMN_DEPARTURE_IATA    + " STRING, " +
                              COLUMN_ARRIVAL_IATA      + " STRING, " +
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
        if (data.size() == 5) return false; //if default values

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        int i = 0;    //iterate i for each data grab

        cv.put(COLUMN_FLIGHT_NUMBER,     data.get(i++));
        cv.put(COLUMN_DEPARTURE_CTRY,    data.get(i++));
        cv.put(COLUMN_ARRIVAL_CTRY,      data.get(i++));
        cv.put(COLUMN_DEPARTURE_AIRPORT, data.get(i++));
        cv.put(COLUMN_ARRIVAL_AIRPORT,   data.get(i++));
        cv.put(COLUMN_DEPARTURE_TERM,    data.get(i++));
        cv.put(COLUMN_ARRIVAL_TERM,      data.get(i++));
        cv.put(COLUMN_DEPARTURE_IATA,    data.get(i++));
        cv.put(COLUMN_ARRIVAL_IATA,      data.get(i++));
        cv.put(COLUMN_DEPARTURE_DATE,    data.get(i++));
        cv.put(COLUMN_ARRIVAL_DATE,      data.get(i++));
        cv.put(COLUMN_DEPARTURE_TIME,    data.get(i++));
        cv.put(COLUMN_ARRIVAL_TIME,      data.get(i++));


            long result = db.insert("FLIGHT_TABLE", null, cv);
            db.close();

        // Log the result of the insert
        if (result == -1) {
            return false;  // Return false if insertion fails
        }
        return true;
    }

    /**
     * retrieve data from database
     * gets entire dataset from database
     * @return 2d array of the whole table to parse for card view's
     */
    public String[][] returnData()
    {
        //build query string
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor =
                db.rawQuery("SELECT * FROM " + FLIGHT_TABLE, null);
        String[][] tableData = new String[cursor.getCount()][cursor.getColumnCount()];

        int rowIndex=0; //set to first position of database

        //get entire database dataset
        if (cursor.moveToFirst()) {
            do {
                for (int colIndex = 0; colIndex < cursor.getColumnCount(); colIndex++) {
                    tableData[rowIndex][colIndex] = cursor.getString(colIndex);
                }
                rowIndex++;
            } while (cursor.moveToNext());
        }
        cursor.close();   //Always close the cursor to free resources
        return tableData; //return entire database
    }

    /**
     * queries for a row based on Flight Number provided (used for CardView clicks)
     * @param flightNumber Flight number to search for in database
     * @return Row data
     */
    public Cursor getFlightDataByFlightNumber(String flightNumber){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql =
                "SELECT  * FROM " + FLIGHT_TABLE + " WHERE " + COLUMN_FLIGHT_NUMBER +
                        " = ?";

        return db.rawQuery(sql, new String[]{flightNumber});
    }

    /**
     * this method will delete the current selected flight
     * @param flightNumber flight number used to find row to delete
     */
    @SuppressLint("NotifyDataSetChanged")
    public void deleteFlightData(String flightNumber, Adapter adapter)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete the row where the flight number matches
        String whereClause = COLUMN_FLIGHT_NUMBER + " = ?";
        String[] whereArgs = new String[] { flightNumber };

        int rowsDeleted = db.delete(FLIGHT_TABLE, whereClause, whereArgs);
        adapter.notifyDataSetChanged();

        if (rowsDeleted > 0) {
            Log.d("Database", "Row with flight number " + flightNumber + " deleted.");
        } else {
            Log.d("Database", "No row with the given flight number found.");
        }
    }

    /**
     * returns the closest flight date to the current day
     * Currently not in use implementation at a later date
     * @return current flight date
     */
    public Cursor getClosestFlight() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM FLIGHT_TABLE " +
                "ORDER BY ABS(julianday(ARRIVAL_DATE) - julianday('now')) ASC " +
                "LIMIT 1";
        return db.rawQuery(query, null);
    }
}
