package edu.commonwealthu.flight_app;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.*;

import java.io.IOException;



public class Plane {
    private static String uriCall;     //call for api has request search appended on the end
    private JSONObject flightData;      // store Data from API call here

    //each JSONObject from api call
    private JSONObject fNum;            // flight number
    private JSONObject gcd;             // great circle distance (shortest path for plane flight)
    private JSONObject aircraft;        // aircraft model
    private JSONObject airline;         // airline data
    private JSONObject arrival;         // arrival data
    private JSONObject departure;       // departure data

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
        //fNum    = flightData     .getJSONObject("number");
        gcd       = flightData.getJSONObject("greatCircleDistance");
        Log.d("TAG", "parseData: GCD");
        aircraft  = flightData.getJSONObject("aircraft");
        Log.d("TAG", "parseData: Aircraft");
        airline   = flightData.getJSONObject("airline");
        Log.d("TAG", "parseData: AIRLINE");
        arrival   = flightData.getJSONObject("arrival");
        Log.d("TAG", "parseData: Arrival");
        departure = flightData.getJSONObject("departure");
        Log.d("TAG", "parseData: Departure received.");
    }

    /**
     * pass flight data in order below
     *  departure airport name
     *  departure time
     *  arrival airport name
     *  arrival time
     *
     *
     * @return array of information
     */
    public String[] getInfo() throws JSONException {
        String[] data = new String[4];

        try {
            Log.d("TAG", "getInfo: Starting");
            if (flightData != null) {
                Log.d("TAG", "getInfo: initialing info grab");
                Log.d("TAG", "getInfo: depature = " +  departure.toString());
                JSONObject depAirport;  //departure airport name
                depAirport = departure.getJSONObject("airport");
                data[0] = depAirport.getString("name");
                Log.d("TAG", "getInfo: depAirport fail");

                JSONObject depTime;     //departure time
                depTime = departure.getJSONObject("scheduledTime");
                data[1] = depTime.getString("local");
                Log.d("TAG", "getInfo: depTime fail");


                JSONObject arrAirport; //arrival airport
                arrAirport = arrival.getJSONObject("airport");
                data[2] = arrAirport.getString("name");
                Log.d("TAG", "getInfo: arrAirport");

                JSONObject arrTime;     //arrival time
                arrTime = arrival.getJSONObject("scheduledTime");
                data[3] = arrTime.getString("local");
                Log.d("TAG", "getInfo: arrTime fail");

            } else { //if can't find data return defaults
                System.out.println("default can't parse !!");
                data[0] = "Unknown Departure Airport";
                data[1] = "00:00";
                data[2] = "Unknown Arrival Airport";
                data[3] = "00:00";
                Log.d("TAG", "getInfo: else fail");
            }
        } catch (JSONException e) {
            //return defaults
            Log.d("TAG", "getInfo catch: ");
            data[0] = "Unknown Departure Airport";
            data[1] = "00:00";
            data[2] = "Unknown Arrival Airport";
            data[3] = "00:00";
            Log.d("TAG", "getInfo: Catch failed");
        }
        return data;
    }

}