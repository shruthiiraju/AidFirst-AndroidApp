package com.example.aidfirsttest1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

public class VictimAcceptedActivity extends AppCompatActivity {

    private TextView nameTV;
    private TextView phoneTV;
    private TextView certTV;

    private String name;
    private String phoneno;
    private String cert;
    private String latitude;
    private String longitude;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victim_accepted);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        phoneno = intent.getStringExtra("phone");
        cert = intent.getStringExtra("cert");
        latitude = intent.getStringExtra("LAT");
        longitude = intent.getStringExtra("LONG");

        nameTV = findViewById(R.id.name_tv);
        phoneTV = findViewById(R.id.phone_tv);
        certTV = findViewById(R.id.cert_tv);

        nameTV.setText("Name: " +name);
        phoneTV.setText("Phone: "+ phoneno);
        certTV.setText("Certification: "+ cert);

        String imageUri = "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + ","+ longitude +"&zoom=16&size=600x300&maptype=roadmap\n%22%20+%20%22&markers=color:blue%7Clabel:S%7C"+latitude+","+longitude+"&key=AIzaSyBWfqBCSoXIcylYHPv73zhzne-wMltdrnA";
        ImageView ivBasicImage = (ImageView) findViewById(R.id.mapPreview);
        Picasso.with(getApplicationContext()).load(imageUri).into(ivBasicImage);

        ivBasicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });


    }


}
