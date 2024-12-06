package edu.commonwealthu.flight_app;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Back end class to conduct api calls to AeroBoxAPI
 * @author Justin Peasley
 *
 * Note some airports JSON string may result in missprints on app not handled currently
 *  ex.had a terminal output be "erminal3 instead of just 3 (copenhagen airport)
 *      seems to not be an issue anymore but still noting incase of furthur issues
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

            /**
             * when receive response back from api save response and parse data
             * @param call         current api call
             * @param response     the response from the api
             * @throws IOException Exception for error handling
             */
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


        String departureTerminal = "N/A";
        if (departure != null && departure.has("terminal")) {
            departureTerminal = departure.getString("terminal");
        }

        String arrivalTerminal = "N/A";
        if (arrival != null && arrival.has("terminal")) {
            arrivalTerminal = arrival.getString("terminal");
        }

    }

    /**
     * pass flight data in order below
     *  departure country, arrival country
     *  departure city, arrival city
     *  departure time, arrival time
     *  departure date, arrival date note: could be different depending on flight times
     *
     * @return ArrayList<String> of information
     */
    public ArrayList<String> getInfo() throws JSONException {
        ArrayList<String> data = new ArrayList<String>();
        try {
            Log.d("TAG", "getInfo: ");
            Log.d("TAG", "getInfo: Starting");
            Log.d("TAG", "getInfo: flightdata: " + flightData);
            if (flightData != null) {
                //get relevant JSONObjects parsed for use
                JSONObject depAirport = departure.getJSONObject("airport");
                JSONObject arrAirport = arrival.getJSONObject("airport");
                JSONObject depTime = departure.getJSONObject("scheduledTime");
                JSONObject arrTime = arrival.getJSONObject("scheduledTime");

                Log.d("TAG", "getInfo: setup done!");

                //local time response is split into two outputs (date , time
                String Departure_time = depTime.getString("local");
                String Arrival_time   = arrTime.getString("local");

                ArrayList<String> tempData = new ArrayList<>();

                //note adds in alternation for proper printout in gridview
                tempData.add(flightData.getString("number"));
                tempData.add(depAirport.getString("countryCode"));
                tempData.add(arrAirport.getString("countryCode"));
                Log.d("TAG", "getInfo: country code done");
                tempData.add(depAirport.getString("municipalityName"));
                tempData.add(arrAirport.getString("municipalityName"));
                Log.d("TAG", "getInfo: municipality done");

                //handling instances where terminal not present
                if (departure.has("terminal"))
                    tempData.add("Terminal: " + departure.getString("terminal"));
                else if (departure.has("checkInDesk"))
                    tempData.add("desk: " + departure.getString("checkInDesk"));
                else tempData.add("No terminal");
                if (arrival.has("terminal"))
                    tempData.add("Terminal: " + arrival.getString("terminal"));
                else if (departure.has("checkInDesk"))
                    tempData.add("desk: " + departure.getString("checkInDesk"));
                else tempData.add("No terminal");

                Log.d("TAG", "getInfo: terminal done");
                tempData.add(depAirport.getString("iata"));        //short form city name
                tempData.add(arrAirport.getString("iata"));        //short form city name
                Log.d("TAG", "getInfo: iata done");
                data.addAll(tempData);
                data.add(Departure_time.substring(0,10));            //date
                data.add(Arrival_time.substring(0,10));              //date
                data.add(Departure_time.substring(11));    //local time
                data.add(Arrival_time.substring(11));      //local time
                Log.d("TAG", "Plane-getInfo: successful grabbed data");
            } else {
                data.clear(); //incase partially stored data
                data.add("");
                data.add("Unknown Departure Airport");
                data.add("00:00");
                data.add("Unknown Arrival Airport");
                data.add("00:00");
                Log.d("TAG", "Plane-getInfo: failed grabbing data from api");
            }
        } catch (JSONException e) { //if can't find data return defaults + clear for default
            //return defaults
            Log.d("TAG", "Plane-getInfo JSONExcept: failed grabbing data from api");
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