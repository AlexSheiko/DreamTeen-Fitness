package ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Timer;
import java.util.TimerTask;

import bellamica.tech.dreamteenfitness.R;
import helpers.Constants;
import ui.activities.SummaryActivity;

public class MapPane extends Fragment
        implements OnClickListener {

    // Callback to update session in RunActivity
    WorkoutStateListener mCallback;

    // Time counter
    private TimerTask timerTask;
    private int elapsedSeconds = 0;

    // Control buttons
    private Button mStartButton;
    private Button mPauseButton;
    private Button mFinishButton;

    // User's settings
    private SharedPreferences mSharedPrefs;
    private TextView mDurationCounter;
    private TextView mDistanceCounter;
    private TextView mDistanceUnitsLabel;

    private TextView mStartButtonLabel;
    private TextView mPauseButtonLabel;
    private TextView mFinishButtonLabel;

    private static final int WORKOUT_START = 1;
    private static final int WORKOUT_PAUSE = 2;
    private static final int WORKOUT_FINISH = 3;
    private GoogleMap mMap;
    private float mTotalDistance = 0;


    public interface WorkoutStateListener {
        public void onWorkoutStateChanged(int state);
    }

    // Required empty constructor
    public MapPane() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Session state change listener
        mCallback = (WorkoutStateListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("location-update"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        disableMapUiControls(
                getFragmentManager().findFragmentById(R.id.map));

        mStartButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
    }

    private void initializeViews(View rootView) {
        mStartButton = (Button) rootView.findViewById(R.id.startButton);
        mPauseButton = (Button) rootView.findViewById(R.id.pauseButton);
        mFinishButton = (Button) rootView.findViewById(R.id.finishButton);

        mStartButtonLabel = (TextView) rootView.findViewById(R.id.start_button_label);
        mPauseButtonLabel = (TextView) rootView.findViewById(R.id.pause_button_label);
        mFinishButtonLabel = (TextView) rootView.findViewById(R.id.finish_button_label);

        mDurationCounter = (TextView) rootView.findViewById(R.id.duration_counter);
        mDistanceCounter = (TextView) rootView.findViewById(R.id.distance_counter);
        mDistanceUnitsLabel = (TextView) rootView.findViewById(R.id.distanceUnitsLabel);

        // Set distance units
        if (mSharedPrefs.getString("pref_units", "1").equals("1"))
            mDistanceUnitsLabel.setText("miles");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startButton:
                mCallback.onWorkoutStateChanged(WORKOUT_START);

                updateUiOnStart();

                startUiStopwatch();

                break;
            case R.id.pauseButton:
                mCallback.onWorkoutStateChanged(WORKOUT_PAUSE);

                updateUiOnPause();

                stopUiStopwatch(Constants.Timer.JUST_PAUSE);

                break;
            case R.id.finishButton:
                mCallback.onWorkoutStateChanged(WORKOUT_FINISH);

                mSharedPrefs.edit()
                        .putFloat("Distance", mTotalDistance)
                        .putString("Duration", convertSecondsToHMmSs(elapsedSeconds))
                        .commit();

                startActivity(new Intent(this.getActivity(), SummaryActivity.class));
                break;
        }
    }

    private void updateUiOnStart() {
        if (getActivity() != null && getActivity().getActionBar() != null)
            getActivity().getActionBar().
                    setTitle(getString(R.string.running_label));

        mStartButton.setVisibility(View.GONE);
        mStartButtonLabel.setVisibility(View.GONE);

        mPauseButton.setVisibility(View.VISIBLE);
        mPauseButtonLabel.setVisibility(View.VISIBLE);

        mFinishButton.setVisibility(View.GONE);
        mFinishButtonLabel.setVisibility(View.GONE);
    }

    private void updateUiOnPause() {
        if (getActivity() != null && getActivity().getActionBar() != null)
            getActivity().getActionBar().
                    setTitle(getString(R.string.pause_label));

        mPauseButton.setVisibility(View.GONE);
        mPauseButtonLabel.setVisibility(View.GONE);

        mStartButton.setVisibility(View.VISIBLE);
        mStartButtonLabel.setVisibility(View.VISIBLE);
        mStartButtonLabel.setText(R.string.resume_run_button_label);

        mFinishButton.setVisibility(View.VISIBLE);
        mFinishButtonLabel.setVisibility(View.VISIBLE);
    }

    // Stopwatch to show duration
    public void startUiStopwatch() {
        final Handler handler = new Handler();
        Timer mTimer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        mDurationCounter.setText(
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
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        if (pauseOrStop == Constants.Timer.STOP)
            elapsedSeconds = 0;
    }

    private void disableMapUiControls(Fragment fragment) {
        mMap = ((MapFragment) fragment).getMap();
        if (mMap == null) return;
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private Location mCurrentLocation;
    private Location mPreviousLocation;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Double latitude = intent.getDoubleExtra("latitude", -1);
            Double longitude = intent.getDoubleExtra("longitude", -1);
            moveCameraFocus(latitude, longitude);

            mCurrentLocation = makeLocation(latitude, longitude);
            if (mPreviousLocation != null) {
                float increment = getDistance(mCurrentLocation, mPreviousLocation);
                incrementDistance(increment);
            }
            mPreviousLocation = mCurrentLocation;
        }
    };

    private void moveCameraFocus(Double latitude, Double longitude) {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(
                                latitude, longitude))
                        .zoom(17)
                        .build()));

        mMap.addPolygon(new PolygonOptions()
                .strokeColor(Color.parseColor("#ff4a01"))
                .strokeWidth(5f)
                .add(new LatLng(mPreviousLocation.getLatitude(),
                                mPreviousLocation.getLongitude()),
                        new LatLng(mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude())));
    }

    private Location makeLocation(double latitude, double longitude) {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    private float getDistance(Location previousLocation, Location currentLocation) {
        if (PreferenceManager.getDefaultSharedPreferences(
                this.getActivity()).getString("pref_units", "1").equals("1")) {
            return previousLocation.distanceTo(currentLocation) / 1000 * 0.621371f;
        } else {
            return previousLocation.distanceTo(currentLocation) / 1000;
        }
    }

    private void incrementDistance(float increment) {
        mTotalDistance = mTotalDistance + increment;
        mDistanceCounter.setText(String.format("%.2f", mTotalDistance));
    }

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}

