package ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.util.Timer;
import java.util.TimerTask;

import bellamica.tech.dreamteenfitness.R;
import helpers.Constants;


public class RunActivity extends Activity
        implements OnClickListener, ConnectionCallbacks {

    // Time counter
    private TimerTask timerTask;
    private int elapsedSeconds = 0;

    // Client used to interact with Google APIs
    private GoogleApiClient mClient;

    // Control buttons
    private Button startButton;
    private Button pauseButton;
    private Button finishButton;

    // User's settings
    private SharedPreferences sharedPrefs;
    private TextView durationCounter;
    private TextView distanceCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        disableMapUiControls(
                getFragmentManager().findFragmentById(R.id.map));

        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startButton:
                updateUiOnStart();

                if (mClient == null)
                    createFitnessClient(this).connect();

                startUiStopwatch();

                break;
            case R.id.pauseButton:
                updateUiOnPause();

                stopUiStopwatch(Constants.Timer.JUST_PAUSE);

                break;
            case R.id.finishButton:
                // TODO:
                // 1. Save duration
                // 2. Save time stamp
                startActivity(new Intent(RunActivity.this, SummaryActivity.class));
                break;
        }
    }

    private GoogleApiClient createFitnessClient(Context context) {
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((ConnectionCallbacks) context)
                .addApi(Fitness.API)
                .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
                .addScope(Fitness.SCOPE_BODY_READ_WRITE)
                .addScope(Fitness.SCOPE_LOCATION_READ_WRITE)
                .build();
        return mClient;
    }

    private void updateUiOnStart() {
        getActionBar().setTitle(getString(R.string.running_label));

        startButton.setVisibility(View.GONE);
        (findViewById(R.id.start_button_label)).setVisibility(View.GONE);

        pauseButton = (Button) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);
        pauseButton.setVisibility(View.VISIBLE);
        (findViewById(R.id.pause_button_label)).setVisibility(View.VISIBLE);

        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(this);
        finishButton.setVisibility(View.GONE);
        (findViewById(R.id.finish_button_label)).setVisibility(View.GONE);

        durationCounter = (TextView) findViewById(R.id.duration_counter);
        distanceCounter = (TextView) findViewById(R.id.distance_counter);

        // Set distance units
        if (sharedPrefs.getString("pref_units", "1").equals("1"))
            ((TextView) findViewById(R.id.distanceUnitsLabel)).setText("miles");
    }

    private void updateUiOnPause() {
        if (getActionBar() != null)
            getActionBar().setTitle(getString(R.string.pause_label));

        pauseButton.setVisibility(View.GONE);
        (findViewById(R.id.pause_button_label)).setVisibility(View.GONE);

        startButton.setVisibility(View.VISIBLE);
        (findViewById(R.id.start_button_label)).setVisibility(View.VISIBLE);

        finishButton.setVisibility(View.VISIBLE);
        (findViewById(R.id.finish_button_label)).setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.start_button_label))
                .setText(R.string.resume_run_button_label);
    }

    // Stopwatch to show duration
    public void startUiStopwatch() {
        final Handler handler = new Handler();
        Timer mTimer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        durationCounter.setText(
                                convertSecondsToHMmSs(elapsedSeconds));
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

    public void stopUiStopwatch(int pauseOrStop) {
        timerTask.cancel();
        timerTask = null;

        if (pauseOrStop == Constants.Timer.STOP)
            elapsedSeconds = 0;
    }

    private void disableMapUiControls(Fragment fragment) {
        GoogleMap map = ((MapFragment) fragment).getMap();
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add action bar items
        getMenuInflater().inflate(R.menu.menu_run, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        switch (item.getItemId()) {
            case (R.id.action_music):
                String action = Intent.ACTION_MAIN;
                String category = Intent.CATEGORY_APP_MUSIC;
                Intent intent = Intent.makeMainSelectorActivity(action, category);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // TODO: Start run session
        Log.i("MainActivity", "Connected!");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
