package ui.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;


public class RunActivity extends Activity
        implements ConnectionCallbacks, LocationListener {

    // Time counter
    private LinearLayout countersContainer;
    private TextView durationCounter;
    private TextView distanceCounter;
    private TimerTask timerTask;
    private int elapsedSeconds = 0;
    private boolean isCountdownJustStarted = true;
    private static final int JUST_PAUSE = 0;
    private static final int STOP = 1;

    // Map
    private GoogleMap mMap;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private static final int UPDATE_INTERVAL = 60 * 1000;
    private static final int FASTEST_INTERVAL = 10 * 1000;
    private SharedPreferences sharedPrefs;
    private Editor editor;

    // Control buttons
    private Button startButton, pauseButton, finishButton;
    private TextView startButtonLabel, pauseButtonLabel, finishButtonLabel;

    // Utils
    protected PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        initializeViews();

        createMap();
        createLocationClient();
        buildNotification();

        // Start button listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUiOnStart();

                mLocationClient.connect();

                startTimer();
            }

            private void updateUiOnStart() {
                if (getActionBar() != null)
                    getActionBar().setTitle(getString(R.string.running_label));

                startButton.setVisibility(View.GONE);
                startButtonLabel.setVisibility(View.GONE);

                pauseButton.setVisibility(View.VISIBLE);
                pauseButtonLabel.setVisibility(View.VISIBLE);

                finishButton.setVisibility(View.GONE);
                finishButtonLabel.setVisibility(View.GONE);

                // Keep screen if preference enable
                if (sharedPrefs.getBoolean("pref_keep_screen", true)) {
                    final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    RunActivity.this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
                    RunActivity.this.mWakeLock.acquire();
                }
            }
        });

        // Pause button listener
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUiOnPause();

                if (mLocationClient.isConnected()) {
                    // Disconnecting the client invalidates it.
                    mLocationClient.disconnect();
                }
                stopTimer(JUST_PAUSE);
            }

            private void updateUiOnPause() {
                if (getActionBar() != null)
                    getActionBar().setTitle(getString(R.string.pause_label));

                pauseButton.setVisibility(View.GONE);
                pauseButtonLabel.setVisibility(View.GONE);

                startButton.setVisibility(View.VISIBLE);
                startButtonLabel.setVisibility(View.VISIBLE);

                finishButton.setVisibility(View.VISIBLE);
                finishButtonLabel.setVisibility(View.VISIBLE);

                startButtonLabel.setText(R.string.resume_run_button_label);
            }
        });

        // Finish button listener
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save trip data
                editor.putString("Duration", String.format("%d min, %d sec",
                        TimeUnit.SECONDS.toMinutes(elapsedSeconds),
                        TimeUnit.SECONDS.toSeconds(elapsedSeconds) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedSeconds))
                ));
                editor.putString("DateTime", new SimpleDateFormat("dd MMM, hh:mm").format(new Date()).toLowerCase());
                editor.commit();

                startActivity(new Intent(RunActivity.this, SummaryActivity.class));
            }
        });
    }

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    private void buildNotification() {

        mBuilder = new Builder(this)
                .setSmallIcon(R.drawable.ic_logo_small)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("A session is currently running");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, RunActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(RunActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
    }

    // Countdown timer
    public void startTimer() {
        final Handler handler = new Handler();
        Timer mTimer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        durationCounter.setText(convertSecondsToHMmSs(elapsedSeconds));
                        elapsedSeconds++;
                    }
                });
            }
        };
        mTimer.schedule(timerTask, 0, 1000);
    }

    private String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public void stopTimer(int pauseOrStop) {
        timerTask.cancel();
        timerTask = null;
        if (pauseOrStop == STOP) elapsedSeconds = 0;
    }

    private void initializeViews() {
        startButton = (Button) findViewById(R.id.startButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        finishButton = (Button) findViewById(R.id.finishButton);

        startButtonLabel = (TextView) findViewById(R.id.start_button_label);
        pauseButtonLabel = (TextView) findViewById(R.id.pause_button_label);
        finishButtonLabel = (TextView) findViewById(R.id.finish_button_label);

        durationCounter = (TextView) findViewById(R.id.duration_counter);
        distanceCounter = (TextView) findViewById(R.id.distance_counter);

        startButton = (Button) findViewById(R.id.startButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        finishButton = (Button) findViewById(R.id.finishButton);

        // Also we will get SharedPrefs here
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPrefs.edit();

        if (sharedPrefs.getString("pref_units", "1").equals("1"))
            ((TextView) findViewById(R.id.distanceUnitsLabel)).setText("miles");
    }

    private void createMap() {
        // Create Google Map
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void createLocationClient() {
        // Create location client
        mLocationClient = new LocationClient(this, this, null);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
    }

    // Location variables
    private LatLng previousLatLng;
    private Location previousLocation;
    private Location startLocation;

    @Override
    public void onLocationChanged(Location location) {

        if (isCountdownJustStarted) {
            startLocation = location;
            isCountdownJustStarted = false;
        }

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (previousLocation == null) previousLatLng = currentLatLng;

        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(currentLatLng)
                        .zoom(17)
                        .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions;
        rectOptions = new PolygonOptions()
                .strokeColor(Color.parseColor("#9f5c8f"));

        // If new session
        if (previousLatLng == null) {
            rectOptions.add(new LatLng(currentLatLng.latitude, currentLatLng.longitude));
        } else {
            rectOptions.add(
                    new LatLng(previousLatLng.latitude, previousLatLng.longitude),
                    new LatLng(currentLatLng.latitude, currentLatLng.longitude));
        }
        // Get back the mutable Polygon
        mMap.addPolygon(rectOptions);

        previousLatLng = currentLatLng;
        previousLocation = location;
        Location finalLocation = location;

        double totalDistance = finalLocation.distanceTo(startLocation);

        if (sharedPrefs.getString("pref_units", "1").equals("1"))
            totalDistance = totalDistance * 0.621371;

        String totalDistanceString;

        if (totalDistance / 1000 <= 99) {
            totalDistanceString = String.format("%.2f", totalDistance / 1000);
        } else if (totalDistance / 1000 >= 100 && totalDistance / 1000 <= 999) {
            totalDistanceString = String.format("%.1f", totalDistance / 1000);
        } else {
            totalDistanceString = String.format("%.0f", totalDistance / 1000);
        }

        distanceCounter.setText(totalDistanceString);
        editor.putString("Distance", totalDistanceString).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add items to the action bar
        getMenuInflater().inflate(R.menu.menu_run, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_music) {
            String action = Intent.ACTION_MAIN;
            String category = Intent.CATEGORY_APP_MUSIC;
            Intent intent = Intent.makeMainSelectorActivity(action, category);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isCountdownJustStarted &&
                sharedPrefs.getBoolean("pref_notifications", true)) {
            mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCountdownJustStarted) {
            try {
                mNotificationManager.cancelAll();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onDestroy() {
        if (sharedPrefs.getBoolean("pref_keep_screen", true) &&
                this.mWakeLock != null)
            this.mWakeLock.release();
        super.onDestroy();
    }
}
