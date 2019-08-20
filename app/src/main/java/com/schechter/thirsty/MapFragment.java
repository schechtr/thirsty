package com.schechter.thirsty;


import android.Manifest;
import android.content.Context;
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
import android.provider.ContactsContract;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;


public class MapFragment extends Fragment implements OnMapReadyCallback {


    // member variables
    private GoogleMap mMap;
    private Place mPlace;
    private FusedLocationProviderClient mFusedLocationClient;
    private PlacesClient placesClient;
    private boolean mFirstRequest; // sketchy way of detecting the first request for locationCallback
    private List<MarkerItem> mMarkerItems;

    private Context mContext;

    // Buttons and fragments
    private SupportMapFragment mapFragment;
    private MarkerDetailFragment markerDetailFragment;
    private AddNewLocationFragment addNewLocationFragment;
    private View locationButton;
    private FloatingActionButton btn_current_location, btn_add, btn_map_type;
    private AutocompleteSupportFragment autocompleteFragment;
    private MultiStateToggleButton mstb_map_type;


    // Firebase database
    private DatabaseReference mDatabaseReference;
    private List<MarkerItem> markerItemList;

    // Marker ID Communication
    private String outgoingMarkerID;
    private String incomingMarkerID = "";

    // general communication
    private IMainActivity iMainActivity;


