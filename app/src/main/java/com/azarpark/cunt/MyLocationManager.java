package com.azarpark.cunt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MyLocationManager {

    // implementation 'com.google.android.gms:play-services-maps:17.0.0'

    /* use :
            MyLocationManager myLocationManager = new MyLocationManager(PayAndExitParkedPlateActivity.this, PayAndExitParkedPlateActivity.this, (lat, lon) -> {
                        System.out.println("----------> lat : " + lat);
                        System.out.println("----------> lon : " + lon);
                    });
                    myLocationManager.requestCurrentLocation();
    */


    private final int REQUEST_CHECK_SETTINGS = 2060;
    private final Context context;
    private final Activity activity;

    public MyLocationManager(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void requestCurrentLocation(OnLocationReceive onLocationReceive) {

        if (!checkLocationPermissions()) {
            Toast.makeText(context, "دسترسی مکانیابی را قبول کنید و دوباره تلاش کنید", Toast.LENGTH_SHORT).show();
            requestLocationPermission();
        } else if (!gpsIsEnabled()) {
            Toast.makeText(context, "جی پی اس دستگاه را روشن کنید و دوباره تلاش کنید", Toast.LENGTH_SHORT).show();
            displayLocationSettingsRequest();
        } else
            getCurrentLocation(onLocationReceive);

    }

    private boolean checkLocationPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestLocationPermission() {
        int REQUEST_PERMISSIONS_REQUEST_CODE = 2050;
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private boolean gpsIsEnabled() {

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    private void displayLocationSettingsRequest() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        final String TAG = "resPMain";
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.i(TAG, "All location settings are satisfied.");
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        Log.i(TAG, "PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                    break;
            }
        });
    }

    private void getCurrentLocation(OnLocationReceive onLocationReceive) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(context)
                                .removeLocationUpdates(this);
                        if (locationResult.getLocations().size() > 0) {
                            int latestLocIndex = locationResult.getLocations().size() - 1;
                            double lati = locationResult.getLocations().get(latestLocIndex).getLatitude();
                            double longi = locationResult.getLocations().get(latestLocIndex).getLongitude();
                            onLocationReceive.onReceive(lati,longi);
                        } else {
                            System.out.println("----------> nooooooo");
                        }
                    }
                }, Looper.getMainLooper());

    }

    public static interface OnLocationReceive {
        public void onReceive(double lat, double lon);
    }
}
