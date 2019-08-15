package com.schechter.thirsty;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/* Credit to @priyankapakhale on github for the JSON parser */

public class GetNearbyPlacesData extends AsyncTask<Object, Void, String> {

    private static final String TAG = "GetNearbyPlacesData";
    private Location mLocation;

    private final String[] TYPES_TO_EXCLUDE = {"political", "locality"};


    @Override
    protected String doInBackground(Object... objects) {

        String nearbySearchUrl = (String) objects[0];
        mLocation = (Location) objects[1];
        String googlePlacesResponse = "";

        try {



            googlePlacesResponse = readUrl(nearbySearchUrl);
            Log.d(TAG, "findNearestPlace: " + googlePlacesResponse);
        } catch (Exception e) {
            Log.d(TAG, "findNearestPlace: exception occurred while getting places data");
            e.printStackTrace();
        }

        return googlePlacesResponse;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        List<HashMap<String, List<String>>> places = parseJSON(s);

        Log.d(TAG, "onPostExecute: " + places);

        mLocation.setNearby_places(places);


    }


    /* JSON parser and download tool */

    private String readUrl(String nearbySearchUrl) throws Exception {

        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        // read JSON line by line and store in a string

        try {

            URL url = new URL(nearbySearchUrl);

            Log.d(TAG, "readUrl: " + url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();


            inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();

            String line = "";
            while ((line = reader.readLine()) != null)
                stringBuffer.append(line);


            data = stringBuffer.toString();
            reader.close();

        } catch (MalformedURLException e) {
            Log.d(TAG, "readUrl: malformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "readUrl: IOException");
            e.printStackTrace();
        } finally {

            // if there is an exception we must close the connections and input stream
            inputStream.close();
            urlConnection.disconnect();
        }

        Log.d(TAG, "readUrl: " + data);
        return data;

    }


    private List<HashMap<String, List<String>>> parseJSON(String googlePlacesResponse) {

        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(googlePlacesResponse);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "parseJSON: JSONException");
        }

        return getPlaces(jsonArray);
    }

    private List<HashMap<String, List<String>>> getPlaces(JSONArray googlePlaceJsonArray) {


        List<HashMap<String, List<String>>> places = new ArrayList<>();
        HashMap<String, List<String>> place;

        int size = googlePlaceJsonArray.length();

        for (int i = 0; i < size; i++) {

            try {

                place = getPlace((JSONObject) googlePlaceJsonArray.get(i));
                if(place != null)
                    places.add(place);
            } catch (JSONException e) {
                Log.d(TAG, "getPlaces: JSONException");
                e.printStackTrace();
            }

        }

        return places;
    }

    private HashMap<String, List<String>> getPlace(JSONObject googlePlaceJson) {

        HashMap<String, List<String>> place = new HashMap<>();
        List<String> placeName = new ArrayList<>();
        List<String> vicinity = new ArrayList<>();
        List<String> latitude = new ArrayList<>();
        List<String> longitude = new ArrayList<>();
        List<String> types = new ArrayList<>();

        //String reference

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName.add(googlePlaceJson.getString("name"));
            } else {
                placeName.add("--NA--");
            }

            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity.add(googlePlaceJson.getString("vicinity"));
            } else {
                vicinity.add("--NA--");
            }

            if (!googlePlaceJson.isNull("types")) {
                JSONArray typesJSON = googlePlaceJson.getJSONArray("types");
                for (int i = 0; i < typesJSON.length(); i++) {

                    String type = typesJSON.getString(i);
                    if (Arrays.asList(TYPES_TO_EXCLUDE).contains(type)) {
                        return null;
                    } else {
                        types.add(type);
                    }
                }
            } else {
                types.add("--NA--");
            }

            latitude.add(googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat"));
            longitude.add(googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng"));

            place.put("place_name", placeName);
            place.put("vicinity", vicinity);
            place.put("lat", latitude);
            place.put("lng", longitude);
            place.put("types", types);


        } catch (JSONException e) {
            Log.d(TAG, "getPlace: JSONException");
            e.printStackTrace();

        }

        return place;
    }
}