    LocationRequest mLocationRequest;
    Location mLastLocation;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iMainActivity = (IMainActivity) getActivity();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            incomingMarkerID = bundle.getString(getString(R.string.marker_id_key));
        }
    }

    /*** limit the amount of computation done in this method, and use onViewCreated for the rest ***/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        markerDetailFragment = (MarkerDetailFragment) getFragmentManager()
                .findFragmentById(R.id.marker_detail);
        addNewLocationFragment = (AddNewLocationFragment) getFragmentManager()
                .findFragmentById(R.id.add_new_location);

        mFirstRequest = true;

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mContext = view.getContext();


    }


    public void showFABs() {
        btn_add.show();
        btn_current_location.show();
        btn_map_type.show();
        btn_map_type.setImageResource(R.drawable.ic_map_type);
    }

    public void hideFABs() {
        btn_add.hide();
        btn_current_location.hide();


        if (mstb_map_type.getVisibility() == View.VISIBLE)
            btn_map_type.callOnClick();
        btn_map_type.hide();
    }


    /******************************************************
     ****                onMapReady                    ****
     ******************************************************/

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        // set up clustering and read from database
        Log.d(TAG, "onMapReady: called");
        loadFirebaseMapData();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // location updates every second
        mLocationRequest.setFastestInterval(1000);
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
        btn_current_location = getView().findViewById(R.id.btn_current_location);
        btn_current_location.setOnClickListener(new View.OnClickListener() {
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

        /*** setup add new fountain button ***/
        btn_add = getView().findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null) {

                    Log.d(TAG, "onClick: add button clicked!");

                    /* TODO: decide whether or not to move the camera?? */

                    // get current location
                    final LatLng latLng = new LatLng(mLastLocation.getLatitude() - 0.0000, mLastLocation.getLongitude());

                    // zoom into location
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(20));


                    // hide fab's
                    hideFABs();


                    // launch new fragment
                    addNewLocationFragment = new AddNewLocationFragment(MapFragment.this);
                    getChildFragmentManager().beginTransaction().replace(R.id.add_new_location_container,
                            addNewLocationFragment).addToBackStack(null).commit();

                    iMainActivity.sendCurrentLocation(addNewLocationFragment, latLng);

                }
            }
        });

        mstb_map_type = getView().findViewById(R.id.map_type_selector);
        mstb_map_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mstb_map_type.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                if (mMap != null) {

                    Log.d(TAG, "onClick: clicked mtsb");

                    boolean[] options = mstb_map_type.getStates();
                    if (options[0])
                        mMap.setMapType(mMap.MAP_TYPE_NORMAL);
                    else if (options[1])
                        mMap.setMapType(mMap.MAP_TYPE_HYBRID);
                }
            }
        });


        btn_map_type = getView().findViewById(R.id.btn_map_type);
        btn_map_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mMap != null) {

                    Log.d(TAG, "onClick: map type button clicked");

                    // show or hide map type toggle switch
                    if (mstb_map_type.getVisibility() == View.GONE) {
                        btn_map_type.setImageResource(R.drawable.ic_map_type_close);

                        // temp solution 4 bug: https://stackoverflow.com/questions/51919865/disappearing-fab-icon-on-navigation-fragment-change
                        btn_map_type.hide();
                        btn_map_type.show();

                        Log.d(TAG, "onClick: close resource");
                        mstb_map_type.setVisibility(View.VISIBLE);

                    } else if (mstb_map_type.getVisibility() == View.VISIBLE) {
                        btn_map_type.setImageResource(R.drawable.ic_map_type);
                        btn_map_type.hide();
                        btn_map_type.show();

                        Log.d(TAG, "onClick: open resource");
                        mstb_map_type.setVisibility(View.GONE);
                    }


                }
            }
        });


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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("marker", "map clicked");

                if (getChildFragmentManager().getBackStackEntryCount() > 0) {
                    Log.d("marker", "popping backstack");
                    getChildFragmentManager().popBackStack();

                    showFABs();

                }
            }
        });


    }

    private void markerItemClicked(MarkerItem markerItem) {

        // get marker location
        final LatLng latLng = new LatLng(markerItem.getPosition().latitude - 0.0015,
                markerItem.getPosition().longitude);

        // center on location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));


        markerDetailFragment = new MarkerDetailFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.marker_detail_container,
                markerDetailFragment).addToBackStack(null).commit();

        hideFABs();

        iMainActivity.sendMarkerID(markerDetailFragment, markerItem.getID());
    }


    /*** setup for map pin clustering ***/
    private void setUpClusterManager(GoogleMap googleMap) {


        /* TODO: getContext is null sometimes?*/


        if (mContext == null)
            Log.d(TAG, "setUpClusterManager: context is null");

        ClusterManager<MarkerItem> clusterManager = new ClusterManager<>(mContext, googleMap);
        clusterManager.setRenderer(new MarkerClusterRenderer(mContext, googleMap, clusterManager));
        googleMap.setOnCameraIdleListener(clusterManager);
        List<MarkerItem> items = getItems();

        Log.d(TAG, "setUpClusterManager: " + items.size());

        clusterManager.addItems(items);
        clusterManager.cluster();


        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerItem>() {
            @Override
            public boolean onClusterItemClick(MarkerItem markerItem) {

                Log.d("marker", "Marker clicked");

                markerItemClicked(markerItem);

                Log.d("marker", markerItem.getPosition().latitude + "," + markerItem.getPosition().longitude);

                return true;
            }
        });


        googleMap.setOnMarkerClickListener(clusterManager);


    }


    /*** list which serves as the database for drawing pins on the map ***/
    private List<MarkerItem> getItems() {
        return markerItemList;

    }

    private void loadFirebaseMapData() {

        Log.d(TAG, "loadFirebaseMapData: called");

        markerItemList = new ArrayList<>();

        // start point for data access
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Locations");



        // inefficient because we have to pull the entire location database anytime there is a change


        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // clear the list and start over
                markerItemList.clear();

                // iterate through marker id's
                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    String id = child.getKey();
                    double lat = (double) child.child("latitude").getValue();
                    double lng = (double) child.child("longitude").getValue();

                    Log.d(TAG, "get items marker 1:" + lat + ", " + lng + ", " + id);
                    markerItemList.add(new MarkerItem(id, new LatLng(lat, lng)));

                }

                if (!incomingMarkerID.equals("")) {

                    final String markerID = incomingMarkerID;

                    for (MarkerItem markerItem : getItems()) {

                        Log.d(TAG, "onMapReady: " + markerItem.getID());
                        if (markerItem.getID().equals(markerID)) {
                            markerItemClicked(markerItem);

                            break;
                        }
                    }
                }

                setUpClusterManager(mMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: marker, database error");

            }
        });


        /* more efficient way maybe

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                children.iterator().next().

                String id = child.getKey();
                double lat = (double) child.child("latitude").getValue();
                double lng = (double) child.child("longitude").getValue();

                Log.d(TAG, "get items marker 1:" + lat + ", " + lng + ", " + id);
                markerItemList.add(new MarkerItem(id, new LatLng(lat, lng)));




                // TODO: find a better place for this

                if (!incomingMarkerID.equals("")) {

                    final String markerID = incomingMarkerID;

                    for (MarkerItem markerItem : getItems()) {

                        Log.d(TAG, "onMapReady: " + markerItem.getID());
                        if (markerItem.getID().equals(markerID)) {
                            markerItemClicked(markerItem);

                            break;
                        }
                    }
                }


                setUpClusterManager(mMap);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        */


    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (mFusedLocationClient.getApplicationContext() != null) {
                    mLastLocation = location;

                    if (mFirstRequest && incomingMarkerID.equals("")) {

                        // move the camera
                        mMap.moveCamera(CameraUpdateFactory.
                                newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

                        mFirstRequest = false; // well i guess it works...
                    }


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




