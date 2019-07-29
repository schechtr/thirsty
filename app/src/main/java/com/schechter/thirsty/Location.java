package com.schechter.thirsty;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Location {

    private boolean dog_bowl;
    private boolean bottle_refill;
    private double latitude;
    private double longitude;


    public boolean isBottle_refill() {
        return bottle_refill;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isDog_bowl() {
        return dog_bowl;
    }

    public Location(LatLng location, boolean bottle_refill, boolean dog_bowl) {

        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.dog_bowl = dog_bowl;
        this.bottle_refill = bottle_refill;

    }

}
