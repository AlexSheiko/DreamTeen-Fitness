package ui.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest.Builder;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ui.fragments.SetGoalsDialog;
import ui.fragments.SetGoalsDialog.OnChallengeValueChanged;

public class GoalsActivity extends Activity
        implements OnChallengeValueChanged {

    public static final String TAG = GoalsActivity.class.getSimpleName();
    public static final String SESSION_NAME = "Afternoon run";

    private int mDailyStepsTaken;
    private int mWeeklyStepsTaken;
    private int mMonthlyStepsTaken;

    private long mDailyDuration;
    private long mWeeklyDuration;
    private long mMonthlyDuration;

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mClient;

    private SharedPreferences mSharedPrefs;

    @InjectView(R.id.stepsNotSetLabel)
    TextView mStepsNotSetLabel;
    @InjectView(R.id.durationNotSetLabel)
    TextView mDurationNotSetLabel;
    @InjectView(R.id.setStepsButton)
    Button mSetStepsButton;
    @InjectView(R.id.setDurationButton)
    Button mSetDurationButton;
    @InjectView(R.id.progressBarDailySteps)
    ProgressBar mProgressBarDailySteps;
    @InjectView(R.id.progressBarWeeklyDuration)
    ProgressBar mProgressBarWeeklyDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);
        ButterKnife.inject(this);
        mSharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        updateProgressBar();
        buildFitnessClient();
    }

    public void addChallengeGoal(View view) {
        Bundle bundle = new Bundle();

        int id = view.getId();
        if (id == R.id.stepsContainer || id == R.id.setStepsButton) {
            bundle.putString("key", "steps");
        } else if (id == R.id.durationContainer || id == R.id.setDurationButton) {
            bundle.putString("key", "duration");
        }
        DialogFragment newFragment = new SetGoalsDialog();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "dialog_challenge_goal");
    }

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Now you can make calls to the Fitness APIs.
                                readSteps();
                                readDuration();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            GoalsActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(GoalsActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    private void readSteps() {
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        // Get time from the start (00:00) of a day
        cal.add(Calendar.HOUR_OF_DAY, -Calendar.HOUR_OF_DAY);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readCaloriesRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        // Invoke the History API to fetch the data with the query
        Fitness.HistoryApi.readData(mClient, readCaloriesRequest).setResultCallback(
                new ResultCallback<DataReadResult>() {
                    @Override
                    public void onResult(DataReadResult dataReadResult) {
                        for (DataSet dataSet : dataReadResult.getDataSets()) {
                            if (dataSet.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                                dumpDailySteps(dataSet);
                            }
                        }
                    }
                });
    }

    private void dumpDailySteps(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                increaseDailyStepCount(
                        dp.getValue(field).asInt());
            }
        }
        updateProgressBar();
    }

    private void increaseDailyStepCount(int increment) {
        mDailyStepsTaken = mDailyStepsTaken + increment;
    }

    private SessionReadRequest readDuration() {
        // [START build_read_session_request]
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -Calendar.WEEK_OF_YEAR);
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .setSessionName(SESSION_NAME)
                .build();
        // [END build_read_session_request]

        Fitness.SessionsApi.readSession(mClient, readRequest)
                .setResultCallback(new ResultCallback<SessionReadResult>() {
                    @Override
                    public void onResult(SessionReadResult sessionReadResult) {
                        // Get a list of the sessions that match the criteria to check the result.
                        for (Session session : sessionReadResult.getSessions()) {
                            // Process the session
                            dumpWeeklyDuration(session);
                        }
                        updateProgressBar();
                    }
                });
        return readRequest;
    }

    private void dumpWeeklyDuration(Session session) {
        long startTime = session.getStartTime(TimeUnit.SECONDS);
        long endTime = session.getEndTime(TimeUnit.SECONDS);
        long increment = endTime - startTime;
        if (increment > 0) {
            increaseWeeklyDuration(increment);
        }
    }

    private void increaseWeeklyDuration(long increment) {
        mWeeklyDuration = mWeeklyDuration + increment;
    }

    private void updateProgressBar() {
        int dailySteps = mSharedPrefs.getInt("daily_steps", -1);
        boolean isStepsGoalSet = false;
        if (dailySteps != -1) {
            isStepsGoalSet = true;
                mProgressBarDailySteps.setVisibility(View.VISIBLE);
                mProgressBarDailySteps.setMax(dailySteps);
                mProgressBarDailySteps.setProgress(mDailyStepsTaken);
                if (mDailyStepsTaken >= dailySteps) {
                    Rect bounds = mProgressBarDailySteps.getProgressDrawable().getBounds();
                    mProgressBarDailySteps.setProgressDrawable(
                            getResources().getDrawable(R.drawable.pb_calories_reached));
                    mProgressBarDailySteps.getProgressDrawable().setBounds(bounds);
                } else {
                    Rect bounds = mProgressBarDailySteps.getProgressDrawable().getBounds();
                    mProgressBarDailySteps.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_daily_steps));
                    mProgressBarDailySteps.getProgressDrawable().setBounds(bounds);
                }
        } else {
            mStepsNotSetLabel.setVisibility(View.VISIBLE);
            mSetStepsButton.setVisibility(View.VISIBLE);
        }
        if (isStepsGoalSet) {
            mStepsNotSetLabel.setVisibility(View.GONE);
            mSetStepsButton.setVisibility(View.GONE);
        }

        int weeklyDuration = mSharedPrefs.getInt("weekly_duration", -1);
        boolean isDurationGoalSet = false;
        if (weeklyDuration != -1) {
            isDurationGoalSet = true;
            mProgressBarWeeklyDuration.setVisibility(View.VISIBLE);
            mProgressBarWeeklyDuration.setMax(weeklyDuration * 60);
            mProgressBarWeeklyDuration.setProgress((int) mWeeklyDuration);
            if (mWeeklyDuration >= weeklyDuration * 60) {
                Rect bounds = mProgressBarWeeklyDuration.getProgressDrawable().getBounds();
                mProgressBarWeeklyDuration.setProgressDrawable(
                        getResources().getDrawable(R.drawable.pb_calories_reached));
                mProgressBarWeeklyDuration.getProgressDrawable().setBounds(bounds);
                if (mSharedPrefs.getInt("needs_to_notify_run", 0) != 2) {
                    mSharedPrefs.edit().putInt("needs_to_notify_run", 1).apply();
                }
            } else {
                Rect bounds = mProgressBarWeeklyDuration.getProgressDrawable().getBounds();
                mProgressBarWeeklyDuration.setProgressDrawable(
                        getResources().getDrawable(R.drawable.progress_bar_weekly_duration));
                mProgressBarWeeklyDuration.getProgressDrawable().setBounds(bounds);
            }
        } else {
            mDurationNotSetLabel.setVisibility(View.VISIBLE);
            mSetDurationButton.setVisibility(View.VISIBLE);
        }
        if (isDurationGoalSet) {
            mDurationNotSetLabel.setVisibility(View.GONE);
            mSetDurationButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    public void onChallengeValueChanged() {
        updateProgressBar();
    }
}