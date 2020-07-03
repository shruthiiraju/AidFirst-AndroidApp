package com.example.aidfirsttest1;


import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.IOException;
import java.util.Random;

public class P2PRecieverActivity extends ConnectionsActivity {
    /** If true, debug logs are shown on the device. */
    private static final boolean DEBUG = true;

    /**
     * The connection strategy we'll use for Nearby Connections. In this case, we've decided on
     * P2P_STAR, which is a combination of Bluetooth Classic and WiFi Hotspots.
     */
    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    /** Length of state change animations. */
    private static final long ANIMATION_DURATION = 600;

    /**
     * A set of background colors. We'll hash the authentication token we get from connecting to a
     * device to pick a color randomly from this list. Devices with the same background color are
     * talking to each other securely (with 1/COLORS.length chance of collision with another pair of
     * devices).
     */
    @ColorInt
    private static final int[] COLORS =
            new int[] {
                    0xFFF44336 /* red */,
                    0xFF9C27B0 /* deep purple */,
                    0xFF00BCD4 /* teal */,
                    0xFF4CAF50 /* green */,
                    0xFFFFAB00 /* amber */,
                    0xFFFF9800 /* orange */,
                    0xFF795548 /* brown */
            };

    /**
     * This service id lets us find other nearby devices that are interested in the same thing. Our
     * sample does exactly one thing, so we hardcode the ID.
     */
    private static final String SERVICE_ID =
            "com.google.location.nearby.apps.walkietalkie.automatic.SERVICE_ID";

    /**
     * The state of the app. As the app changes states, the UI will update and advertising/discovery
     * will start/stop.
     */
    private State mState = State.UNKNOWN;

    /** A random UID used as this device's endpoint name. */
    private String mName;

    /**
     * The background color of the 'CONNECTED' state. This is randomly chosen from the {@link #COLORS}
     * list, based off the authentication token.
     */
    @ColorInt private int mConnectedColor = COLORS[0];

    /** Displays the previous state during animation transitions. */
    //private TextView mPreviousStateView;

    /** Displays the current state. */
    //private TextView mCurrentStateView;

    /** An animator that controls the animation from previous state to current state. */
    @Nullable
    private Animator mCurrentAnimator;

    /** A running log of debug messages. Only visible when DEBUG=true. */
    private TextView mDebugLogView;
    private TextView volumeTV;

    /** Listens to holding/releasing the volume rocker. */
    private final GestureDetector mGestureDetector =
            new GestureDetector(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP) {
                @Override
                protected void onHold() {
                    logV("onHold");
                    startRecording();
                }

                @Override
                protected void onRelease() {
                    logV("onRelease");
                    stopRecording();
                }
            };

    /** For recording audio as the user speaks. */
    @Nullable private AudioRecorder mRecorder;

    /** For playing audio from other users nearby. */
    @Nullable private AudioPlayer mAudioPlayer;

