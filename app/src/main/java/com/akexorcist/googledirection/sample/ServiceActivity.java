package com.akexorcist.googledirection.sample;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

import static android.R.attr.name;

public class ServiceActivity extends FragmentActivity implements OnMapReadyCallback, DirectionCallback, View.OnClickListener {

    //Explicit
    private GoogleMap mMap;
    private TextView nameTextView, phoneTextView, dateTextView, timeTextView;
    private ImageView imageView;
    private Button button;
    private String[] loginStrings;
    private MyConstant myConstant;
    private String[] jobString;
    private String phoneString;
    private LocationManager locationManager;
    private Criteria criteria;
    private double latADouble, lngADouble;
    private LatLng latLng;
    private int hourWaitStartAnInt, minusWaitStartInt,
            hourWaitEndAnInt, minusWaitEndAnInt;
    private boolean aBoolean = true, aBoolean2 = false, aBoolean3 = true;
    private int startTimeCountHour = 0;
    private int startTimeCountMinus = 0;
    private int endTimeCountHour, endTimeCountMinus, endTimeCountDay;
    private String strStartCountTime, endCountTime;

    private String serverKey = "AIzaSyD_6HZwKgnxSOSkMWocLs4-2AViQuPBteQ";
    private LatLng camera = new LatLng(13.667837, 100.621810);
    private LatLng origin = new LatLng(13.668880, 100.623441);
    private LatLng destination = new LatLng(13.678262, 100.623612);
    private String[] colors = {"#7f000077", "#7f31c7c5", "#7fff8a00"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_service_layout);


        //Bind WIdget
        bindWidget();

        //Get Value From Intent โดยการดึงข้อมูล ของผู้ที่กำลัง Login อยู่นั้นเอง
        getValueFromIntent();


        //Get Value From JSON
        getValueFromJSON();


        //Setup For Get Location
        setupForGetLocation();

        //Setup MapFragment
        setupMapFragment();

