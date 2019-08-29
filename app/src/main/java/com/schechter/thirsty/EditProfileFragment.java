package com.schechter.thirsty;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";


    // views
    private Button btn_cancel;
    private Button btn_save;
    private CircleImageView profile_picture;
    private TextView btn_change_profile_picture;
    private FirebaseUser user;
    private TextInputEditText name, bio, city, state, country;

    // data
    private String nameValue, bioValue, cityValue, stateValue, countryValue;


    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            getFragmentManager().popBackStack();

        pullFireBaseUserData(view);

    }

    private void setupUI(View view) {

        // buttons
        btn_save = view.findViewById(R.id.btn_edit_profile_save);
        btn_cancel = view.findViewById(R.id.btn_edit_profile_cancel);
        btn_change_profile_picture = view.findViewById(R.id.edit_profile_pic_text);


        // TODO: set profile pic with glide
        profile_picture = view.findViewById(R.id.edit_profile_pic);

        // edit texts
        name = view.findViewById(R.id.edit_profile_name);
        bio = view.findViewById(R.id.edit_profile_bio);
        city = view.findViewById(R.id.edit_profile_city);
        state = view.findViewById(R.id.edit_profile_state);
        country = view.findViewById(R.id.edit_profile_country);

        // set text
        name.setText(nameValue);
        bio.setText(bioValue);
        city.setText(cityValue);
        state.setText(stateValue);
        country.setText(countryValue);


        // on click listeners
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateFirebaseUserData();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        btn_change_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_change_profile_picture.callOnClick();
            }
        });

    }

    private void updateFirebaseUserData() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Users").child(user.getUid());

        String newName = name.getText().toString();
        String newBio = bio.getText().toString();
        String newCity = city.getText().toString();
        String newState = state.getText().toString();
        String newCountry = country.getText().toString();

        if(!nameValue.equals(newName)) {
            databaseReference.child("name").setValue(newName);
        }

        if(!bioValue.equals(newBio)) {
            databaseReference.child("bio").setValue(newBio);
        }

        if(!cityValue.equals(newCity)) {
            databaseReference.child("city").setValue(newCity);
        }

        if(!stateValue.equals(newState)) {
            databaseReference.child("state").setValue(newState);
        }

        if(!countryValue.equals(newCountry)) {
            databaseReference.child("country").setValue(newCountry);
        }


    }

    private void pullFireBaseUserData(final View view) {

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