    /** The phone's original media volume. */
    private int mOriginalVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2_p);
        setTitle("Alert");

        mDebugLogView = (TextView) findViewById(R.id.debug_log);
        volumeTV = findViewById(R.id.volumekeys);

        mName = generateRandomName();

        ((TextView) findViewById(R.id.name)).setText(mName);

        updateTextView(mDebugLogView, State.SEARCHING);


    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mState == State.CONNECTED && mGestureDetector.onKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set the media volume to max.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOriginalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        setState(State.SEARCHING);
    }

    @Override
    protected void onStop() {
        // Restore the original volume.
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

        // Stop all audio-related threads
        if (isRecording()) {
            stopRecording();
        }
        if (isPlaying()) {
            stopPlaying();
        }

        // After our Activity stops, we disconnect from Nearby Connections.
        setState(State.UNKNOWN);

        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getState() == State.CONNECTED) {
            setState(State.SEARCHING);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        // We found an advertiser!
        stopDiscovering();
        updateTextView(mDebugLogView, State.DISCOVERED);
        connectToEndpoint(endpoint);
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        // A connection to another device has been initiated! We'll use the auth token, which is the
        // same on both devices, to pick a color to use when we're connected. This way, users can
        // visually see which device they connected with.
        mConnectedColor = COLORS[connectionInfo.getAuthenticationToken().hashCode() % COLORS.length];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.journaldev.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("Notifications Title");
        builder.setContentText("Your notification content here.");
        builder.setSubText("Tap to view the website.");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
        // We accept the connection immediately.
        acceptConnection(endpoint);
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.CONNECTED);
        volumeTV.setVisibility(View.VISIBLE);
        updateTextView(mDebugLogView, State.CONNECTED);

    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        updateTextView(mDebugLogView, State.DISCONNECTED);
        volumeTV.setVisibility(View.INVISIBLE);
        setState(State.SEARCHING);
    }

    @Override
    protected void onConnectionFailed(Endpoint endpoint) {
        // Let's try someone else.
        if (getState() == State.SEARCHING) {
            startDiscovering();
        }
    }

    /**
     * The state has changed. I wonder what we'll be doing now.
     *
     * @param state The new state.
     */
    private void setState(State state) {
        if (mState == state) {
            logW("State set to " + state + " but already in that state");
            return;
        }

        logD("State set to " + state);
        State oldState = mState;
        mState = state;
        onStateChanged(oldState, state);
    }

    /** @return The current state. */
    private State getState() {
        return mState;
    }

    /**
     * State has changed.
     *
     * @param oldState The previous state we were in. Clean up anything related to this state.
     * @param newState The new state we're now in. Prepare the UI for this state.
     */
    private void onStateChanged(State oldState, State newState) {
        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }

        // Update Nearby Connections to the new state.
        switch (newState) {
            case SEARCHING:
                disconnectFromAllEndpoints();
                startDiscovering();
                break;
            case CONNECTED:
                stopDiscovering();
                break;
            case UNKNOWN:
                stopAllEndpoints();
                break;
            default:
                // no-op
                break;
        }

        // Update the UI.
        switch (oldState) {
            case UNKNOWN:
                // Unknown is our initial state. Whatever state we move to,
                // we're transitioning forwards.
                transitionForward(oldState, newState);
                break;
            case SEARCHING:
                switch (newState) {
                    case UNKNOWN:
                        transitionBackward(oldState, newState);
                        break;
                    case CONNECTED:
                        transitionForward(oldState, newState);
                        break;
                    default:
                        // no-op
                        break;
                }
                break;
            case CONNECTED:
                // Connected is our final state. Whatever new state we move to,
                // we're transitioning backwards.
                transitionBackward(oldState, newState);
                break;
        }
    }

    /** Transitions from the old state to the new state with an animation implying moving forward. */
    @UiThread
    private void transitionForward(State oldState, final State newState) {

    }

    /** Transitions from the old state to the new state with an animation implying moving backward. */
    @UiThread
    private void transitionBackward(State oldState, final State newState) {

    }


    /** Updates the {@link TextView} with the correct color/text for the given {@link State}. */
    @UiThread
    private void updateTextView(TextView textView, State state) {
        switch (state) {
            case SEARCHING:
                textView.setBackgroundResource(R.color.state_searching);
                textView.setText("Searching for victims...");
                break;
            case CONNECTED:
                textView.setBackgroundColor(mConnectedColor);
                textView.setText("Connected to a nearby victim...");
                break;
            case DISCOVERED:
                textView.setBackgroundResource(R.color.state_searching);
                textView.setText("Devices discovered... \nWaiting to connect...");
                break;
            case DISCONNECTED:
                textView.setBackgroundResource(R.color.colorPrimary);
                textView.setText("Device Disconnected");
            default:
                textView.setBackgroundResource(R.color.state_unknown);
                textView.setText(R.string.status_unknown);
                break;
        }
    }

    /** {@see ConnectionsActivity#onReceive(Endpoint, Payload)} */
    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.STREAM) {
            if (mAudioPlayer != null) {
                mAudioPlayer.stop();
                mAudioPlayer = null;
            }

            AudioPlayer player =
                    new AudioPlayer(payload.asStream().asInputStream()) {
                        @WorkerThread
                        @Override
                        protected void onFinish() {
                            runOnUiThread(
                                    new Runnable() {
                                        @UiThread
                                        @Override
                                        public void run() {
                                            mAudioPlayer = null;
                                        }
                                    });
                        }
                    };
            mAudioPlayer = player;
            player.start();
        }
        else if (payload.getType() == Payload.Type.BYTES){
            String s = new String(payload.asBytes());
            Log.e("PAYLOAD", s);
        }
    }

    /** Stops all currently streaming audio tracks. */
    private void stopPlaying() {
        logV("stopPlaying()");
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
            mAudioPlayer = null;
        }
    }

    /** @return True if currently playing. */
    private boolean isPlaying() {
        return mAudioPlayer != null;
    }

    /** Starts recording sound from the microphone and streaming it to all connected devices. */
    private void startRecording() {
        logV("startRecording()");
        try {
            ParcelFileDescriptor[] payloadPipe = ParcelFileDescriptor.createPipe();

            // Send the first half of the payload (the read side) to Nearby Connections.
            send(Payload.fromStream(payloadPipe[0]));

            // Use the second half of the payload (the write side) in AudioRecorder.
            mRecorder = new AudioRecorder(payloadPipe[1]);
            mRecorder.start();
        } catch (IOException e) {
            logE("startRecording() failed", e);
        }
    }

    /** Stops streaming sound from the microphone. */
    private void stopRecording() {
        logV("stopRecording()");
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder = null;
        }
    }

    /** @return True if currently streaming from the microphone. */
    private boolean isRecording() {
        return mRecorder != null && mRecorder.isRecording();
    }

    /** {@see ConnectionsActivity#getRequiredPermissions()} */
    @Override
    protected String[] getRequiredPermissions() {
        return join(
                super.getRequiredPermissions(),
                Manifest.permission.RECORD_AUDIO);
    }

    /** Joins 2 arrays together. */
    private static String[] join(String[] a, String... b) {
        String[] join = new String[a.length + b.length];
        System.arraycopy(a, 0, join, 0, a.length);
        System.arraycopy(b, 0, join, a.length, b.length);
        return join;
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

    @Override
    protected void logV(String msg) {
        super.logV(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_verbose)));
    }

    @Override
    protected void logD(String msg) {
        super.logD(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_debug)));
    }

    @Override
    protected void logW(String msg) {
        super.logW(msg);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    @Override
    protected void logW(String msg, Throwable e) {
        super.logW(msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_warning)));
    }

    @Override
    protected void logE(String msg, Throwable e) {
        super.logE(msg, e);
        appendToLogs(toColor(msg, getResources().getColor(R.color.log_error)));
    }

    private void appendToLogs(CharSequence msg) {
        //mDebugLogView.append("\n");
        //mDebugLogView.append(DateFormat.format("hh:mm", System.currentTimeMillis()) + ": ");
        //mDebugLogView.append(msg);
    }

    private static CharSequence toColor(String msg, int color) {
        SpannableString spannable = new SpannableString(msg);
        spannable.setSpan(new ForegroundColorSpan(color), 0, msg.length(), 0);
        return spannable;
    }

    private static String generateRandomName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name;
    }

    /**
     * Provides an implementation of Animator.AnimatorListener so that we only have to override the
     * method(s) we're interested in.
     */
    private abstract static class AnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationEnd(Animator animator) {}

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }

    /** States that the UI goes through. */
    public enum State {
        UNKNOWN,
        SEARCHING,
        DISCOVERED,
        CONNECTED,
        DISCONNECTED
    }
}
