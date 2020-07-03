package com.example.aidfirsttest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class HeroAcceptRejectActivity extends AppCompatActivity {

    private String name;
    private String phone_no;
    private String blood_group;
    private Float latitude;
    private Float longitude;
    private String reportid;

    private TextView nametv;
    private TextView phonetv;
    private TextView bloodtv;
    private Button callVictim_button;
    private Button directionsButton;

    FirebaseAuth mAuth;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference reportsdb = db.collection("reports");
    CollectionReference userdb = db.collection("users");

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()==null){
            Intent goHome = new Intent(HeroAcceptRejectActivity.this, HeroActivity.class);
            HeroAcceptRejectActivity.this.startActivity(goHome);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hero_accept_reject);
        setTitle("Accepted Request");
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            Intent goHome = new Intent(HeroAcceptRejectActivity.this, HeroActivity.class);
            goHome.putExtra("heroAccept", true);
            goHome.putExtra("name", name);
            goHome.putExtra("phone", phone_no);
            goHome.putExtra("blood", blood_group);
            goHome.putExtra("LAT", latitude.toString());
            goHome.putExtra("LONG", longitude.toString());
            goHome.putExtra("reportID", reportid);
            startActivity(goHome);
        }


        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        phone_no = intent.getStringExtra("phone");
        blood_group = intent.getStringExtra("blood");
        longitude = Float.parseFloat(intent.getStringExtra("LAT"));
        latitude = Float.parseFloat(intent.getStringExtra("LONG"));
        reportid = intent.getStringExtra("reportID");

        phonetv = findViewById(R.id.phone_tv);
        nametv = findViewById(R.id.name_tv);
        bloodtv = findViewById(R.id.blood_tv);
        callVictim_button = findViewById(R.id.callVictim_button);
        directionsButton = findViewById(R.id.getDirections_button);

        phonetv.setText("Phone Number: " + phone_no);
        nametv.setText("Name: " + name);
        bloodtv.setText("Blood Group: "+blood_group);

        Log.e("UID", mAuth.getUid());



        DocumentReference docRef = userdb.document(mAuth.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (!documentSnapshot.exists()) {
                        Log.e("HEROACCLOGFETCH", ": LIST EMPTY");
                        return;
                    } else {
                        User userlog = documentSnapshot.toObject(User.class);
                        Log.e("HEROACCLOGFETCH", "onSuccess: " + userlog.getIs_hero());
                        DocumentReference ref = reportsdb.document(reportid);
                        ref
                                .update("isAssigned", "Acc," + userlog.getName() + "," +userlog.getPhonenumber() + "," + userlog.getCertification() + "," + userlog.getLocation().split(" ")[0] + "," + userlog.getLocation().split(" ")[1])
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
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


        callVictim_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(HeroAcceptRejectActivity.this, new String[]{Manifest.permission.CALL_PHONE},1);
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone_no));
                startActivity(intent);
            }
        });

        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });


    }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        return super.checkPermission(permission, pid, uid);

    }
}
