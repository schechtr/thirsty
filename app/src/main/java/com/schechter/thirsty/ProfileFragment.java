package com.schechter.thirsty;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // views
    private Button btn_logout;
    private Button btn_login;
    private RelativeLayout profile_content_container;
    private LinearLayout profile_message;
    private Button btn_edit_profile;
    private TextView name, hometown, bio;
    private FirebaseUser user;

    // data
    private String nameValue, bioValue, cityValue, stateValue, countryValue;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }

    @Override
    public void onResume() {
        super.onResume();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null)
            setupUI(getView());
        else
            pullFirebaseUserData(getView());

    }


    private void setupUI(View view) {
        btn_edit_profile = view.findViewById(R.id.btn_edit_profile);
        btn_logout = view.findViewById(R.id.btn_logout);
        btn_login = view.findViewById(R.id.btn_login);
        profile_content_container = view.findViewById(R.id.profile_container);
        profile_message = view.findViewById(R.id.profile_message);

        if (user == null) {
            btn_logout.setVisibility(View.GONE);
            btn_login.setVisibility(View.VISIBLE);
            profile_content_container.setVisibility(View.GONE);
            profile_message.setVisibility(view.VISIBLE);


            btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), LoginMasterActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            btn_logout.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.GONE);
            profile_content_container.setVisibility(View.VISIBLE);
            profile_message.setVisibility(view.GONE);

            /* set profile data */
            name = view.findViewById(R.id.profile_name);
            bio = view.findViewById(R.id.profile_bio);
            hometown = view.findViewById(R.id.profile_hometown);
            name.setText(nameValue);
            bio.setText(bioValue);

            // parse hometown
            String hometownValue = "";
            if(!cityValue.equals(""))
                hometownValue += cityValue;
                if(!stateValue.equals(""))
                    hometownValue += ", " + stateValue;
                        if(!countryValue.equals(""))
                            hometownValue += ", " + countryValue;
            hometown.setText(hometownValue);


            // on click listeners
            btn_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    btn_logout.setVisibility(View.GONE);
                    btn_login.setVisibility(View.GONE);


                    MapFragment mapFragment = new MapFragment();
                    getFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                            mapFragment).commit();

                }
            });


            btn_edit_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    EditProfileFragment editProfileFragment = new EditProfileFragment();
                    getFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                            editProfileFragment).addToBackStack(null).commit();
                }
            });

        }


    }

    private void pullFirebaseUserData(final View view) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Users").child(user.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                nameValue = (String) dataSnapshot.child("name").getValue();
                bioValue = (String) dataSnapshot.child("bio").getValue();
                cityValue = (String) dataSnapshot.child("city").getValue();
                stateValue = (String) dataSnapshot.child("state").getValue();
                countryValue = (String) dataSnapshot.child("country").getValue();


                setupUI(view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: error pulling user data");
            }
        });



    }

}
