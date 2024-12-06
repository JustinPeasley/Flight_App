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
 * Back end class to conduct api calls to AeroBoxAPI and get flight information back
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
    private String     fNum;         // flight number
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

        //build the api call
        Request request = new Request.Builder()
                .url(uriCall)
                .header("x-rapidapi-key", "1aa8bbc1b2msh5062d99bafbbe82p109479jsnee0b36ad902a")
                .header("x-rapidapi-host", "aerodatabox.p.rapidapi.com")
                .get()
                .build();

        //make api call (uses enqueue to run on different thread)
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }

            /**
             * when receive response back from api save response and parse data
             * @param call         current api call
             * @param response     the response from the api
             * @throws IOException Exception for error handling
             */
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        // Temp array to hold data
                        JSONArray arrayData = new JSONArray(responseBody);
                        // Move data to JSONObject for parsing
                        flightData = arrayData.getJSONObject(0);
                        parseData();    //parse data upon successful response
                    } catch (JSONException e) {
                        //noinspection CallToPrintStackTrace
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
        fNum      = flightData.getString("number");
        gcd       = flightData.getJSONObject("greatCircleDistance");
        aircraft  = flightData.getJSONObject("aircraft");
        airline   = flightData.getJSONObject("airline");
        arrival   = flightData.getJSONObject("arrival");
        departure = flightData.getJSONObject("departure");

        // handle these independently (some flights don't have terminals)
        //      may have desk numbers or neither
        String departureTerminal = "N/A";
        //noinspection ConstantValue
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
            //if have api data get all necessary data
            //note below store everything into a temp Arraylist before adding substring's
            //due to code printing out in wrong order at first
            if (flightData != null) {
                //get relevant JSONObjects parsed for use
                JSONObject depAirport = departure.getJSONObject("airport");
                JSONObject arrAirport = arrival.getJSONObject("airport");
                JSONObject depTime = departure.getJSONObject("scheduledTime");
                JSONObject arrTime = arrival.getJSONObject("scheduledTime");

                //local time response is split into two outputs (date , time
                String Departure_time = depTime.getString("local");
                String Arrival_time   = arrTime.getString("local");

                //split the data addition due to getString being asynchronous call
                //leading to addition of items in the wrong order
                ArrayList<String> tempData = new ArrayList<>();

                //start adding data in order
                tempData.add(flightData.getString("number"));
                tempData.add(depAirport.getString("countryCode"));
                tempData.add(arrAirport.getString("countryCode"));
                tempData.add(depAirport.getString("municipalityName"));
                tempData.add(arrAirport.getString("municipalityName"));

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

                tempData.add(depAirport.getString("iata"));  //short form city name
                tempData.add(arrAirport.getString("iata"));  //short form city name

                data.addAll(tempData);                             //add above data
                data.add(Departure_time.substring(0,10));          //date
                data.add(Arrival_time.substring(0,10));            //date
                data.add(Departure_time.substring(11));  //local time
                data.add(Arrival_time.substring(11));    //local time
            } else {
                //sets to defaults
                //noinspection RedundantOperationOnEmptyContainer
                data.clear(); //in case partially stored data
                data.add("");
                data.add("Unknown Departure Airport");
                data.add("00:00");
                data.add("Unknown Arrival Airport");
                data.add("00:00");
            }
        } catch (JSONException e) { //if can't find data return defaults + clear for default
            //return defaults
            //noinspection RedundantOperationOnEmptyContainer
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