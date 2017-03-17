package com.akexorcist.googledirection.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MonitorActivity extends FragmentActivity implements OnMapReadyCallback, DirectionCallback {

    //Explicit
    private GoogleMap mMap;
    private String[] loginStrings;  // นี่คือ Array ของ user ที่ Login อยู่
    private LatLng destinationLatLng, startLatLng;
    private LocationManager locationManager;
    private Criteria criteria;
    private double userLatADouble = 0, userLngADouble = 0;
    private Marker destinationMarker, userMarker;
    private String serverKey = "AIzaSyDE4-7-stlHCxH4BB539QF9OM4VU1u6HSs";
    private boolean aBoolean = true;
    private Marker marker;
    private Handler handler = new Handler();
    private int secAnInt = 60000;    // ความถี่ ของการใช้ API
    private boolean aBoolean2 = true;
    private double somLengthADouble = 0.0;




    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {

                marker.remove();


                LatLng latLng = new LatLng(userLatADouble, userLngADouble); // จุดล่าสุด

                createRoutingMap(startLatLng, latLng);

                createCarMarker();


            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.postDelayed(runnable, secAnInt);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_monitor_layout);
        //For Create Fragment
        forCreateFragment();

        //Get Value Form Intent
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

        //myLoop();


    }   // refreshLocation

    @Override
    protected void onStop() {
        super.onStop();

        locationManager.removeUpdates(locationListener);
        aBoolean = false;

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

            userLatADouble = location.getLatitude();
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

            double[] doubles = new double[]{13.669993, 100.621239};
            loginStrings = getIntent().getStringArrayExtra("Login");
            double lat = getIntent().getDoubleExtra("Lat", doubles[0]);
            double lng = getIntent().getDoubleExtra("Lng", doubles[1]);
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

        createMarker();

        //Create Routing Map
        startLatLng = new LatLng(userLatADouble, userLngADouble);
        createRoutingMap(startLatLng, destinationLatLng);

        // myLoop();

        createCarMarker();

        handler.postDelayed(runnable, secAnInt);


    }   // onMapReady

    private void createCarMarker() {

        startLatLng = new LatLng(userLatADouble, userLngADouble);

        marker = mMap.addMarker(new MarkerOptions()
                .position(startLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_car2)));
    }

    private void myLoop() {

        try {

            //ToDo
            // Create Marker for Car
            final Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(userLatADouble, userLngADouble))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_car2)));


            //Delay
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (aBoolean) {
                        marker.remove();
                        myLoop();
                    }
                }
            }, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }   // myLoop

    private void createRoutingMap(LatLng latlngStart, LatLng latlngDestination) {

        try {

            GoogleDirection.withServerKey(serverKey)
                    .from(latlngStart)
                    .to(latlngDestination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }   // createRoutingMap


    private void createMarker() {

        //for Destination
        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(destinationLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_desination)));

        //for User
        userMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(userLatADouble, userLngADouble))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_origin)));



    }   // createMarker

    private void createMapAndSetup() {
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(userLatADouble, userLngADouble), 16));
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();

            if (aBoolean2) {

                aBoolean2 = false;
                mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.GRAY));
                //ระยะที่ สั้นสุดที่ Google แนะนำ
                String strLengthIdea = direction.getRouteList().get(0).getLegList().get(0).getDistance().getText().toString();
                Log.d("16MarchV1", "ระยะห่่าง ที่แนะนำ ==> " + strLengthIdea);
                String[] strings = strLengthIdea.split(" ");
                somLengthADouble = somLengthADouble - Double.parseDouble(strings[0]);


            }


            String strLength = direction.getRouteList().get(0).getLegList().get(0).getDistance().getText().toString();

            Log.d("16MarchV1", "ระยะที่ อ่านได้ strLength ==> " + strLength);

            String[] strings = strLength.split(" ");





            double douAdd = Double.parseDouble(strings[0]);

            if (douAdd == 1 && strings[1].equals("m")) {
                douAdd = 0;
            } else {
                if (strings[1].equals("m")) {
                    douAdd = douAdd / 1000;
                }
            }

            somLengthADouble = somLengthADouble + douAdd;
            Log.d("16MarchV1", "ระยะจริง ที่ยังไม่ลบ ระยะแนะ ==> " + somLengthADouble);



        } else {
            Log.d("14MarchV1", "direction ==> not OK");
        }


    }   // onDirectionSuccess

    @Override
    public void onDirectionFailure(Throwable t) {

        Log.d("14MarchV1", "t ==> " + t.getMessage());

    }   // onDirectionFailure

}   // Main Class