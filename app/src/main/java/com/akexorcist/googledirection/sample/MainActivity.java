package com.akexorcist.googledirection.sample;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    //Explicit
    private EditText userEditText, passwordEditText;
    private Button signInButton, signUpButton;
    private String userString, passwordString;
    private CheckBox checkBox;
    private MyManage myManage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myManage = new MyManage(MainActivity.this);

        //Bind Windget
        bindWindget();

        //Check User And Pass
        checkUserAndPass();

        //signIn Controller
        signInController();


    } // Main Method

    private void checkUserAndPass() {

        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.DATABASE_NAME,
                MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM userTABLE", null);
        cursor.moveToFirst();
        int i = cursor.getCount();  // ถ้าไม่บันทึก i=0 แต่ถ้าบันทึก i-1
        Log.d("17MarchV3", "cursor.getCount ==> " + i);

        if (i == 1) {

           myCheckUserPassword(cursor.getString(1), cursor.getString(2));

        }   //   if



    }   // checkUserAndPass

    private void signInController() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //Get Value from Edit Text
                userString = userEditText.getText().toString().trim();
                passwordString = passwordEditText.getText().toString().trim();

                //Check Space
                if (userString.equals("") || passwordString.equals("")) {
                    //Have Space

                    MyAlert myAlert = new MyAlert(MainActivity.this,
                            R.drawable.bird48, "Have Space",
                            "Please Fill All Every Blank");
                    myAlert.myDialog();

                } else {
                    //No Space
                    myCheckUserPassword(userString, passwordString);
                }

            } //onClick
        });
    }

    private void myCheckUserPassword(String strUser, String strPassword) {
        Authen authen = new Authen(MainActivity.this, strUser, strPassword);
        authen.execute("http://swiftcodingthai.com/ry/get_data_ry.php");
    }

    private void bindWindget() {
        userEditText = (EditText) findViewById(R.id.editText2);
        passwordEditText = (EditText) findViewById(R.id.editText);
        signInButton = (Button) findViewById(R.id.button);
        signUpButton = (Button) findViewById(R.id.btnSingUp);
        checkBox = (CheckBox) findViewById(R.id.chbRemember);
    }


    private class Authen extends AsyncTask<String, Void, String> {

        //Explicit
        private Context context;
        private String strUser, strPassword;

        public Authen(Context context, String strUser, String strPassword) {
            this.context = context;
            this.strUser = strUser;
            this.strPassword = strPassword;
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                OkHttpClient okHttpClient = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                Request request = builder.url(params[0]).build();
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();


            } catch (Exception e) {
                Log.d("DriverV1", "e doInBack ==>" + e.toString());
                return null;
            }


        }   //doInBack

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("DriverV1", "Json ==>" + s);

            try {

                boolean status = true;
                MyConstant myConstant = new MyConstant();
                String[] columnStrings = myConstant.getUserStrings();
                String[] loginStrings = new String[columnStrings.length];

                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i+=1) {

                   JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (strUser .equals(jsonObject.getString(columnStrings[1]))) {

                        status = false;
                        for (int i1=0;i1<columnStrings.length;i1+=1 ) {

                            loginStrings[i1] = jsonObject.getString(columnStrings[i1]);

                        } //for

                    } //if

                }//for

                //Check User
                if (status){
                    //User False
                    MyAlert myAlert = new MyAlert(context, R.drawable.doremon48,
                            "User False", "ไม่มี" + userString + "ในฐานข้อมูลของเรา");
                    myAlert.myDialog();
                } else if (!strPassword.equals(loginStrings[2])) {
                    // Password False
                    MyAlert myAlert = new MyAlert(context, R.drawable.nobita48,
                            "Password False", "Please Try Again Password False");
                    myAlert.myDialog();
                } else {

                    // Password True
                    Toast.makeText(context,"Welcome" + loginStrings[3],
                            Toast.LENGTH_SHORT).show();

                    // Add User/Password to SQLite
                    if (checkBox.isChecked()) {

                        myManage.addUser(userString, passwordString);

                    }

                    //Intent to ConfirmJob
                    Intent intent = new Intent(MainActivity.this, ConfirmJob.class);
                    intent.putExtra("Login", loginStrings);
                    startActivity(intent);
                    finish();

                }



            } catch (Exception e) {
                Log.d("DriverV1", "e onPost ==>" + e.toString());
            }

        } // onPost

    } //Authen Class

} // Main Class
