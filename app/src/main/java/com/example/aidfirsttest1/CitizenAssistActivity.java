package com.example.aidfirsttest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CitizenAssistActivity extends AppCompatActivity {
    TextView register_textView;
    EditText emaileditText, passwordeditText;
    Button loginButton;
    final String TAG = "Login Activity";
    ProgressBar loginProgressbar;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference students = db.collection("users");

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            Intent goHome = new Intent(CitizenAssistActivity.this, CitizenAssistHomepageActivity.class);
            CitizenAssistActivity.this.startActivity(goHome);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goHome = new Intent(CitizenAssistActivity.this, MainActivity.class);
        finish();
        CitizenAssistActivity.this.startActivity(goHome);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizen_assist);
        setTitle("Login as a Citizen");
        register_textView = findViewById(R.id.register_textView);
        emaileditText = findViewById(R.id.email_edittext);
        loginProgressbar = findViewById(R.id.login_progressbar);
        passwordeditText = findViewById(R.id.password_edittext);
        loginButton = findViewById(R.id.loginButton);
        loginProgressbar.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        ImageView anim=findViewById(R.id.anim);
        animate(anim);
        Log.e("Login ACtivity", ""+ mAuth.getCurrentUser());

        register_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CitizenAssistActivity.this, CitizenAssistRegisterActivity.class);
                startActivity(intent);
            }
        });
        loginfirebase();
    }

    private void loginfirebase() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgressbar.setVisibility(View.VISIBLE);
                final String email = emaileditText.getText().toString();
                String password = passwordeditText.getText().toString();
                if (email.isEmpty()) {
                    loginProgressbar.setVisibility(View.GONE);
                    emaileditText.setError("Please Enter Valid Email");
                }
                if (password.isEmpty()) {
                    loginProgressbar.setVisibility(View.GONE);
                    passwordeditText.setError("Please Enter Valid Email");
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(CitizenAssistActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        loginProgressbar.setVisibility(View.INVISIBLE);
                                        Intent heroIntent = new Intent(CitizenAssistActivity.this, CitizenAssistHomepageActivity.class);
                                        heroIntent.putExtra("email", email);
                                        CitizenAssistActivity.this.startActivity(heroIntent);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(CitizenAssistActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        loginProgressbar.setVisibility(View.INVISIBLE);

                                    }

                                    // ...
                                }
                            });
                }
            }
        });

    }


    public void animate(View view) {
        ImageView v = (ImageView) view;
        Drawable drawable = v.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();

        } else if (drawable instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) drawable;
            avd.start();
        }
    }
}
