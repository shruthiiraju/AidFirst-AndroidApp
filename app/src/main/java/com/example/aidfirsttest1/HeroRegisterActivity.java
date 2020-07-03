package com.example.aidfirsttest1;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.InstanceIdResult;

public class HeroRegisterActivity extends AppCompatActivity {
    EditText registeremaileditText, registerpasswordeditText, fullnameeditText, phoneEditText;
    Button registerbutton;
    ProgressBar progressBarRegister;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userdb = db.collection("users");
    private FirebaseAuth mAuth;
    String[] blood_types = { "B+", "B-", "AB+", "AB-", "O+" };
    String[] certs = {"Cardiopulmonary Resuscitation (CPR)", "Automated External Defibrillator (AED)", "Basic Life Support (BLS)", "Certified Nursing Assistant (CNA)", "Lifeguard", "First Aid"};
    private String blood_type;
    private String cert;
    private Spinner spin;
    private Spinner certSpin;
    private LocationTrack locationTrack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Register as a Hero");
        setContentView(R.layout.activity_register_hero);
        registeremaileditText = findViewById(R.id.registeremail_editText);
        registerpasswordeditText = findViewById(R.id.registerpassword_editText);
        registerbutton = findViewById(R.id.register_button);
        fullnameeditText = findViewById(R.id.fullname_editText);
        phoneEditText = findViewById(R.id.phone_editText);
        spin = (Spinner) findViewById(R.id.blood_spinner);
        certSpin = findViewById(R.id.certification_spinner);

        mAuth = FirebaseAuth.getInstance();
        progressBarRegister = findViewById(R.id.progressbar_register);
        progressBarRegister.setVisibility(View.GONE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, blood_types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                blood_type = blood_types[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                Toast.makeText(HeroRegisterActivity.this, "Please enter blood type." ,
                        Toast.LENGTH_LONG).show();

            }
        });


        ArrayAdapter<String> cert_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        certSpin.setAdapter(cert_adapter);
        certSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cert = certs[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                Toast.makeText(HeroRegisterActivity.this, "Please enter certification type." ,
                        Toast.LENGTH_LONG).show();

            }
        });


        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarRegister.setVisibility(View.VISIBLE);
                final String TAG = "Register Activity";
                final String name = fullnameeditText.getText().toString();
                final String email = registeremaileditText.getText().toString();
                final String password = registerpasswordeditText.getText().toString();
                final String phone =  phoneEditText.getText().toString();
                final String finalbloodtype = blood_type;
                final String finalcert = cert;
                if (name.isEmpty()) {
                    progressBarRegister.setVisibility(View.GONE);
                    fullnameeditText.setError("Please Enter a Username");
                }
                if (email.isEmpty() || !email.contains("@")) {
                    progressBarRegister.setVisibility(View.GONE);

                    registeremaileditText.setError("Please enter a valid email");
                }
                if (password.isEmpty() || password.length() < 8) {
                    progressBarRegister.setVisibility(View.GONE);

                    registerpasswordeditText.setError("Please Enter a password at least 8 characters long");
                }
                if (phone.isEmpty() || phone.length() < 10) {
                    progressBarRegister.setVisibility(View.GONE);

                    registerpasswordeditText.setError("Please enter a valid Phone Number.");
                }
                if(finalbloodtype.length() < 1 ) {
                    progressBarRegister.setVisibility(View.GONE);
                    Toast.makeText(HeroRegisterActivity.this, "Please enter blood type." ,
                            Toast.LENGTH_LONG).show();

                }
                if(finalbloodtype.length() < 2 ) {
                    progressBarRegister.setVisibility(View.GONE);
                    Toast.makeText(HeroRegisterActivity.this, "Please enter certification type." ,
                            Toast.LENGTH_LONG).show();

                }
                else {

                  Log.e("OK TEST", "YEET");

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(HeroRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.e(TAG, "createUserWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            progressBarRegister.setVisibility(View.GONE);

                                            FirebaseInstanceId.getInstance().getInstanceId()
                                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                            if (!task.isSuccessful()) {
                                                                Log.w(TAG, "getInstanceId failed", task.getException());
                                                                return;
                                                            }

                                                            // Get new Instance ID token
                                                            String token = task.getResult().getToken();
                                                            Toast.makeText(HeroRegisterActivity.this, token, Toast.LENGTH_SHORT).show();

                                                            User userObject = new User(mAuth.getUid(), email, phone, name, true, finalcert, true, finalbloodtype, token);
                                                            userdb.document(mAuth.getUid()).set(userObject)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(HeroRegisterActivity.this, "Registration Successful." ,
                                                                                    Toast.LENGTH_LONG).show();
                                                                            Intent heroIntent = new Intent(HeroRegisterActivity.this, HeroActivity.class);
                                                                            HeroRegisterActivity.this.startActivity(heroIntent);

                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(HeroRegisterActivity.this, "Registration Failed." ,
                                                                                    Toast.LENGTH_LONG).show();

                                                                        }
                                                                    });
                                                        }
                                                    });



                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.e(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(HeroRegisterActivity.this, "Authentication failed. User already exists." ,
                                                    Toast.LENGTH_LONG).show();
                                            progressBarRegister.setVisibility(View.GONE);

                                        }
                                    // ...
                                }
                            });

                }
            }

        });
    }
}






