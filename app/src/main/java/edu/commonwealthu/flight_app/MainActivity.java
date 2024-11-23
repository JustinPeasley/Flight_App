package edu.commonwealthu.flight_app;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import android.content.pm.ActivityInfo;

import edu.commonwealthu.flight_app.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private int displayWidth = getResources().getDisplayMetrics().widthPixels;
    private int displayHeight = getResources().getDisplayMetrics().heightPixels;
    private GridLayout InfoGrid;

    private Plane curFlight;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Lock screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get gridlayout that info will be displayed to
        InfoGrid = findViewById(R.id.infoDisplayGrid);

        findViewById(R.id.addflight).setOnClickListener(view -> {
            try {
                add_flight(view);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Allow for user to add a flight
     *  will call backend and api call query for data on flight
     *  then will call setInfo() to display it
     *
     * @param view
     */
    private void add_flight(View view) throws JSONException {

        //inflate window with ui to take in input

        //make a plane object with new fnum call

        //store data

        setInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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

    /**
     * Display information of current displayed flight
     */
    private void setInfo() throws JSONException {

        //clear past information
        InfoGrid.removeAllViews();

        String[] data = curFlight.getInfo(); //grabs string[] from backend

        for (int i = 0; i < 4; i++) {
            TextView txt = new TextView(this);
            txt.setText(data[i]);
            txt.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )); // Define layout params for TextView
            //txt.setGravity(Gravity.CENTER); // Center align the text
            txt.setPadding(8, 8, 8, 8); // Add padding for better readability
            
        }
    }

}