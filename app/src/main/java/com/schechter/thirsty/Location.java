package com.schechter.thirsty;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

public class Location {

    private double latitude;
    private double longitude;

    private boolean dog_bowl = false;
    private boolean bottle_refill = false;
    private String photoURL = "";

    //private String[] photoURLs;
    //private Map<String, Boolean> type;


    public boolean isBottle_refill() {
        return bottle_refill;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public boolean isDog_bowl() {
        return dog_bowl;
    }

    public void setDog_bowl(boolean dog_bowl) {
        this.dog_bowl = dog_bowl;
    }

    public void setBottle_refill(boolean bottle_refill) {
        this.bottle_refill = bottle_refill;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }


    public Location(LatLng location) {
        this.latitude = location.latitude;
        this.longitude = location.longitude;
    }

    public Location(LatLng location, boolean bottle_refill, boolean dog_bowl) {

        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.dog_bowl = dog_bowl;
        this.bottle_refill = bottle_refill;

    }

}
