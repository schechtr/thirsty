package com.schechter.thirsty;


import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.volley.VolleyLog.TAG;
import static com.android.volley.VolleyLog.v;


/**
 * A simple {@link Fragment} subclass.
 */
public class MarkerDetailFragment extends Fragment {

    private static final String TAG = "MarkerDetailFragment";

    private String incomingMarkerID = "";
    private IMainActivity iMainActivity;
    private ChipGroup typeChipGroup;
    private CircleImageView marker_detail_photo;
    //private String markerID;


    // props from database
    private boolean bottle_refill;
    private boolean dog_bowl;
    private String databasePhotoURL = "";
    private Uri storagePhotoDownloadURI;
    //private StorageReference mImageStorageReference;

    public MarkerDetailFragment() {
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
            incomingMarkerID = bundle.getString(getString(R.string.marker_id_key));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_marker_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        typeChipGroup = view.findViewById(R.id.marker_detail_tags);
        marker_detail_photo = view.findViewById(R.id.marker_detail_photo);
        marker_detail_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!databasePhotoURL.equals("")) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    final View photo_view_container = getLayoutInflater().inflate(R.layout.photo_view, null);
                    builder.setView(photo_view_container);
                    AlertDialog dialog = builder.create();

                    PhotoView photoView = photo_view_container.findViewById(R.id.photo_view);
                    Glide.with(getContext()).load(storagePhotoDownloadURI).into(photoView);
                    dialog.show();

                }
            }
        });


        if (!incomingMarkerID.equals("")) {
            Log.d("marker", incomingMarkerID);

            final String markerID = incomingMarkerID;

            pullFirebaseData(view, markerID);
        }


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("marker", "detail view clicked");
            }
        });




    }



    private void pullFirebaseData(final View view, String markerID) {

        /* pull data from firebase */
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Locations").child(markerID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                bottle_refill = (boolean) dataSnapshot.child("bottle_refill").getValue();
                dog_bowl = (boolean) dataSnapshot.child("dog_bowl").getValue();
                databasePhotoURL = (String) dataSnapshot.child("photoURL").getValue();


                // now grab the detail images
                if (!databasePhotoURL.equals("")) {

                    pullFirebaseImages(view);
                } else
                    setupUI(view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: marker, database error");
            }
        });

    }

    private void pullFirebaseImages(final View view) {

        // create an image url to give to glide
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(databasePhotoURL).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                storagePhotoDownloadURI = uri;
                setupUI(view);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: couldn't download image");
            }
        });

    }


    private void setupUI(View view) {



        /* Add Chips */

        List<String> fountainType = new ArrayList<>();
        if (bottle_refill)
            fountainType.add("bottle refill");
        if (dog_bowl)
            fountainType.add("dog bowl");

        // idea: if fountainType is empty then add standard fountain as a tag perhaps
        if (fountainType.isEmpty()) {
            Chip chip = new Chip(getContext());
            chip.setText("standard fountain");
            typeChipGroup.addView(chip);
        } else {
            for (String tag : fountainType) {
                Chip chip = new Chip(getContext());
                chip.setText(tag);
                typeChipGroup.addView(chip);
            }
        }

        /* add photo */
        if (!databasePhotoURL.equals("")) {

            // load with glide
            Glide.with(getContext()).load(storagePhotoDownloadURI).into(marker_detail_photo);

            Log.d(TAG, "setupUI: " + storagePhotoDownloadURI);
        }

    }


}
