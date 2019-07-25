package com.schechter.thirsty;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Arrays;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;


public class MapFragment extends Fragment implements OnMapReadyCallback {


    /*** member variables ***/
    private GoogleMap mMap;
    private Place mPlace;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;
    private View locationButton;
    private ImageButton myLocationButton;
    private AutocompleteSupportFragment autocompleteFragment;
    private PlacesClient placesClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;


    public MapFragment() {
        // Required empty public constructor
    }


    /*** limit the amount of computation done in this method, and use onViewCreated for the rest ***/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_maps, container, false);


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());


      /*  autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));*/

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }


    /******************************************************
     ****                onMapReady                    ****
     ******************************************************/

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // set up clustering
        setUpClusterManager(googleMap);

        mLocationRequest = new LocationRequest();
        // mLocationRequest.setInterval(1000);
        // mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        /*** check for location permission before proceeding ***/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);

            } else {
                checkLocationPermission();

            }
        }

        /*** setup custom current location button ***/
        myLocationButton = (ImageButton) getView().findViewById(R.id.btn_current_location);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null) {
                    if (locationButton != null) {
                        locationButton.callOnClick();
                    }
                }
            }
        });

        locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        if (locationButton != null) {
            locationButton.setVisibility(View.GONE);
        }



        /*** setup for autocomplete places api ***/

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.api_key));
        }

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                mPlace = place;


                // TODO: find a way to show the complete address in the edittext search bar

                //autocompleteFragment.setHint(mPlace.getAddress());

               // ((EditText) getView().findViewById(R.id.places_autocomplete_search_input)).setText(place.getAddress());

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Places Activity", "An error occurred: " + status);
            }


        });



    }

    /*** setup for map pin clustering ***/
    private void setUpClusterManager(GoogleMap googleMap) {
        ClusterManager<MarkerItem> clusterManager = new ClusterManager<>(getContext(), googleMap);
        clusterManager.setRenderer(new MarkerClusterRenderer(getContext(), googleMap, clusterManager));
        googleMap.setOnCameraIdleListener(clusterManager);
        List<MarkerItem> items = getItems();
        clusterManager.addItems(items);
        clusterManager.cluster();
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerItem>() {
            @Override
            public boolean onClusterItemClick(MarkerItem markerItem) {

                Log.d("marker clicked", "Marker clicked");

                return true;
            }
        });

        googleMap.setOnMarkerClickListener(clusterManager);



    }

    /*** list which serves as the database for drawing pins on the map ***/
    private List<MarkerItem> getItems() {
        return Arrays.asList(

                new MarkerItem(new LatLng(33.628671, -117.657340)),
                new MarkerItem(new LatLng(-32.563910, 147.154312)),
                new MarkerItem(new LatLng(-33.563910, 147.154312)),
                new MarkerItem(new LatLng(-34.563910, 147.154312)),
                new MarkerItem(new LatLng(-31.563910, 148.154312)),
                new MarkerItem(new LatLng(-31.563910, 146.154312)),
                new MarkerItem(new LatLng(-31.563910, 145.154312)),
                new MarkerItem(new LatLng(-31.563910, 144.154312)),
                new MarkerItem(new LatLng(-31.563910, 143.154312)),
                new MarkerItem(new LatLng(-31.563910, 142.154312)),
                new MarkerItem(new LatLng(-31.563910, 141.154312)),
                new MarkerItem(new LatLng(-31.563910, 140.154312)),
                new MarkerItem(new LatLng(-31.563910, 149.154312)),
                new MarkerItem(new LatLng(-31.963910, 147.154312)),
                new MarkerItem(new LatLng(-31.563910, 146.954312)),
                new MarkerItem(new LatLng(-32.163910, 147.154312)),
                new MarkerItem(new LatLng(-33.963910, 147.854312)),
                new MarkerItem(new LatLng(-32.563910, 147.754312))
        );
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (mFusedLocationClient.getApplicationContext() != null) {
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // move the camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                }
            }
        }
    };


    /******************************************************
     **** PERMISSION TO ACCESS LOCATION IMPLEMENTATION ****
     ******************************************************/

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /*** this dialog box looks a bit tacky i guess ***/
            /*
             if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
             new android.app.AlertDialog.Builder(getContext())
             .setTitle("Grant Location Permission")
             .setMessage("In order to find drinking fountains near you, thirsty needs to know your location. Please press 'Allow' in the next dialog box to enable location tracking.")
             .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            })
             .create()
             .show();
             } else {
             requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
             }
             */

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getContext(), "please allow location permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


}




