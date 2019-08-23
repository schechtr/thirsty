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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContributedFragment extends Fragment {

    private static final String TAG = "ContributedFragment";

    // stuff a recycler item needs
    private List<String> mNearbyPlaceNames;
    private List<String> mVicinities;
    private List<Uri> mImageURLs;
    private List<String> mMarkerIDs;

    private MainActivity mMainActivity;

    // data pulled from firebase
    private List<Location> mLocations;
    private List<String> mContributed; // a list of markerID's

    // views
    private   ProgressBar progressBar;
    private LinearLayout contributedMessage;
    private RecyclerView mRecyclerView;


    public ContributedFragment() {
        // Required empty public constructor
        mLocations = new ArrayList<>();
        mContributed = new ArrayList<>();

        mMarkerIDs = new ArrayList<>();
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
        return inflater.inflate(R.layout.fragment_contributed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.contributed_progress);
        progressBar.setVisibility(View.GONE);

        contributedMessage = view.findViewById(R.id.contributed_message);
        contributedMessage.setVisibility(View.GONE);

        // if user is not logged in, prompt them to login
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {

            contributedMessage.setVisibility(View.VISIBLE);

            Snackbar snackbar = Snackbar.make(view, "Sign In to view contributed", Snackbar.LENGTH_LONG)
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

            // show progress wheel
            progressBar = view.findViewById(R.id.contributed_progress);
            progressBar.setVisibility(View.VISIBLE);

            // check if the user has any contributed locations
            pullFirebaseUserData(view, uid);

        }

        mMainActivity = (MainActivity) getActivity();

    }

    private void pullFirebaseUserData(final View view, final String uid) {


        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("contributed");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // pull the list of contributed locations
                mContributed.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    if (child.getValue().equals(true)) {

                        // then add that location to the list
                        if (!child.getKey().equals("0")) {
                            Log.d(TAG, "onDataChange: " + child.getKey());
                            mContributed.add(child.getKey());
                        }
                    }
                }

                if (!mContributed.isEmpty()) {
                    Log.d(TAG, "onDataChange: calling pullFirebaseLocationData");
                    pullFirebaseLocationData(view);
                } else {
                    /* TODO: what now */
                    progressBar.setVisibility(View.GONE);
                    contributedMessage.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onDataChange: this user has no contributions");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: error fetching user data");
            }
        });


    }


    /* TODO: optimize the search algorithm */
    private void pullFirebaseLocationData(final View view) {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Locations");


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mLocations.clear();

                for (final String markerID : mContributed) {

                    Location location = dataSnapshot.child(markerID).getValue(Location.class);
                    mLocations.add(location);
                }

                // remove locations that no longer exist
                mLocations.removeAll(Collections.singleton(null));
                initRecyclerItemContent(view);

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: error fetching location data");
            }
        });


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
                    mMarkerIDs.add(location.getLocation_id());
                    Log.d(TAG, "onSuccess: " + location.getNearby_place_name() + ", " + location.getLocation_id());


                    if (mImageURLs.size() == mLocations.size()) {
                        initRecyclerView(view);
                    }

                    progressBar.setVisibility(View.GONE);


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

        mRecyclerView = view.findViewById(R.id.contributed_recycler_container);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), mVicinities, mNearbyPlaceNames, mImageURLs, mMarkerIDs);
        adapter.setMainActivity(mMainActivity);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


    }

}
