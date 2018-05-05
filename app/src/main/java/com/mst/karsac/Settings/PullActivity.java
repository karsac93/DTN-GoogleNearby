package com.mst.karsac.Settings;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;
import com.mst.karsac.Utils.LocationHandler;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.connections.BackgroundService;

public class PullActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String TAG = "PullActivity";

    private GoogleMap mMap;
    private Marker marker;
    TextView location;
    EditText tags, radius, location_edit;
    Button save_pull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        location = findViewById(R.id.location);
        tags = findViewById(R.id.tags_enter);
        radius = findViewById(R.id.radius);
        save_pull = findViewById(R.id.save_pull_details);
        location_edit = findViewById(R.id.location_edit);
        String tags_temp = SharedPreferencesHandler.getStringPreferences(getApplicationContext(), Setting.TAG_KEYS);
        if(tags_temp.trim().length() > 0)
            tags.setText(tags_temp);
        int radius_temp = SharedPreferencesHandler.getRadiusPreferences(getApplicationContext(), Setting.RADIUS);
        radius.setText(String.valueOf(radius_temp));

        save_pull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tags_string = tags.getText().toString();
                if (tags_string.trim().length() > 0) {
                    String[] tags_array = tags_string.split(",");
                    for (String interest : tags_array) {
                        GlobalApp.dbHelper.insertInterest(interest.trim().toLowerCase(), 0, 0.5f);
                    }
                    Log.d(TAG, "All the tags have been inserted");
                    SharedPreferencesHandler.setStringPreferences(getApplicationContext(), Setting.TAG_KEYS, tags_string.toLowerCase());
                }
                String lat_long_location = location_edit.getText().toString();
                if (lat_long_location.contains(",")) {
                    String temp = lat_long_location.substring(lat_long_location.indexOf(":") + 1, lat_long_location.length());
                    SharedPreferencesHandler.setStringPreferences(getApplicationContext(), Setting.LAT_LON_KEY, temp);
                    Log.d(TAG, "lat lon set in shared preferences");
                }
                String radius_string = radius.getText().toString();
                if (radius_string.trim().length() > 0 && lat_long_location.contains(",")) {
                    try {
                        int radius_int = Integer.parseInt(radius_string);
                        SharedPreferencesHandler.setIntPreference(getApplicationContext(), Setting.RADIUS, radius_int);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Enter a valid radius!", Toast.LENGTH_SHORT).show();
                    }
                }
                SharedPreferencesHandler.setStringPreferences(getApplicationContext(), Setting.MODE_SELECTION, Setting.PULL);
                Toast.makeText(getApplicationContext(), "Pull mode preferences saved!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(15.0f);
        if (!(SharedPreferencesHandler.getStringPreferences(getApplicationContext(), Setting.LAT_LON_KEY).trim().length() > 0)) {
            double[] my_location = new LocationHandler().getLocation(this);
            if (my_location[0] != 0.0 && my_location[1] != 0.0) {
                LatLng latLng = new LatLng(my_location[0], my_location[1]);
                marker = mMap.addMarker(new MarkerOptions().position(latLng));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                mMap.moveCamera(cameraUpdate);
                mMap.animateCamera(zoom);
                location_edit.setText(latLng.latitude + ", " + latLng.longitude);
            }
        } else {
            String location_temp = SharedPreferencesHandler.getStringPreferences(getApplicationContext(), Setting.LAT_LON_KEY);
            String[] my_location = location_temp.split(",");
            LatLng latLng = new LatLng(Double.parseDouble(my_location[0]), Double.parseDouble(my_location[1]));
            marker = mMap.addMarker(new MarkerOptions().position(latLng));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            mMap.moveCamera(cameraUpdate);
            mMap.animateCamera(zoom);
            location_edit.setText(latLng.latitude + ", " + latLng.longitude);
        }

        // Add a marker in Sydney and move the camera
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                String lat = String.valueOf(latLng.latitude);
                String lng = String.valueOf(latLng.longitude);
                marker.setPosition(latLng);
                marker = mMap.addMarker(new MarkerOptions().position(latLng));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                mMap.moveCamera(cameraUpdate);
                mMap.animateCamera(zoom);
                Log.d("PULLActivity", lat + "|" + lng);
                location_edit.setText(latLng.latitude + ", " + latLng.longitude);
            }
        });
    }
}

