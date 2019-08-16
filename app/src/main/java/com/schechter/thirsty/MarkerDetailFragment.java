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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.like.LikeButton;
import com.like.OnLikeListener;

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

    // props from database
    private boolean bottle_refill;
    private boolean dog_bowl;
    private String databasePhotoURL = "";
    private Uri storagePhotoDownloadURI;
    private String vicinity;
    private String place_name;

    // user specific
    private boolean starred = false;

    // buttons and ui elements
    private LikeButton btn_star;
    private ChipGroup typeChipGroup;
    private CircleImageView marker_detail_photo;


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
                if (!databasePhotoURL.equals("")) {

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

        btn_star = view.findViewById(R.id.star_button);
        btn_star.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                starred = btn_star.isLiked();

                if (!incomingMarkerID.equals("")) {
                    final String markerID = incomingMarkerID;

                    // TODO: IF no one is logged in then tell them they need to before being able to star                    */

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        final String uid = user.getUid();
                        updateFirebaseUserData(markerID, uid);
                    }
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {

                starred = btn_star.isLiked();

                if (!incomingMarkerID.equals("")) {
                    final String markerID = incomingMarkerID;

                    // TODO: IF no one is logged in then tell them they need to before being able to star                  */

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        final String uid = user.getUid();
                        updateFirebaseUserData(markerID, uid);


                    }
                }
            }
        });


        if (!incomingMarkerID.equals("")) {
            Log.d("marker", incomingMarkerID);

            final String markerID = incomingMarkerID;

            pullFirebaseData(view, markerID);

            // maybe hide the star up until this point
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                final String uid = user.getUid();
                pullFirebaseUserData(view, markerID, uid);
            }
        }


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("marker", "detail view clicked");
            }
        });


    }

    private void updateFirebaseUserData(final String markerID, final String uid) {

        /* update starred status */
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("starred");

        reference.child(markerID).setValue(starred);

    }


    private void pullFirebaseUserData(final View view, final String markerID, final String uid) {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("starred");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // find out if the markerID specified exists in the starred list and if so find out
                // if it is set to true (as in yes)

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    if (child.getKey().equals(markerID) && child.getValue().equals(true)) {
                        // then set starred to true
                        starred = true;
                    }
                }

                setupStar(view);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: marker, error fetching user data");
            }
        });

    }


    private void pullFirebaseData(final View view, final String markerID) {

        /* pull data from firebase */
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Locations").child(markerID);
        

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                
                /* TODO: check to make sure that dataSnapshot child actually exist */

                bottle_refill = (boolean) dataSnapshot.child("bottle_refill").getValue();
                dog_bowl = (boolean) dataSnapshot.child("dog_bowl").getValue();
                databasePhotoURL = (String) dataSnapshot.child("photo_url").getValue();

                vicinity = (String) dataSnapshot.child("vicinity").getValue();
                place_name = (String) dataSnapshot.child("nearby_place_name").getValue();

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

        /* add places data */
        ((TextView) view.findViewById(R.id.near_value)).setText(place_name);
        ((TextView) view.findViewById(R.id.address_value)).setText(vicinity);

    }

    private void setupStar(View view) {

        LikeButton star = view.findViewById(R.id.star_button);

        if (starred == false) {
            star.setLiked(false);
        } else {
            star.setLiked(true);
        }

    }

}
