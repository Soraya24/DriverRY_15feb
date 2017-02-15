package com.akexorcist.googledirection.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class NotificationAlert extends AppCompatActivity {

    //Explicit
    private Button okButton, noButton;
    private String[] loginStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_alert);

        loginStrings = getIntent().getStringArrayExtra("Login");

        //Bind Widget
        okButton = (Button) findViewById(R.id.button5);
        noButton = (Button) findViewById(R.id.button6);

        //no Controller
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    //เปลี่ยน Status ที่ jobTABLE
                    EditStatusTo2 editStatusTo2 = new EditStatusTo2(NotificationAlert.this,
                            loginStrings[0],"0", "1");
                    editStatusTo2.execute();

                    //เปลี่ยน Status ที่ userTABLE
                    EditStatusDriver editStatusDriver = new EditStatusDriver(NotificationAlert.this,
                            loginStrings[0], "1");
                    editStatusDriver.execute();

                    Intent intent = new Intent(NotificationAlert.this, ConfirmJob.class);
                    intent.putExtra("Login", loginStrings);
                    startActivity(intent);
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });



        //ok Controller
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    //For userTABLE
                    EditStatusDriver editStatusDriver = new EditStatusDriver(NotificationAlert.this,
                            loginStrings[0], "2");
                    editStatusDriver.execute();
                    Log.d("29decV2", "Result userTABLE ==> " + editStatusDriver.get());

                    //For jobTABLE
                    EditStatusTo2 editStatusTo2 = new EditStatusTo2(NotificationAlert.this,
                            loginStrings[0],"0", "2");
                    editStatusTo2.execute();
                    Log.d("29decV2", "Result jobTABLE ==> " + editStatusTo2.get());

                    Intent intent = new Intent(NotificationAlert.this, ServiceActivity.class);
                    intent.putExtra("Login", loginStrings);
                    startActivity(intent);
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }   // onClick
        });

    }   // Main Method

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}   // Main Class
