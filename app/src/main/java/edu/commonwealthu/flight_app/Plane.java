package edu.commonwealthu.flight_app;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Back end class to conduct api calls to AeroBoxAPI
 * @author Justin Peasley
 *
 * Note some airports JSON string may result in missprints on app not handled
 * submitted a bug report for following:
 *  ex.had a terminal output be "erminal3 instead of just 3 (copenhagen airport)
 */
public class Plane {
    private static String uriCall;     //call for api has request search appended on the end
    private JSONObject flightData;      // store Data from API call here

    //each JSONObjects from api call to simplify searching for data
    private String     fNum;             // flight number
    private JSONObject gcd;          // great circle distance (shortest path for plane flight)
    private JSONObject aircraft;     // aircraft model
    private JSONObject airline;      // airline data
    private JSONObject arrival;      // arrival data
    private JSONObject departure;    // departure data

    private static final OkHttpClient client = new OkHttpClient();

    /**
     * constructor that gathers Flight number to request data for
     * @param fNum flight data
     */
    public Plane(String fNum) {
        uriCall = "https://aerodatabox.p.rapidapi.com/flights/number/" + fNum;
        getData(); //api call
    }

    /**
     * makes the api call for the data and stores it in a JSONArray
     */
    private void getData() {
        String[] jsonResponse = new String[1]; // Using an array to handle lambda scoping

        Request request = new Request.Builder()
                .url(uriCall)
                .header("x-rapidapi-key", "1aa8bbc1b2msh5062d99bafbbe82p109479jsnee0b36ad902a")
                .header("x-rapidapi-host", "aerodatabox.p.rapidapi.com")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.err.println("API call failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONArray arrayData = new JSONArray(responseBody); // Temp array to hold data
                        flightData = arrayData.getJSONObject(0); // Move data to JSONObject for parsing
                        System.out.println(arrayData.toString(4)); // Pretty print JSON array
                        Log.d("TAG", "onResponse: Data recieved and stored in JSON Obj");
                        parseData();    //parse data upon successful response
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("API call failed: " + response.code() + " " + response.message());
                }
            }
        });
    }


    /**
     * parses the main JSONObjects
     * note: further data calls from these objects will be done when needed
     *       to prevent having too many variables of parsed data
     */
    private void parseData() throws JSONException {
        Log.d("TAG", "parseData: INITIATE PARSE");
        fNum    = flightData     .getString("number");
        gcd       = flightData.getJSONObject("greatCircleDistance");
        aircraft  = flightData.getJSONObject("aircraft");
        airline   = flightData.getJSONObject("airline");
        arrival   = flightData.getJSONObject("arrival");
        departure = flightData.getJSONObject("departure");
    }

    /**
     * pass flight data in order below
     *  departure airport name
     *  departure time
     *  arrival airport name
     *  arrival time
     *
     *
     * @return ArrayList<String> of information
     */
    public ArrayList<String> getInfo() throws JSONException {
        ArrayList<String> data = new ArrayList<String>();

        try {
            Log.d("TAG", "getInfo: Starting");
            if (flightData != null) {
                //get relevant JSONObjects parsed for use
                JSONObject depAirport = departure.getJSONObject("airport");
                JSONObject arrAirport = arrival.getJSONObject("airport");
                JSONObject depTime = departure.getJSONObject("scheduledTime");
                JSONObject arrTime = arrival.getJSONObject("scheduledTime");

                //local time response is split into two outputs (date , time
                String Departure_time = depTime.getString("local");
                String Arrival_time   = arrTime.getString("local");

                //note adds in alternation for proper printout in gridview
                data.add(flightData.getString("number"));
                data.add(depAirport.getString("countryCode"));
                data.add(arrAirport.getString("countryCode"));
                data.add(depAirport.getString("municipalityName"));
                data.add(arrAirport.getString("municipalityName"));
                data.add("Terminal: " + departure.getString("terminal"));
                data.add("Terminal: " + arrival.getString("terminal"));
                data.add(Departure_time.substring(0,9));            //date
                data.add(Arrival_time.substring(0,9));              //date
                data.add(Departure_time.substring(10));   //local time
                data.add(Arrival_time.substring(10));     //local time

            } else {          //if can't find data return defaults + clear for default
                data.clear();
                data.add("");
                data.add("Unknown Departure Airport");
                data.add("00:00");
                data.add("Unknown Arrival Airport");
                data.add("00:00");
            }
        } catch (JSONException e) {
            //return defaults
            data.clear();
            data.add("");
            data.add("Unknown Departure Airport");
            data.add("00:00");
            data.add("Unknown Arrival Airport");
            data.add("00:00");
        }
        return data;
    }

}