package edu.commonwealthu.flight_app;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;

import edu.commonwealthu.flight_app.databinding.ActivityMainBinding;

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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

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

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //current display information of the device
        displayWidth = getResources().getDisplayMetrics().widthPixels;
        displayHeight = getResources().getDisplayMetrics().heightPixels;

        super.onCreate(savedInstanceState);

        edu.commonwealthu.flight_app.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        populate_recycle();

        //on button click calls add_view method to display AlertDialog for adding flight
        //information
        findViewById(R.id.addflight).setOnClickListener(view -> {
            try {
                add_flight();       //NOTE CHANGED MIGHT BREAK

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * populates the recycle view of the app and gathers data from the database to display
     */
    private void populate_recycle()
    {
        items = new ArrayList<>();
        items.add("first card");
        items.add("first card");
        items.add("2");
        items.add("second card");
        items.add("second card");
        items.add("3");
        items.add("third card");
        items.add("third card");
        items.add("4");

        recycle.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this, items);
        recycle.setAdapter(adapter);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(add_flight_view);
        AlertDialog dialog = builder.create();
        dialog.show();

        confirm.setOnClickListener(v -> { // on confirm button click
            //make a plane object with new fnum call
            Plane newFlight = null;
            newFlight = new Plane(input_field.getText().toString());


            try {    //buffer to wait for the call back
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            curFlight= newFlight; //update the current flight to be displayed

            //add flight to the database
            try {
                DatabaseHelper databaseHelper =
                        new DatabaseHelper(MainActivity.this, curFlight.getInfo());
                boolean b = databaseHelper.addFlightData();
                Log.d("TAG", " do i work?: " + b);
                Toast.makeText(MainActivity.this, "Flight added", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "error adding flight",
                        Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }

            try {   //set current flight info for display
                setInfo();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            dialog.dismiss();

            cardViewCreation();
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
     */
    private void setInfo() throws JSONException {
        ArrayList<String> data = curFlight.getInfo(); //grabs ArrayList<String>
        // from backend

        //clear past information and sets up for new info
        // dynamically set size here in case backend is changed to support more info
        infoGrid.removeAllViews();
        infoGrid.setRowCount(data.size()/2);

        //displays flight number in header
        TextView header_fnum = findViewById(R.id.header_fNum);
        header_fnum.setText(data.get(0));

        //iterates starting a 1 to avoid Flight number
        for (int i = 1; i < data.size(); i++) {
            if(i == 5) i=i+2;
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
    private void cardViewCreation(){

        items = new ArrayList<>(); //data to send to reycle view adapter to read

        try {
            DatabaseHelper db = new DatabaseHelper(MainActivity.this, curFlight.getInfo());
            String[][] parasable = db.returnData();
            for (int i = 0; i < parasable.length; i++) {
                for (int j = 0; j < parasable[0].length; j++) {
                    System.out.println(parasable[i][j]);

                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //CardView temp = new CardView(this);
        //temp.getCardElevation();

        //place stuff from
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