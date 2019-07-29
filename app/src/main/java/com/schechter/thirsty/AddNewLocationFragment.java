package com.schechter.thirsty;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddNewLocationFragment extends Fragment {

    private static final String TAG = "AddNewLocationFragment";
    
    private IMainActivity iMainActivity;
    private LatLng incomingLatLng;
    private MultiStateToggleButton multiStateToggleButton;
    private Button btn_confirm_add;
    private ImageButton btn_add_a_photo;

    public AddNewLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        iMainActivity = (MainActivity) getActivity();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            final double latitude = bundle.getDouble(getString(R.string.latitude_key));
            final double longitude = bundle.getDouble(getString(R.string.longitude_key));

            Log.d(TAG, "latitude received :" + latitude);
            Log.d(TAG, "longitude received :" + longitude);

            incomingLatLng = new LatLng(latitude, longitude);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_add_new_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // initialize elements
        multiStateToggleButton = getView().findViewById(R.id.mstb_multi_id);
        multiStateToggleButton.enableMultipleChoice(true);


        btn_add_a_photo = getView().findViewById(R.id.btn_add_a_photo);
        btn_add_a_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        btn_confirm_add = getView().findViewById(R.id.btn_confirm_add);
        btn_confirm_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LatLng currentLocation = incomingLatLng;
                final boolean[] options = multiStateToggleButton.getStates();
                addLocationToDatabase(currentLocation, options);

                /* TODO: pop back stack and send a message to the map to show the fab's */



            }
        });

    }

    private void addLocationToDatabase(LatLng location, boolean[] options) {

        final boolean bottle_refill = options[0];
        final boolean dog_bowl = options[1];

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Locations");
        databaseReference.push().setValue(new Location(location, bottle_refill, dog_bowl));
        
    }
}
