package com.mst.karsac.Utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;


public class LocationHandler {
    public double[] getLocation(Context context) {
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return new double[]{latitude, longitude};
    }
}
