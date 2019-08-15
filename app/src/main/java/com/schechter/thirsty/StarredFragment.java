package com.schechter.thirsty;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class StarredFragment extends Fragment {

    private static final String TAG = "StarredFragment";


    // stuff a recycler item needs
    private List<String> mNearbyPlaceNames;
    private List<String> mVicinities;
    private List<Uri> mImageURLs;

    private RecyclerView mRecyclerView;

    // data pulled from firebase
    private List<Location> mLocations;
    private List<String> mStarred; // a list of markerID's


    public StarredFragment() {
        // Required empty public constructor
        mLocations = new ArrayList<>();
        mStarred = new ArrayList<>();

        mImageURLs = new ArrayList<>();
        mNearbyPlaceNames = new ArrayList<>();
        mVicinities = new ArrayList<>();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_starred, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Snackbar snackbar = Snackbar.make(view, "Sign In to view starred", Snackbar.LENGTH_LONG)
                    .setAction("Sign In", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(getActivity(), LoginMasterActivity.class);
                            startActivity(intent);

                        }
                    });
            snackbar.show();
        } else {
            final String uid = user.getUid();

            // check if the user has any starred locations
            pullFirebaseUserData(view, uid);

        }

    }

    private void pullFirebaseUserData(final View view, final String uid) {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("starred");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // pull the list of starred locations

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    if (child.getValue().equals(true)) {
                        // then add that location to the list
                        if (!child.getKey().equals("0"))
                            mStarred.add(child.getKey());
                    }
                }

                if (!mStarred.isEmpty()) {
                    Log.d(TAG, "onDataChange: calling pullFirebaseLocationData");
                    pullFirebaseLocationData(view);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: error fetching user data");
            }
        });


    }


    private void pullFirebaseLocationData(final View view) {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Locations");

        for (String markerID : mStarred) {

            reference.child(markerID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    Location location = dataSnapshot.getValue(Location.class);
                    mLocations.add(location);

                    // a shitty way of handling this async task, but faster than going thru literally every location in the database
                    // mStarred is 'guaranteed' to not be empty because we already checked for that earlier
                    if (mLocations.size() == mStarred.size()) {
                        // proceed
                        Log.d(TAG, "onDataChange: calling initRecyclerItemContent");

                        initRecyclerItemContent(view);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: error fetching location data");
                }
            });

        }


    }

    private void initRecyclerItemContent(final View view) {

        for (final Location location : mLocations) {


            if (location.getPhoto_url().equals(""))
                location.setPhoto_url("images/fountain_round.png");

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            storageReference.child(location.getPhoto_url()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    Log.d(TAG, "onSuccess: " + uri);


                    // proceed
                    mImageURLs.add(uri);
                    mNearbyPlaceNames.add(location.getNearby_place_name());
                    mVicinities.add(location.getVicinity());


                    if (mImageURLs.size() == mLocations.size()) {
                        initRecyclerView(view);
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: couldn't get download url");
                }
            });
        }

    }


    private void initRecyclerView(View view) {

        mRecyclerView = view.findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), mVicinities, mNearbyPlaceNames, mImageURLs);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


    }


}
