package com.schechter.thirsty;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Parcel;
import android.util.Log;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.widget.ImageView;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IMainActivity {

    private static final String TAG = "MainActivity";


    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView btn_profile = findViewById(R.id.btn_toolbar_profile);
        btn_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileFragment profileFragment = new ProfileFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                        profileFragment).addToBackStack("").commit();
            }
        });


        setSupportActionBar(toolbar);


        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);


        // set map as the default menu to open on launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                    new MapFragment()).commit();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.


        getMenuInflater().inflate(R.menu.main, menu);

        // disable the 3 dot menu
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d(TAG, "onOptionsItemSelected: " + id);

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_map:

                MapFragment mapFragment = new MapFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                        mapFragment).addToBackStack(null).commit();

                break;

            case R.id.nav_starred:

                StarredFragment starredFragment = new StarredFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                        starredFragment).addToBackStack(null).commit();

                break;

            /*case R.id.nav_visited:

                VisitedFragment visitedFragment = new VisitedFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                        visitedFragment).addToBackStack(null).commit();

                break;*/

            case R.id.nav_contributed:

                ContributedFragment contributedFragment = new ContributedFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                        contributedFragment).addToBackStack(null).commit();

                break;

            case R.id.nav_profile:

                ProfileFragment profileFragment = new ProfileFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                        profileFragment).addToBackStack(null).commit();

                break;

            case R.id.nav_share:
                break;

            case R.id.nav_rate:
                break;

            case R.id.nav_contact_us:
                break;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*** switch fragments without using the navigation drawer ***/
    public void switchContent(String markerID) {

        MapFragment mapFragment = new MapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.contentMainLayout,
                mapFragment).addToBackStack(null).commit();

        sendMarkerID(mapFragment, markerID);

    }


    /*** Communication with IMainActivity Interface ***/

    private void doFragmentTransactionWithString(Fragment fragment, String key, String message) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


        Bundle bundle = new Bundle();
        bundle.putString(key, message);
        fragment.setArguments(bundle);


        transaction.commit();
    }

    private void doFragmentTransactionWithDouble(Fragment fragment, String[] keys, Double[] values) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


        Bundle bundle = new Bundle();
        bundle.putDouble(keys[0], values[0]);
        bundle.putDouble(keys[1], values[1]);
        fragment.setArguments(bundle);

        transaction.commit();
    }


    @Override
    public void sendMarkerID(Fragment fragment, String ID) {

        doFragmentTransactionWithString(fragment, getString(R.string.marker_id_key), ID);

    }

    @Override
    public void sendCurrentLocation(Fragment fragment, LatLng latLng) {

        Log.d(TAG, "latitude in transmission: " + latLng.latitude);

        String[] keys = {getString(R.string.latitude_key), getString(R.string.longitude_key)};
        Double[] values = {latLng.latitude, latLng.longitude};

        doFragmentTransactionWithDouble(fragment, keys, values);

    }


    private void tellFragmentsBackPressed(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for(Fragment f : fragments){
            if(f instanceof BaseFragment) {
                Log.d(TAG, "tellFragmentsBackPressed: called onbackpressed");
                ((BaseFragment) f).onBackPressed();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            getSupportFragmentManager().popBackStack();
            tellFragmentsBackPressed();
        }
    }

}
