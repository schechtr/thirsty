package com.schechter.thirsty;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

// bug workaround from stackoverflow for displaying the full address after selecting a place
// https://stackoverflow.com/questions/41373630/placeautocompletefragment-full-address


public class customAutoCompleteFragment extends AutocompleteSupportFragment {

    Place place;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        this.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                customAutoCompleteFragment.this.place = place;

                customAutoCompleteFragment.this.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }




}
