package com.example.aidfirsttest1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SimpleGestureFilter.SimpleGestureListener {

    private RelativeLayout heroTap;
    private RelativeLayout citizenTap;

    private SimpleGestureFilter detector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        getSupportActionBar().hide();


        detector = new SimpleGestureFilter(MainActivity.this, this);

        heroTap = findViewById(R.id.herotap);
        citizenTap = findViewById(R.id.citizentap);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d("SHURTHI", "Key: " + key + " Value: " + value);
            }
        }

        heroTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent heroIntent = new Intent(MainActivity.this, HeroActivity.class);
                MainActivity.this.startActivity(heroIntent);
            }
        });

        citizenTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent citizenAssistIntent = new Intent(MainActivity.this, CitizenAssistActivity.class);
                MainActivity.this.startActivity(citizenAssistIntent);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        //Detect the swipe gestures and display toast
        String showToastMessage = "";

        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT:
                showToastMessage = "You have Swiped Right.";

                Intent heroIntent = new Intent(MainActivity.this, HeroActivity.class);
                MainActivity.this.startActivity(heroIntent);
                break;
            case SimpleGestureFilter.SWIPE_LEFT:
                showToastMessage = "You have Swiped Left.";
                Intent citizenAssistIntent = new Intent(MainActivity.this, CitizenAssistActivity.class);
                MainActivity.this.startActivity(citizenAssistIntent);

                break;

        }
        Toast.makeText(this, showToastMessage, Toast.LENGTH_SHORT).show();
    }




    //Toast shown when double tapped on screen
    @Override
    public void onDoubleTap() {
        Toast.makeText(this, "You have Double Tapped.", Toast.LENGTH_SHORT)
                .show();
    }

}
