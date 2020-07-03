package com.example.aidfirsttest1;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String token = "";
    //String channel_id = "personal_notifications";
    //int notif_id = 001;
    NotificationManagerCompat notificationManager;

    private static final String TAG = "FirebaseServiceClass";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data paylad: " + remoteMessage.getData());
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
                Map<String, String> map;
                map = remoteMessage.getData();

                String title = map.get("title");
                Log.e(TAG, "Message TITLE: " + title);

                String body = map.get("body");
                Log.e(TAG, "Message BODY: " + body);
                String priority = map.get("priority");
                String tag = map.get("tag");


                notificationManager = NotificationManagerCompat.from(this);

                if(title.contains("HERO ON THE WAY")){
                    buildNotification2("fcm_default_channel", map);
                } else {
                    buildNotification("fcm_default_channel", map);
                }


            }

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }

            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.
        }}
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */


    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
    public void buildNotification(String CHANNEL_ID, Map map) {
        Log.e("HELLO", (String) map.get("body"));
        String[] info = ((String) map.get("body")).split("\n");
        HashMap<String, String> information = new HashMap<>();
        for(String temp: info){
            information.put(temp.split(":")[0], temp.split(":")[1]);
        }

        Log.e("INFO", information.toString());



        Intent intent = new Intent(this, HeroHomepageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Intent accept_intent = new Intent(this, HeroAcceptRejectActivity.class);
        accept_intent.putExtra("name", information.get("Name"));
        accept_intent.putExtra("phone", information.get("Phone"));
        accept_intent.putExtra("blood", information.get("Blood group"));
        accept_intent.putExtra("LAT", information.get("Latitude"));
        accept_intent.putExtra("LONG", information.get("Longitude"));
        accept_intent.putExtra("reportID", information.get("id"));

        accept_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent acceptpendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, accept_intent,
                PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle((CharSequence) map.get("title"))
                .setVibrate(new long[]{500, 1000, 1500})
                .addAction(R.drawable.ic_stat_ic_notification, "ACCEPT", acceptpendingIntent)
                .addAction(R.drawable.ic_stat_ic_notification, "REJECT", pendingIntent)
                .setContentText((CharSequence) map.get("body"));



        notification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));


        notificationManager.notify(1, notification.build());
    }

    public void buildNotification2(String CHANNEL_ID, Map map) {
        Log.e("HELLO", (String) map.get("body"));
        String[] info = ((String) map.get("body")).split("\n");
        HashMap<String, String> information = new HashMap<>();
        for(String temp: info){
            information.put(temp.split(":")[0], temp.split(":")[1]);
        }

        Log.e("INFO", information.toString());

        Intent accept_intent = new Intent(this, VictimAcceptedActivity.class);
        accept_intent.putExtra("name", information.get("Name"));
        accept_intent.putExtra("phone", information.get("Phone"));
        accept_intent.putExtra("LAT", information.get("Latitude"));
        accept_intent.putExtra("LONG", information.get("Longitude"));
        accept_intent.putExtra("cert", information.get("Certification"));

        accept_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent acceptpendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, accept_intent,
                PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle((CharSequence) map.get("title"))
                .setVibrate(new long[]{500, 1000, 1500})
                .setContentIntent(acceptpendingIntent)
                .setContentText((CharSequence) map.get("body"));



        notification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));


        notificationManager.notify(1, notification.build());
    }

}
