package com.example.aidfirsttest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;


public class CitizenAssistHomepageActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    private Button alertButton;
    private ProgressBar mProgressBar;
    private TextView lookingHero;

    private TextView internettv;
    private TextView mobiletv;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private String email;
    private LocationTrack locationTrack;
    private Spinner categSpin;
    private String[] category = {"Fire", "Natural Disaster", "Theft", "Medical Emergency", "Industrial Hazard"};

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userdb = db.collection("users");
    CollectionReference reports = db.collection("reports");

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goHome = new Intent(CitizenAssistHomepageActivity.this, MainActivity.class);
        finish();
        startActivity(goHome);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()==null){
            Intent goHome = new Intent(CitizenAssistHomepageActivity.this, CitizenAssistActivity.class);
            CitizenAssistHomepageActivity.this.startActivity(goHome);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh) {
            refresh();
        }

        if (id == R.id.signOut) {
            mAuth.signOut();
            Intent gobackIntent = new Intent(CitizenAssistHomepageActivity.this, CitizenAssistActivity.class);
            CitizenAssistHomepageActivity.this.startActivity(gobackIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizen_assist_homepage);
        setTitle("Citizen Assist");

        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Log.e("permission", "Permission already granted.");
            } else {
                requestPermission();
            }
        }

        Log.e("EMAAIL", email + "");
        Log.e("Login ACtivityHompage", ""+ mAuth.getUid());


        mobiletv = findViewById(R.id.mobileStatustv);
        categSpin = (Spinner) findViewById(R.id.alert_spinner);
        internettv = findViewById(R.id.internetStatustv);
        alertButton = findViewById(R.id.alertButton);
        mProgressBar = findViewById(R.id.progressbar_register);
        lookingHero = findViewById(R.id.lookingHero);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, category);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categSpin.setAdapter(adapter);
        categSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {


            }
        });


        final ConnectionChecker connectionChecker = new ConnectionChecker();
        if(!connectionChecker.isMobileAvailable(getApplicationContext())){
            mobiletv.setText("NO CELLULAR");
            mobiletv.setTextColor(getResources().getColor(R.color.red));
        }

        if(!connectionChecker.isOnline()){
            internettv.setText("NO INTERNET");
            internettv.setTextColor(getResources().getColor(R.color.red));
        }

        Log.e("CITIZENMOBIL", Boolean.toString(connectionChecker.isMobileAvailable(getApplicationContext())));
        Log.e("CITIZENINTER", Boolean.toString(connectionChecker.isOnline()));



        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!connectionChecker.isOnline() && !connectionChecker.isMobileAvailable(getApplicationContext())){
                    Intent connectActivityIntent = new Intent(CitizenAssistHomepageActivity.this, P2PActivity.class);
                    startActivity(connectActivityIntent);

                }
                if(!connectionChecker.isOnline() && connectionChecker.isMobileAvailable(getApplicationContext())){
                    Toast.makeText(CitizenAssistHomepageActivity.this, "Twilio Mode" ,
                            Toast.LENGTH_LONG).show();

                    String loc = "";
                    locationTrack = new LocationTrack(CitizenAssistHomepageActivity.this);
                    if (locationTrack.canGetLocation()) {
                        double longitude = locationTrack.getLongitude();
                        double latitude = locationTrack.getLatitude();
                        loc = latitude + " " + longitude;
                        Log.e("LOCATION", "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude));
                        Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                    } else {
                        locationTrack.showSettingsAlert();
                    }
                    try{
                        SmsManager smgr = SmsManager.getDefault();
                        smgr.sendTextMessage("+17732957512",null, loc,null,null);
                        Toast.makeText(CitizenAssistHomepageActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e){
                        Toast.makeText(CitizenAssistHomepageActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
                    }

                }
                if(connectionChecker.isOnline() && connectionChecker.isMobileAvailable(getApplicationContext())){
                    mProgressBar.setVisibility(View.VISIBLE);
                    lookingHero.setVisibility(View.VISIBLE);

                    DocumentReference docRef = userdb.document(mAuth.getUid());
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.exists()) {
                                Log.e("HEROLOGFETCH", ": LIST EMPTY");
                                return;
                            } else {
                                User userlog = documentSnapshot.toObject(User.class);
                                String loc = "";
                                locationTrack = new LocationTrack(CitizenAssistHomepageActivity.this);
                                if (locationTrack.canGetLocation()) {
                                    double longitude = locationTrack.getLongitude();
                                    double latitude = locationTrack.getLatitude();
                                    loc = latitude + " " + longitude;
                                    Log.e("LOCATION", "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude));
                                    Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                                } else {
                                    locationTrack.showSettingsAlert();
                                }
                                Log.e("TAG", loc.split(" ")[0]);

                                DocumentReference ref = userdb.document(mAuth.getUid());
                                ref
                                        .update("location", loc)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.e("UPDATING LOC", "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("UPDATING LOC", "Error updating document", e);
                                            }
                                        });


                                Report report = new Report("", userlog.getName() + "," + userlog.getPhonenumber() + "," + userlog.getBlood_type() + "," + loc.split(" ")[0] + "," + loc.split(" ")[1], "Help Me Now!", userlog.getPhonenumber(), true, loc.split(" ")[0], loc.split(" ")[1] );
                                reports.add(report)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(CitizenAssistHomepageActivity.this, "Report Success ", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CitizenAssistHomepageActivity.this, "Report Unsuccessful", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }
            }
        });

    }

    private boolean checkPermission() {
        int result1  = ContextCompat.checkSelfPermission(CitizenAssistHomepageActivity.this, Manifest.permission.SEND_SMS);
        int result2  = ContextCompat.checkSelfPermission(CitizenAssistHomepageActivity.this, Manifest.permission.RECEIVE_SMS);
        if (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(CitizenAssistHomepageActivity.this,
                            "Permission accepted", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(CitizenAssistHomepageActivity.this,
                            "Permission denied", Toast.LENGTH_LONG).show();
                    Button sendSMS = (Button) findViewById(R.id.alertButton);
                    sendSMS.setEnabled(false);

                }
                break;
        }
    }

    public void refresh(){
        this.recreate();
    }


}
