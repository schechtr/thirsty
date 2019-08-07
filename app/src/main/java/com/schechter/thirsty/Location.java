package com.schechter.thirsty;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.List;


public class Location {

    private static final String TAG = "Location";

    // Location Properties

    private double latitude;
    private double longitude;

    private boolean dog_bowl = false;
    private boolean bottle_refill = false;
    private String photoURL = "";



    private List<HashMap<String, String>> nearbyPlaces;


    private Context mContext;



    private final int NEARBY_SEARCH_RADIUS = 50; //meters

    //private String[] photoURLs;
    //private Map<String, Boolean> type;


    // Getters

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public String getPhotoURL() { return photoURL; }
    public boolean isDog_bowl() {
        return dog_bowl;
    }
    public boolean isBottle_refill() {
        return bottle_refill;
    }
    public List<HashMap<String, String>> getNearbyPlaces() { return nearbyPlaces; }

    // Setters
    public void setDog_bowl(boolean dog_bowl) {
        this.dog_bowl = dog_bowl;
    }
    public void setBottle_refill(boolean bottle_refill) {
        this.bottle_refill = bottle_refill;
    }
    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
    public void setNearbyPlaces(List<HashMap<String, String>> nearbyPlaces) { this.nearbyPlaces = nearbyPlaces; }

// Constructors

    public Location() {
        // Default constructor required for calls to DataSnapshot.getValue(Location.class)
    }


    public Location(Context context, LatLng location) {
        mContext = context;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
    }

    public Location(LatLng location, boolean bottle_refill, boolean dog_bowl) {

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
        dataTransfer[1] = this;

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);


    }

    // Private Methods

    private String buildUrl() {

        String nearbySearchUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        //nearbySearchUrl += "&location=" + getLatitude() + "," + getLongitude();
        nearbySearchUrl += "&location=" + 33.6817602 + "," + -117.6616694;
        nearbySearchUrl += "&radius=" + NEARBY_SEARCH_RADIUS;
        nearbySearchUrl += "&key=" + mContext.getString(R.string.api_key);


        return nearbySearchUrl;
    }


}
