package edu.commonwealthu.flight_app;

import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_ARRIVAL_AIRPORT;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_ARRIVAL_CTRY;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_ARRIVAL_DATE;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_ARRIVAL_IATA;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_ARRIVAL_TERM;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_ARRIVAL_TIME;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_DEPARTURE_AIRPORT;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_DEPARTURE_CTRY;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_DEPARTURE_DATE;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_DEPARTURE_IATA;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_DEPARTURE_TERM;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_DEPARTURE_TIME;
import static edu.commonwealthu.flight_app.DatabaseHelper.COLUMN_FLIGHT_NUMBER;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;

import edu.commonwealthu.flight_app.databinding.ActivityMainBinding;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Flight_watch app for tracking flights through taking a Flight Number as input
 * Uses AeroBoxAPI to get all the data
 * app has use of a SQLite local database to store searched flights
 * @author Justin Peasley
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private int displayWidth;
    private int displayHeight;
    private GridLayout infoGrid;                //displays current flight data
    private RecyclerView recycle;               //recycle view that fills with flights
    private GridLayout.LayoutParams params;     //used to dynamically space gridlayout

    private int rows;                           //row count for gridview
    private Plane curFlight;                    //current selected flight for display

    private Adapter adapter;                    //Adapter for RecyclerView
    ArrayList<String> items;                    //used to store data from database

    /**
     * Creates the main view of the screen
     * ran on creation (when the app is made)
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edu.commonwealthu.flight_app.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);
        
        //current display information of the device
        displayWidth = getResources().getDisplayMetrics().widthPixels;
        displayHeight = getResources().getDisplayMetrics().heightPixels;


        //Lock screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get gridlayout that info on launch to display
        infoGrid = findViewById(R.id.infoDisplayGrid);
        infoGrid.setRowCount(3);
        infoGrid.setPadding((displayWidth/3)- displayWidth/16,0,0,displayWidth/20);
        params = new GridLayout.LayoutParams();     //used for dynamically spacing grid
        params.width = displayWidth / infoGrid.getColumnCount();
        recycle = findViewById(R.id.flightCards);   //get recyclerview then
        cardViewCreation();                         //create card views from database

        //on button click calls add_view method to display AlertDialog for adding flight
        findViewById(R.id.addflight).setOnClickListener(view -> {
            try {
                add_flight();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        //when
        findViewById(R.id.delete).setOnClickListener(view -> {
                TextView header_fnum = findViewById(R.id.header_fNum);
                DatabaseHelper db = new DatabaseHelper(MainActivity.this);
                db.deleteFlightData((String) header_fnum.getText(), adapter);
                cardViewCreation();
        });
    }

    /** Allow for user to add a flight
     *  Upon confirming a flight number the method will
     *  make a new Plane object this object will make an api call query for data on flight
     *  change curFlight to this new flight
     *  then will call setInfo() to display it
     *
     */
    private void add_flight() throws JSONException {
        //inflate window with ui to take in input
        LayoutInflater inflater = getLayoutInflater();
        View add_flight_view = inflater.inflate(R.layout.add_flight_menu, null);

        //get button references
        EditText input_field = add_flight_view.findViewById(R.id.input_field);
        Button confirm = add_flight_view.findViewById(R.id.confirm_button);

        //build alert dialog for adding a new flight
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(add_flight_view);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Inflate the loading overlay layout
        LayoutInflater overlayInflater = getLayoutInflater();
        View loadingOverlay = overlayInflater.inflate(R.layout.loading_dimmer, null);

        // Find the dim background and spinner in the inflated layout (loading display)
        View dimBackground = loadingOverlay.findViewById(R.id.dimBackground);
        ProgressBar loadingSpinner = loadingOverlay.findViewById(R.id.loadingSpinner);

        // Add the overlay to the current layout
        CoordinatorLayout parentLayout = findViewById(R.id.main_screen); // Your parent layout
        parentLayout.addView(loadingOverlay);

        // Initially hide the dim background and spinner
        dimBackground.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);

        // on confirm button click
        confirm.setOnClickListener(v -> {
            //make a plane object with new fnum call
            Plane newFlight = new Plane(input_field.getText().toString());

            dialog.dismiss(); //dismiss the alert dialog when confirm flight number

            // Show the loading spinner and dim background while processing data
            dimBackground.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.VISIBLE);

            //handler for halting code until api return (2 seconds)
            new Handler().postDelayed(() -> {
                curFlight= newFlight; //update the current flight to be displayed

                //add flight to the database
                try {
                    DatabaseHelper databaseHelper =
                            new DatabaseHelper(MainActivity.this, curFlight.getInfo());
                    boolean b = databaseHelper.addFlightData();

                    if (b)
                        Toast.makeText(MainActivity.this, "Flight added", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "error adding flight",
                            Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }

                try {   //set current flight info for display
                    setInfo(curFlight.getInfo(), false);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                cardViewCreation();

                // Hide the loading spinner and dim background after processing
                dimBackground.setVisibility(View.GONE);
                loadingSpinner.setVisibility(View.GONE);
            },2000); //delay 2000 ms, keeps ui thread active
        });

        // Set the dimensions of the dialog programmatically
        Window window = dialog.getWindow();
        if (window != null) {
            // Remove default dialog padding
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(displayWidth/2,displayHeight/4 );
        }
        add_flight_view.post(add_flight_view::requestLayout);
    }

    /**
     * Display information of current displayed flight
     *      * Used when adding flight
     * @param newData data set to be parsed and displayed to screen
     * @param backend boolean that determines if data came from backend or user input
     *                true: handles backend data through newData
     *                false: handles frontend data through curFlight
     * @throws JSONException
     */
    private void setInfo(ArrayList<String> newData, boolean backend) throws JSONException{
        ArrayList<String> data;
        if (!backend) {
            data = curFlight.getInfo(); //grabs ArrayList<String>
        } else {
            data = newData;
        }

        //clear past information and sets up for new info
        // dynamically set size here in case backend is changed to support more info
        infoGrid.removeAllViews();
        infoGrid.setRowCount(data.size()/2);

        //displays flight number in header
        TextView header_fnum = findViewById(R.id.header_fNum);
        header_fnum.setText(data.get(0));

        //display current flight information
        for (int i = 1; i < data.size(); i++) { //starts at 1 to avoid Flight number
            if(i == 7) i=i+2;
            TextView txt = new TextView(this);

            txt.setGravity(Gravity.CENTER);
            txt.setText(data.get(i));
            txt.setLayoutParams(params);
            txt.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            // Define layout params for TextView
            txt.setPadding(8, 8, displayWidth/8, 8);
            infoGrid.addView(txt);
        }
    }

    /**
     *  creates cardView objects to add to recycleView using recycleView adapter
     */
    @SuppressLint("Range")
    private void cardViewCreation(){

        items = new ArrayList<>(); //data to send to recycle view adapter to read

        //noinspection resource
        DatabaseHelper db = new DatabaseHelper(MainActivity.this);
        String[][] parasable = db.returnData();         //2d-array of database data
        for (int i = 0; i < parasable.length; i++) {    //get data for each CardView
                items.add(parasable[i][0]);  //flight num
                items.add(parasable[i][7]); //departure iata
                items.add(parasable[i][8]); //arrival   iata
                items.add(parasable[i][9]); //date
        }

        //if card is clicked fetch data and display in top of app
        recycle.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this, items, position ->{
            String selectedItem = items.get(position);
            int sfn = position * 4;  //cards take 4 values, need to scale for that
            String selectedFlightNumber = items.get(sfn);

            // Fetch flight details from the database using the flight number
            Cursor cursor = db.getFlightDataByFlightNumber(selectedFlightNumber);

            //used to store data from database on card click
            ArrayList<String> gatherData = new ArrayList<>();

            // Extract flight details from the cursor
            if (cursor != null && cursor.moveToFirst()) {
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_FLIGHT_NUMBER)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_DEPARTURE_CTRY)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_ARRIVAL_CTRY)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_DEPARTURE_AIRPORT)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_ARRIVAL_AIRPORT)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_DEPARTURE_TERM)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_ARRIVAL_TERM)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_DEPARTURE_IATA)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_ARRIVAL_IATA)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_DEPARTURE_DATE)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_ARRIVAL_DATE)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_DEPARTURE_TIME)));
                gatherData.add(cursor.getString(cursor.getColumnIndex
                        (COLUMN_ARRIVAL_TIME)));
                setInfo(gatherData, true);  //sets parsed data from database
            }
            if (cursor != null) cursor.close(); // Close the cursor
        });
        recycle.setAdapter(adapter); //refreshes RecyclerView
    }
}