        //Button Controller
        buttonController();



    }   //Main Method

    private void googleMapController(String strLat, String strLng) {

        Button button = (Button) findViewById(R.id.btnGoogleMap);

        final String strUri = "geo:0,0?q="+strLat+", "+strLng+" (" + name + ")";

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                startActivity(intent);

            }
        });


    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupForGetLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
    }

    private void getValueFromJSON() {
        myConstant = new MyConstant();

        try {

            GetJob getJob = new GetJob(ServiceActivity.this);
            getJob.execute(myConstant.getUrlGetJobWhereID());

        } catch (Exception e) {
            Log.d("13MarchV1", "e getValueFromJSON ==> " + e.toString());
        }

    }   // getValue

    private void getValueFromIntent() {
        loginStrings = getIntent().getStringArrayExtra("Login");
        Log.d("28decV2", "id_Passenger ==>" + loginStrings[0]);
    }   // getValueFromIntent

    private void buttonController() {

        button.setOnClickListener(ServiceActivity.this);

    }   // buttonController

    private void confirmClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceActivity.this);
        builder.setCancelable(false);
        builder.setTitle("กดถ่ายรูป");
        builder.setMessage("ยืนยันการส่งข้อมูล");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                aBoolean = false;   // คลิกอีกครั้งจะไม่มาที่นี่
                aBoolean2 = true;
                //เปลี่ยน Label Button เป็น ออกเดินทาง
                button.setText(getResources().getString(R.string.start));

                //Intent to PhotoActivity
                Intent intent = new Intent(ServiceActivity.this, PhotoActivity.class);
                intent.putExtra("id_job", jobString[0]);
                intent.putExtra("phone_customer", phoneString);
                intent.putExtra("id_Driver", loginStrings[0]);
                aBoolean2 = true;
                startActivity(intent);
                dialogInterface.dismiss();

            }
        });
        builder.show();


    }

    ;

    private void findWaitMinus() {

        Log.d("19janV1", "เวลาที่เริ่มจับ ==> " + strStartCountTime);
        Log.d("19janV1", "เวลาที่หยุดจับ ==> " + endCountTime);

        //แก้ Error แบบ กำปันทุบดิน
        if (strStartCountTime == null) {
            Log.d("19janV1", "ทำงานใน if");

        }

        String[] startStrings = strStartCountTime.split(":");
        String[] endStrings = endCountTime.split(":");
        int startMinus = (Integer.parseInt(startStrings[0]) * 60) + Integer.parseInt(startStrings[1]);
        int endMinus = (Integer.parseInt(endStrings[0]) * 60) + Integer.parseInt(endStrings[1]);

        Log.d("19janV1", "startMinus ==> " + startMinus);
        Log.d("19janV1", "endMinus ==> " + endMinus);

        int countTimeMinus = endMinus - startMinus;
        Log.d("19janV1", "countTimeMinus ==> " + countTimeMinus);

        try {

            //Edit Status of jobTABLE
            UpdateCountMinus updateCountMinus = new UpdateCountMinus(ServiceActivity.this,
                    loginStrings[0], "3", endCountTime, Integer.toString(countTimeMinus));
            updateCountMinus.execute();

            //Edit Status of userTABLE
            EditStatusDriver editStatusDriver = new EditStatusDriver(ServiceActivity.this,
                    loginStrings[0], "4");
            editStatusDriver.execute();

            String strResult = updateCountMinus.get();
            Log.d("19janV1", "ผลของการ Update ==> " + strResult);


            Intent intent = new Intent(ServiceActivity.this, MonitorActivity.class);
            intent.putExtra("Login", loginStrings);
            intent.putExtra("Lat", destination.latitude);
            intent.putExtra("Lng", destination.longitude);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.d("19janV1", "e ==> " + e.toString());
        }


    }   // findWaitMinus

    private void bindWidget() {
        nameTextView = (TextView) findViewById(R.id.textView3);
        phoneTextView = (TextView) findViewById(R.id.textView4);
        dateTextView = (TextView) findViewById(R.id.textView5);
        timeTextView = (TextView) findViewById(R.id.textView6);
        imageView = (ImageView) findViewById(R.id.imageView2);
        button = (Button) findViewById(R.id.button4);
    }   // bindWidget

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("14novV2", "Resume Worked");

        //For Find Location
        forFindLocation();

        //Resume After Take Photo
        if (!aBoolean2) {
            aBoolean2 = getIntent().getBooleanExtra("aBoolean2", false);
        }
        Log.d("15febV2", "ค่าของ aBoolean2 ก่อน Check If ==> " + aBoolean2);
        // ถ้า aBoolean2 เป็น true
        if (aBoolean2) {
            aBoolean = false;
            afterReume();
        }

        //หลังจาก ไปถ่ายรูป aBoolean จะเป็น False
        if (!aBoolean) {

            Log.d("14novV2", "Min ==>" + 0);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    //จุดที่เริ่มจับเวลา และส่ง SMS
                    Log.d("28decV1", "หน่วงเวลา 60 วินาทีเรียบร้อบแล้ว");
                    myCounterTime();
                    mySentSMS(phoneString);


                }   // run
            }, 60000);

        }   // if


    }   // onResume

    private void forFindLocation() {
        //by Network
        Location networkLocation = myFindLocation(LocationManager.NETWORK_PROVIDER);
        if (networkLocation != null) {
            latADouble = networkLocation.getLatitude();
            lngADouble = networkLocation.getLongitude();

        }

        //by GPS
        Location gpsLocation = myFindLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation != null) {
            latADouble = gpsLocation.getLatitude();
            lngADouble = gpsLocation.getLongitude();

        }

        Log.d("8novV1", "Lat ==> " + latADouble);
        Log.d("8novV1", "lug ==> " + lngADouble);
    }

    //คือ methodที่ทำงาน หลังจาก ถ่ายรูปมิเตอร์ เรียบร้อยแล้ว

    private void afterReume() {

        try {

            //=======================================================
            // หาการมาก่อนนัด หลัง นัด
            //=======================================================

            Calendar calendar = Calendar.getInstance(); // เวลาที่มาถึง
            Log.d("28decV2", "เวลาที่มา ==> " + calendar.getTime().toString());
            int intDay = calendar.get(Calendar.DAY_OF_MONTH);
            int intHour = calendar.get(Calendar.HOUR_OF_DAY);
            int intMinus = calendar.get(Calendar.MINUTE);
            Log.d("28decv2", "intHour ==> " + intHour);
            Log.d("28decV2", "intMinus ==> " + intMinus);
            Log.d("28decV2", "เวลานัดหมาย ==> " + jobString[5]);


            Calendar calendar1 = Calendar.getInstance();
            String[] timeStrings = jobString[5].split(Pattern.quote(":"));
            Log.d("28decV2", "Hour ที่นัดหมาย ==> " + timeStrings[0]);
            Log.d("28decV2", "Minus ที่นัดหมาย ==> " + timeStrings[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStrings[0]));
            calendar1.set(Calendar.MINUTE, Integer.parseInt(timeStrings[1]));
            Log.d("28decV2", "เวลาที่นัด ==> " + calendar1.getTime().toString());

            Log.d("28decV2", "มาก่อนเวลา มั้ง ==> " + calendar.before(calendar1));

            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String strArriveTime = dateFormat.format(calendar.getTime());
            Log.d("28decV2", "strArriveTime ==> " + strArriveTime);
            String strPointTime = dateFormat.format(calendar1.getTime());
            Log.d("28decV2", "strPointTime ==> " + strPointTime);


            if (calendar.before(calendar1)) {
                //มาก่อนเวลานัด
                strStartCountTime = strPointTime;

            } else {
                //มาหลังเวลานัด
                strStartCountTime = strArriveTime;
            }


            //For userTABLE_ry กำหนด Status เท่ากับ 3
            EditStatusDriver editStatusDriver = new EditStatusDriver(ServiceActivity.this,
                    loginStrings[0], "3");
            editStatusDriver.execute();
            Log.d("28decV2", "Result userTABLE ==> " + editStatusDriver.get());

            //For jobTABLE
            EditStatusTo2 editStatusTo2 = new EditStatusTo2(ServiceActivity.this,
                    loginStrings[0], "2", "3");
            editStatusTo2.execute();
            Log.d("28decV2", "Result jobTABLE ==> " + editStatusTo2.get());

            //==============================================================
            //Update TimeArrive
            //==============================================================


            EditTimeArrive editTimeArrive = new EditTimeArrive(ServiceActivity.this,
                    loginStrings[0], "3", strArriveTime, strStartCountTime);
            editTimeArrive.execute();
            Log.d("28decV2", "Result timeArrive Update ==> " + editTimeArrive.get());


        } catch (Exception e) {
            e.printStackTrace();
        }


    }   // afterResume

    private void mySentSMS(String phoneString) {

        Log.d("28decV1", "phoneCustomer ==> " + phoneString);

//       Uri uri = Uri.parse("smsto" + phoneString);
//       Intent intent = new Intent(Intent.ACTION_SENDTO);
//       intent.setData(uri);
//       intent.putExtra("sms_body", "Test by MasterUNG");
//       startActivity(intent);

//       SmsManager smsManager = SmsManager.getDefault();
//       smsManager.sendTextMessage(phoneString, null, "Test Master", null null);


    }   //mySentSMS

    //ทำหน้าที่จับเวลา ที่ต้องรอลูกค้า
    private void myCounterTime() {

        //Get Time WaitStart
        Calendar calendar = Calendar.getInstance();
        hourWaitStartAnInt = calendar.get(Calendar.HOUR_OF_DAY);
        minusWaitStartInt = calendar.get(Calendar.MINUTE);
        Log.d("14novV2", "HrStarts ==>" + hourWaitStartAnInt);
        Log.d("14novV2", "MinStart ==>" + minusWaitStartInt);

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);

    }

    public Location myFindLocation(String strProvider) {

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

            latADouble = location.getLatitude();
            lngADouble = location.getLongitude();

        }   //onLocationChange

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

    @Override
    public void onClick(View view) {

        //ค่าเริ่มต้นของ aBoolean มีค่า True แต่ถ้าคลิ๊กครั้งแรก จะมีค่า False
        // คลิกเมื่อถึงที่รับ
        if (aBoolean) {

            //ก่อนออกเดินทาง
            //Confirm Click ย้ำคิด ย้ำทำ ว่า คลิกแล้วนะ
            confirmClick();

        } else {

            //นี่คือ สภาวะ หลังจากถ่ายรูปเสร็จ และ คลิ๊กออกเดินทาง

            myAlertStart();


        }   //if

        Log.d("28decV2", "aBoolean ==> " + aBoolean);

    }   // onClick

    private void myAlertStart() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceActivity.this);
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.mk_car2);
        builder.setTitle("คุณต้องการยืนยันออกเดินทาง");
        builder.setMessage("ยืนยันการออกเดินทาง");
        builder.setNegativeButton("ไม่ยืนยัน", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Log.d("13MarchV1", "นี่คือ สภาวะ คลิกออกเดินทาง");

                //เริ่มเดินทาง หรือหยุดเวลา ที่จับ
                Calendar calendar = Calendar.getInstance();
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                endCountTime = dateFormat.format(calendar.getTime());
                Log.d("28decV2", "endcountTime หรือเวลาออกเดินทาง ==> " + endCountTime);


                findWaitMinus();

                dialogInterface.dismiss();

            }
        });



    }


    // GetJob จะส่งค่า id ของคนขับ และ Status ที่มีค่าเท่ากับ 2 ไป
    // Select Where ที่ Table jobTABLE
    // เพื่อหา Record ที่ คนขับคนนี่รับงานอยู่
    private class GetJob extends AsyncTask<String, Void, String> {


        //Explicit
        private Context context;

        public GetJob(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormEncodingBuilder()
                        .add("isAdd", "true")
                        .add("ID_passenger", loginStrings[0])
                        .add("Status", "2")
                        .build();
                Request.Builder builder = new Request.Builder();
                Request request = builder.url(strings[0]).post(requestBody).build();
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }   //doInBack

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("7novV1", "Result ==>" + s);

            try {

                JSONArray jsonArray = new JSONArray(s);

                String[] columnStrings = myConstant.getJobStrings();

                jobString = new String[columnStrings.length];

                for (int i = 0; i < columnStrings.length; i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    jobString[i] = jsonObject.getString(columnStrings[i]);
                    Log.d("7novV2", "jobString(" + i + ") ==> " + jobString[i]);

                }   // for

                //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                //  การสร้าง Marker of จุดรับ และ จุดส่ง
                //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                //Create Marker Start
                origin = new LatLng(Double.parseDouble(jobString[7]),
                        Double.parseDouble(jobString[8]));

                //Create Marker End
                destination = new LatLng(Double.parseDouble(jobString[10]),
                        Double.parseDouble(jobString[11]));

                requestDirection();

                //Show Text
                GetPassenger getPassenger = new GetPassenger(context, jobString);
                getPassenger.execute(myConstant.getUrlGetPassengerWhereID());

                googleMapController(jobString[7], jobString[8]);


            } catch (Exception e) {
                Log.d("7novV2", "e ==>" + e.toString());
            }

        }   // onPost

    }   //GetJob Class


    private class GetPassenger extends AsyncTask<String, Void, String> {

        //Explicit
        private Context context;
        private String[] resultStrings;

        public GetPassenger(Context context, String[] resultStrings) {
            this.context = context;
            this.resultStrings = resultStrings;
        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormEncodingBuilder()
                        .add("isAdd", "true")
                        .add("id", resultStrings[1])
                        .build();
                Request.Builder builder = new Request.Builder();
                Request request = builder.url(strings[0]).post(requestBody).build();
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();

            } catch (Exception e) {
                Log.d("7novV3", "e ==>" + e.toString());
                return null;
            }

        }   //doInBack

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("7novV3", "Passenger ==>" + s);

            try {

                JSONArray jsonArray = new JSONArray(s);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                nameTextView.setText(jsonObject.getString("Name"));
                phoneString = jsonObject.getString("Phone");
                phoneTextView.setText("Phone = " + phoneString);
                dateTextView.setText("วันที่ไปรัย = " + jobString[4]);
                timeTextView.setText("เวลาที่ไปรับ = " + jobString[5]);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("7novV3", "ClickPhone = " + phoneString);

                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:=" + phoneString));
                        if (ActivityCompat.checkSelfPermission(ServiceActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        startActivity(intent);

                    }   //onClick
                });

            } catch (Exception e) {
                Log.d("7novV3", "e onPost ==> " + e.toString());
            }


        }   // onPost
    } // GetPassenger Class


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {

            latLng = new LatLng(latADouble, lngADouble);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

            //Create Marker Driver
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_driver)));


        } catch (Exception e) {
            Toast.makeText(ServiceActivity.this, "ไม่สามารถหาพิกัด", Toast.LENGTH_SHORT).show();

        }


    }  //onMapReady

    //  Method ที่ทำหน้าที่ แนะนำเส้นทาง การไปได้ระหว่างจุดสองจุด ไม่เกิน 3 เส้น
    public void requestDirection() {

        // Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .alternativeRoute(true)
                .execute(ServiceActivity.this);
    }   // requestDirection


    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        // Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {

            //นี่คือการสร้าง Marker ของจุดไปรับลูกค้า Origin
            //และ จุดไปส่งลูกค้า Destination
            mMap.addMarker(new MarkerOptions()
                    .position(origin)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_origin)));

            mMap.addMarker(new MarkerOptions()
                    .position(destination)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.mk_desination)));

// ถ้าต้องการ Routing จำนวน 3 เส้น ให้ใช้ ตรงนี้
            // for (int i = 0; i < direction.getRouteList().size(); i++) {

            // ต้องการ Routing เพียงเส้นใกล้สุดเส้นเดียว
            for (int i = 0; i < 1; i++) {
                Route route = direction.getRouteList().get(i);
                String color = colors[i % colors.length];
                ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.parseColor(color)));

                Log.d("13MarchV1", "ระยะ ระหว่างจุด ==> " + route.getLegList().get(0).getDistance().getText());
                Log.d("13MarchV1", "เวลา ระหว่างจุด ==> " + route.getLegList().get(0).getDuration().getText());


            }   // for

            //  btnRequestDirection.setVisibility(View.GONE);
        }
    }   // onDirectionSuccess

    @Override
    public void onDirectionFailure(Throwable t) {
        //  Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }   // onDriectionFeilure


}  //Main Class