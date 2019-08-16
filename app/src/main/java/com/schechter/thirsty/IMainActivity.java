package com.schechter.thirsty;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;

public interface IMainActivity  {

    void sendMarkerID(Fragment fragment, String ID);

    void sendCurrentLocation(Fragment fragment, LatLng latLng);

}
