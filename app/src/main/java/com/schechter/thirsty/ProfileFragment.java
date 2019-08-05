package com.schechter.thirsty;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    Button btn_logout;
    Button btn_login;

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

        setupUI(view);

    }

    @Override
    public void onResume() {
        super.onResume();

        setupUI(getView());
    }


    private void setupUI(View view)
    {
        btn_logout = view.findViewById(R.id.btn_logout);
        btn_login = view.findViewById(R.id.btn_login);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            btn_logout.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.GONE);

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

        }

        if(user == null) {
            btn_logout.setVisibility(View.GONE);
            btn_login.setVisibility(View.VISIBLE);

            btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), LoginMasterActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
