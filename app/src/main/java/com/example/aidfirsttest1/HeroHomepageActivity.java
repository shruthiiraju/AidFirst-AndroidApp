package com.example.aidfirsttest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HeroHomepageActivity extends ConnectionsActivity {

    FirebaseAuth mAuth;
    private Button signOut;
    private Button refreshButton;
    private Button alertButton;
    private LocationTrack locationTrack;

    private static final String SERVICE_ID =
            "com.google.location.nearby.apps.walkietalkie.automatic.SERVICE_ID";

    /**
     * The state of the app. As the app changes states, the UI will update and advertising/discovery
     * will start/stop.
     */
    private P2PActivity.State mState = P2PActivity.State.UNKNOWN;

    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    /** A random UID used as this device's endpoint name. */
    private String mName;

    private TextView internettv;
    private TextView mobiletv;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userdb = db.collection("users");

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goHome = new Intent(HeroHomepageActivity.this, MainActivity.class);
        finish();
        startActivity(goHome);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()==null){
            Intent goHome = new Intent(HeroHomepageActivity.this, HeroActivity.class);
            HeroHomepageActivity.this.startActivity(goHome);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hero_homepage);
        setTitle("Hero Alerts");
        mAuth = FirebaseAuth.getInstance();




        Log.e("Login ACtivityHompage", ""+ mAuth.getCurrentUser());


        mobiletv = findViewById(R.id.mobileStatustv);
        internettv = findViewById(R.id.internetStatustv);
        signOut = findViewById(R.id.citizenSignOut);
        refreshButton = findViewById(R.id.citizenRefresh);
        alertButton = findViewById(R.id.alertButton);





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

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent gobackIntent = new Intent(HeroHomepageActivity.this, HeroActivity.class);
                HeroHomepageActivity.this.startActivity(gobackIntent);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(alertButton.getText().equals("Hero ON")){
                    alertButton.setText("Hero OFF");
                    alertButton.setTextColor(getResources().getColor(R.color.red));
                } else if (alertButton.getText().equals("Hero OFF")){
                    alertButton.setText("Hero ON");
                    alertButton.setTextColor(getResources().getColor(R.color.green));
                    String loc = "";
                    locationTrack = new LocationTrack(HeroHomepageActivity.this);
                    if (locationTrack.canGetLocation()) {
                        double longitude = locationTrack.getLongitude();
                        double latitude = locationTrack.getLatitude();
                        loc = latitude + " " + longitude;
                        Log.e("LOCATION", "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude));
                        Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                    } else {
                        locationTrack.showSettingsAlert();
                    }

                    try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if(!connectionChecker.isOnline() && !connectionChecker.isMobileAvailable(getApplicationContext())){
                        Intent connectActivityIntent = new Intent(HeroHomepageActivity.this, P2PRecieverActivity.class);
                        startActivity(connectActivityIntent);
                    }

                }
            }
        });
    }

    /**
     * Queries the phone's contacts for their own profile, and returns their name. Used when
     * connecting to another device.
     */
    @Override
    protected String getName() {
        return mName;
    }

    /** {@see ConnectionsActivity#getServiceId()} */
    @Override
    public String getServiceId() {
        return SERVICE_ID;
    }

    /** {@see ConnectionsActivity#getStrategy()} */
    @Override
    public Strategy getStrategy() {
        return STRATEGY;
    }

    public void refresh(){
        this.recreate();
    }

}
