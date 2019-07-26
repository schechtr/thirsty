package com.schechter.thirsty;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerItem implements ClusterItem {

    private final LatLng latLng;
    private final String id;

    public MarkerItem(String id, LatLng latLng) {
        this.latLng = latLng;
        this.id = id;
    }

    @Override
    public LatLng getPosition() {

        return latLng;
    }

    @Override
    public String getTitle() {

        return "";
    }


    public String getID() {

        return id;
    }

    @Override
    public String getSnippet() {

        return "";
    }


}
