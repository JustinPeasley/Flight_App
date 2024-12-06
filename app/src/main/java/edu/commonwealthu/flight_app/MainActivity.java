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
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private int displayWidth;
    private int displayHeight;
    private GridLayout infoGrid;                //displays current flight data
    private RecyclerView recycle;               //recycle view that fills with flights
    private GridLayout.LayoutParams params;     //used to dynamically space gridlayout

    private int rows;                           //row count for gridview
    private Plane curFlight;

    private Adapter adapter;
    ArrayList<String> items;

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

        //get gridlayout that info will be displayed to
        infoGrid = findViewById(R.id.infoDisplayGrid);
        infoGrid.setRowCount(3);
        infoGrid.setPadding((displayWidth/3)- displayWidth/16,0,0,displayWidth/20);
        params = new GridLayout.LayoutParams();     //used for dynamically spacing grid
        params.width = displayWidth / infoGrid.getColumnCount();

        //get RecycleView
        recycle = findViewById(R.id.flightCards);
        cardViewCreation();

        //on button click calls add_view method to display AlertDialog for adding flight
        //information
        findViewById(R.id.addflight).setOnClickListener(view -> {
            try {
                add_flight();       //NOTE CHANGED MIGHT BREAK

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        findViewById(R.id.search).setOnClickListener(view -> {
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

        EditText input_field = add_flight_view.findViewById(R.id.input_field);
        Button confirm = add_flight_view.findViewById(R.id.confirm_button);

        //build alert dialog for adding a new flight
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(add_flight_view);
        AlertDialog dialog = builder.create();
        dialog.show();

        ////////---------------------------------------

        // Inflate the loading overlay layout
        LayoutInflater overlayInflater = getLayoutInflater();
        View loadingOverlay = overlayInflater.inflate(R.layout.loading_dimmer, null);

        // Find the dim background and spinner in the inflated layout
        View dimBackground = loadingOverlay.findViewById(R.id.dimBackground);
        ProgressBar loadingSpinner = loadingOverlay.findViewById(R.id.loadingSpinner);

        // Add the overlay to the current layout (e.g., a FrameLayout or RelativeLayout)
        CoordinatorLayout parentLayout = findViewById(R.id.main_screen); // Your parent layout
        parentLayout.addView(loadingOverlay);

        // Initially hide the dim background and spinner
        dimBackground.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);


        //---------------------------------------------

        confirm.setOnClickListener(v -> { // on confirm button click
            //make a plane object with new fnum call
            Plane newFlight = new Plane(input_field.getText().toString());

            dialog.dismiss(); //dismiss the alert dialog when confirm flight number

            // Show the loading spinner and dim background while processing data
            dimBackground.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> {
                curFlight= newFlight; //update the current flight to be displayed
                Log.d("TAG", "add_flight: about to store flight data!");
                //add flight to the database
                try {
                    DatabaseHelper databaseHelper =
                            new DatabaseHelper(MainActivity.this, curFlight.getInfo());
                    boolean b = databaseHelper.addFlightData();
                    Log.d("TAG", "add_flight: done adding data");
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
            window.setBackgroundDrawableResource(android.R.color.transparent); // Remove default dialog padding
            window.setLayout(displayWidth/2,displayHeight/4 );
        }
        add_flight_view.post(add_flight_view::requestLayout);
    }

    /**
     * Display information of current displayed flight
     * Used when adding flight
     */
    private void setInfo(ArrayList<String> newData, boolean backend) throws JSONException {
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

        //iterates starting a 1 to avoid Flight number
        for (int i = 1; i < data.size(); i++) {
            if(i == 7) i=i+2;
            TextView txt = new TextView(this);

            txt.setGravity(Gravity.CENTER);
            txt.setText(data.get(i));
            txt.setLayoutParams(params);
            txt.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )); // Define layout params for TextView
            //txt.setGravity(Gravity.CENTER); // Center align the text
            txt.setPadding(8, 8, displayWidth/8, 8); // Add padding for better readability

            infoGrid.addView(txt);
        }
    }

    /**
     *  creates cardView object to add to recycleView
     */
    @SuppressLint("Range")
    private void cardViewCreation(){
        Log.d("TAG", "cardViewCreation: creating card");
        items = new ArrayList<>(); //data to send to recycle view adapter to read

        DatabaseHelper db = new DatabaseHelper(MainActivity.this);
        String[][] parasable = db.returnData();
        for (int i = 0; i < parasable.length; i++) {
            Log.d("TAG", "cardViewCreation: loop data");
                items.add(parasable[i][0]); //flight num
                items.add(parasable[i][7]); //departure iata
                items.add(parasable[i][8]); //arrival   iata
                items.add(parasable[i][9]); //date
        }

        //if card is clicked run below
        recycle.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this, items, position ->{
            String selectedItem = items.get(position);
            int sfn = position * 4;  //cards take 4 values, need to scale for that
            String selectedFlightNumber = items.get(sfn);
            Log.d("TAG", "cardViewCreation: " + selectedFlightNumber);

            // Fetch flight details from the database using the flight number
            Cursor cursor = db.getFlightDataByFlightNumber(selectedFlightNumber);

            //used to store data from database on card click
            ArrayList<String> gatherData = new ArrayList<>();

            // Extract flight details from the cursor
            if (cursor != null && cursor.moveToFirst()) {
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_FLIGHT_NUMBER)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_DEPARTURE_CTRY)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_ARRIVAL_CTRY)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_DEPARTURE_AIRPORT)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_ARRIVAL_AIRPORT)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_DEPARTURE_TERM)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_ARRIVAL_TERM)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_DEPARTURE_IATA)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_ARRIVAL_IATA)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_DEPARTURE_DATE)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_ARRIVAL_DATE)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_DEPARTURE_TIME)));
                gatherData.add(cursor.getString(cursor.getColumnIndex(COLUMN_ARRIVAL_TIME)));
                setInfo(gatherData, true);
            }
            if (cursor != null) cursor.close(); // Close the cursor
        });
        recycle.setAdapter(adapter);
    }


    /**
     * inflates the main menu of the application
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * handle action bar clicks
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}