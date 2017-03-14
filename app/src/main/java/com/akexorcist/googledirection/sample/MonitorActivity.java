package com.akexorcist.googledirection.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MonitorActivity extends FragmentActivity implements OnMapReadyCallback {

    //Explicit
    private GoogleMap mMap;
    private String[] loginString; //นี่คือ Array ของ user ที่ Login อยู่
    private LatLng destinationLatLng;
    private LocationManager locationManager;
    private Criteria criteria;
    private double userLatADouble = 0, userLngADouble = 0;
    private Marker destinationMarker, userMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_monitor_layout);
        // For Create Fragment
        forCreateFragment();

        // Get Value Form Intent
        getValueFromIntent();

        //Setup Parameter
        setupParameter();

    }   // Main Method

    @Override
    protected void onResume() {
        super.onResume();

        refreshLocation();

    }

    private void refreshLocation() {

        locationManager.removeUpdates(locationListener);

        //For Network
        Location networkLocation = monitorLocation(LocationManager.NETWORK_PROVIDER);
        if (networkLocation != null) {
            userLatADouble = networkLocation.getLatitude();
            userLngADouble = networkLocation.getLongitude();
        }

        //For GPS
        Location gpsLocation = monitorLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation != null) {
            userLatADouble = gpsLocation.getLatitude();
            userLngADouble = gpsLocation.getLongitude();

        }

        Log.d("14MarchV1", "userLat ==> " + userLatADouble);
        Log.d("14MarchV1", "userLng ==> " + userLngADouble);
    }

    @Override
    protected void onStop() {
        super.onStop();

        locationManager.removeUpdates(locationListener);

    }

    public Location monitorLocation(String strProvider) {

        Location location = null;

        if (locationManager.isProviderEnabled(strProvider)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            locationManager.requestLocationUpdates(strProvider, 1000, 10, locationListener);
            location = locationManager.getLastKnownLocation(strProvider);
        }

        return location;

    }


    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            userLatADouble = location.getLongitude();
            userLngADouble = location.getLongitude();


        }   // onLocationChange

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void setupParameter() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
    }

    private void getValueFromIntent() {

        try {

            loginString = getIntent().getStringArrayExtra("Login");
            double lat = getIntent().getDoubleExtra("Lat", 13.694019);
            double lng = getIntent().getDoubleExtra("Lng", 100.648247);
            destinationLatLng = new LatLng(lat, lng);

        } catch (Exception e) {
            Log.d("14MarchV1", "e getValue ==> " + e.toString());

        }

    }   // getValue

    private void forCreateFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Create Map and Setup Center Map
        createMapAndSetup();

        //Create Marker
        createMarker();

    }   // onMapReady

    private void createMarker() {

        //for Destination
        destinationMarker = mMap.addMarker(new MarkerOptions()
        .position(destinationLatLng)
        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_desination)));

        //for User
        userMarker = mMap.addMarker(new MarkerOptions()
        .position(new LatLng(userLatADouble, userLngADouble))
        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_car2)));


    }   // createMarker

    private void createMapAndSetup() {
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(userLatADouble, userLngADouble),16));
    }

}   // Main Class
