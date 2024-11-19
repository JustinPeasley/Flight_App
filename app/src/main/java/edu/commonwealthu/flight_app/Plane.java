package edu.commonwealthu.flight_app;

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
        getData();
    }

    /**
     * makes the api call for the data and stores it in a JSONArray
     */
    private void getData() {
        String[] jsonResponse = new String[1]; // Using an array to handle lambda scoping

        Request request = new Request.Builder()
                .url(uriCall)
                .header("x-rapidapi-key", "")
                .header("x-rapidapi-host", "aerodatabox.p.rapidapi.com")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONArray arrayData = new JSONArray(responseBody); // Temp array to hold data
                flightData = arrayData.getJSONObject(0); // Move data to JSONObject for parsing
                System.out.println(arrayData.toString(4)); // Pretty print JSON array
            } else {
                System.err.println("API call failed: " + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * parses the main JSONObjects
     * note: further data calls from these objects will be done when needed
     *       to prevent having too many variables of parsed data
     */
    private void parseData() throws JSONException {
        fNum = flightData     .getJSONObject("number");
        gcd  = flightData     .getJSONObject("greatCircleDistance");
        aircraft = flightData .getJSONObject("aircraft");
        airline = flightData  .getJSONObject("airline");
        arrival = flightData  .getJSONObject("arrival");
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
     * @return array of information
     */
    public String[] getInfo() throws JSONException {
        String[] data = new String[3];

        //parse data
        JSONObject depAirport;  //departure airport name
        depAirport = departure.getJSONObject("airport");
        data[0] = depAirport.getString("name");

        JSONObject depTime;     //departure time
        depTime = depAirport.getJSONObject("scheduledTime");
        data[1] = depTime.getString("local");


        JSONObject arrAirport; //arrival airport
        arrAirport = arrival.getJSONObject("airport");
        data[2] = arrAirport.getString("name");

        JSONObject arrTime;     //arrival time
        arrTime = arrAirport.getJSONObject("scheduledTime");
        data[3] = arrTime.getString("local");

        return data;
    }

}