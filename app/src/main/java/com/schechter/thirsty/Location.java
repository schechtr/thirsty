package com.schechter.thirsty;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class Location {

    private static final String TAG = "Location";

    // Location Properties

    private double latitude;
    private double longitude;


    private String vicinity = "";
    private String nearby_place_name = "";

    private boolean dog_bowl = false;
    private boolean bottle_refill = false;
    private String photo_url = "";


    //private List<HashMap<String, List<String>>> nearby_places;


    private String location_id;
    private Context mContext;


    private final int NEARBY_SEARCH_RADIUS = 50; //meters

    //private String[] photoURLs;
    //private Map<String, Boolean> type;


    /* ************************** */
    /*          Getters           */
    /* ************************** */

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isDog_bowl() {
        return dog_bowl;
    }

    public boolean isBottle_refill() {
        return bottle_refill;
    }

   /* public List<HashMap<String, List<String>>> getNearby_places() {
        Log.d(TAG, "getNearbyPlaces: " + nearby_places);
        return nearby_places;
    }*/

    public String getPhoto_url() {
        return photo_url;
    }

    public String getVicinity() {
        return vicinity;
    }

    public String getNearby_place_name() {
        return nearby_place_name;
    }


    public String getLocation_id() {
        return location_id;
    }

    /* ************************** */
    /*           Setters          */
    /* ************************** */

    public void setDog_bowl(boolean dog_bowl) {
        this.dog_bowl = dog_bowl;
    }

    public void setBottle_refill(boolean bottle_refill) {
        this.bottle_refill = bottle_refill;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public void setNearby_place_name(String nearby_place_name) {
        this.nearby_place_name = nearby_place_name;
    }


    public void setNearby_places(List<HashMap<String, List<String>>> nearby_places) {
        
        
        // sort out which place data to keep

        /* TODO: What should happen when no nearby places were found */
        if (nearby_places.isEmpty()) {


            vicinity = "Unknown";
            nearby_place_name = "Unknown";

            return;
        }

        HashMap<String, List<String>> entry = nearby_places.get(0);
        try {

            vicinity = entry.get("vicinity").get(0);
            if (vicinity.equals("--NA--")) {
                vicinity = "Unknown";
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "setNearby_places: vicinity was null");
            e.printStackTrace();
        }
        try {
            nearby_place_name = entry.get("place_name").get(0);
            if (nearby_place_name.equals("--NA--")) {
                nearby_place_name = "Unknown";
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "setNearby_places: place_name was null");
            e.printStackTrace();
        }


           // this.nearby_places = nearby_places;

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Locations");
            //databaseReference.child(location_id).child("nearby_places").setValue(nearby_places);
            databaseReference.child(location_id).child("nearby_place_name").setValue(nearby_place_name);
            databaseReference.child(location_id).child("vicinity").setValue(vicinity);

        }


    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }



    /* ************************** */
    /*          MarkerID          */
    /* ************************** */


    public Location() {
        // Default constructor required for calls to DataSnapshot.getValue(Location.class)
    }


    public Location(Context context, LatLng location) {
        mContext = context;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        //this.nearby_places = new ArrayList<>();
        //HashMap<String, List<String>> placeHolder = new HashMap<>();
        //List<String> placeHolderList = new ArrayList<String>();
        //placeHolderList.add("0");
        //placeHolder.put("0", placeHolderList);
        //nearby_places.add(placeHolder);
    }

    public Location(Context context, LatLng location, boolean bottle_refill, boolean dog_bowl) {
        mContext = context;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.dog_bowl = dog_bowl;
        this.bottle_refill = bottle_refill;


    }


    // Public Methods

    public void generateAddress() {


    }

    public void findNearestPlace() {

        String nearbySearchUrl = buildUrl();
        String googlePlacesResponse = "";

        Object[] dataTransfer = new Object[2];
        dataTransfer[0] = nearbySearchUrl;
        dataTransfer[1] = (Location) this;

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);


    }

    // Private Methods

    private String buildUrl() {



        String nearbySearchUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        nearbySearchUrl += "&location=" + getLatitude() + "," + getLongitude();
        //nearbySearchUrl += "&location=" + 33.6817602 + "," + -117.6616694;
        nearbySearchUrl += "&radius=" + NEARBY_SEARCH_RADIUS;
        nearbySearchUrl += "&key=" + mContext.getString(R.string.api_key);


        return nearbySearchUrl;

        //return "https://api.myjson.com/bins/1gfxzx";
    }


}
