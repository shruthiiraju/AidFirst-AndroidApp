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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class CitizenAssistRegisterActivity extends AppCompatActivity {
    EditText registeremaileditText, registerpasswordeditText, fullnameeditText, phonenumbereditText;
    Button registerbutton;
    ProgressBar progressBarRegister;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userdb = db.collection("users");
    private FirebaseAuth mAuth;
    String[] blood_types = { "B+", "B-", "AB+", "AB-", "O+" };
    private String blood_type;
    private Spinner spin;
    private String token;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizen_assist_register);
        setTitle("Register as a Citizen");
        registeremaileditText = findViewById(R.id.registeremail_editText);
        registerpasswordeditText = findViewById(R.id.registerpassword_editText);
        registerbutton = findViewById(R.id.register_button);
        fullnameeditText = findViewById(R.id.fullname_editText);
        phonenumbereditText = findViewById(R.id.phone_editText);
        spin = (Spinner) findViewById(R.id.blood_spinner);
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

                Toast.makeText(CitizenAssistRegisterActivity.this, "Please enter blood type." ,
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
                final String phone = phonenumbereditText.getText().toString();
                final String finalbloodtype = blood_type;
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
                if (phone.isEmpty() || phone.length() < 8) {
                    progressBarRegister.setVisibility(View.GONE);

                    registerpasswordeditText.setError("Please enter a valid Phone number.");
                }
                if(finalbloodtype.length() < 1 ) {
                    progressBarRegister.setVisibility(View.GONE);
                    Toast.makeText(CitizenAssistRegisterActivity.this, "Please enter blood type." ,
                            Toast.LENGTH_LONG).show();

                }
                else {

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(CitizenAssistRegisterActivity.this, new OnCompleteListener<AuthResult>() {
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


                                                        token = task.getResult().getToken();

                                                        Toast.makeText(CitizenAssistRegisterActivity.this, token, Toast.LENGTH_SHORT).show();
                                                        User userObject = new User(mAuth.getUid(), email, phone, name, false, null, false, finalbloodtype, token);
                                                        userdb.document(mAuth.getUid()).set(userObject)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(CitizenAssistRegisterActivity.this, "Registration Successful." ,
                                                                                Toast.LENGTH_LONG).show();
                                                                        Intent caIntent = new Intent(CitizenAssistRegisterActivity.this, CitizenAssistActivity.class);
                                                                        CitizenAssistRegisterActivity.this.startActivity(caIntent);

                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(CitizenAssistRegisterActivity.this, "Registration Failed." ,
                                                                                Toast.LENGTH_LONG).show();

                                                                    }
                                                                });
                                                    }
                                                });



                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.e(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(CitizenAssistRegisterActivity.this, "Authentication failed. User already exists." ,
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






