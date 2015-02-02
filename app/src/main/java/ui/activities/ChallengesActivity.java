package ui.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ui.fragments.ChallengeGoalDialog;
import ui.fragments.ChallengeGoalDialog.OnChallengeValueChanged;

public class ChallengesActivity extends Activity
        implements OnChallengeValueChanged {

    public static final String TAG = ChallengesActivity.class.getSimpleName();

    private int mDailyStepsTaken;
    private int mWeeklyStepsTaken;
    private int mMonthlyStepsTaken;

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
    @InjectView(R.id.progressBarWeeklySteps)
    ProgressBar mProgressBarWeeklySteps;
    @InjectView(R.id.progressBarMonthlySteps)
    ProgressBar mProgressBarMonthlySteps;
    @InjectView(R.id.progressBarDailyDuration)
    ProgressBar mProgressBarDailyDuration;
    @InjectView(R.id.progressBarWeeklyDuration)
    ProgressBar mProgressBarWeeklyDuration;
    @InjectView(R.id.progressBarMonthlyDuration)
    ProgressBar mProgressBarMonthlyDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
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
        switch (view.getId()) {
            case R.id.stepsContainer:
                bundle.putString("key", "steps");
                break;
            case R.id.durationContainer:
                bundle.putString("key", "duration");
                break;
        }
        DialogFragment newFragment = new ChallengeGoalDialog();
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
                                readStepCount();
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
                                            ChallengesActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(ChallengesActivity.this,
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

    private void readStepCount() {
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

        cal.setTime(now);
        endTime = cal.getTimeInMillis();
        // Get time from the start (00:00) of a day
        cal.add(Calendar.WEEK_OF_YEAR, -Calendar.WEEK_OF_YEAR);
        startTime = cal.getTimeInMillis();

        readCaloriesRequest = new DataReadRequest.Builder()
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
                                dumpWeeklySteps(dataSet);
                            }
                        }
                    }
                });

        cal.setTime(now);
        endTime = cal.getTimeInMillis();
        // Get time from the start (00:00) of a day
        cal.add(Calendar.MONTH, -Calendar.MONTH);
        startTime = cal.getTimeInMillis();

        readCaloriesRequest = new DataReadRequest.Builder()
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
                                dumpMonthlySteps(dataSet);
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

    private void dumpWeeklySteps(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                increaseWeeklyStepCount(
                        dp.getValue(field).asInt());
            }
        }
        updateProgressBar();
    }

    private void dumpMonthlySteps(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                increaseMonthlyStepCount(
                        dp.getValue(field).asInt());
            }
        }
        updateProgressBar();
    }

    private void updateProgressBar() {
        int dailySteps = mSharedPrefs.getInt("daily_steps", -1);
        int weeklySteps = mSharedPrefs.getInt("weekly_steps", -1);
        int monthlySteps = mSharedPrefs.getInt("monthly_steps", -1);
        int dailyDuration = mSharedPrefs.getInt("daily_duration", -1);
        int weeklyDuration = mSharedPrefs.getInt("weekly_duration", -1);
        int monthlyDuration = mSharedPrefs.getInt("monthly_duration", -1);

        boolean isStepsGoalSet = false;
        if (dailySteps != -1 || weeklySteps != -1 || monthlySteps != -1) {
            isStepsGoalSet = true;
            if (dailySteps != -1) {
                mProgressBarDailySteps.setVisibility(View.VISIBLE);
                mProgressBarDailySteps.setMax(dailySteps);
                mProgressBarDailySteps.setProgress(mDailyStepsTaken);
            }
            if (weeklySteps != -1) {
                mProgressBarWeeklySteps.setVisibility(View.VISIBLE);
                mProgressBarWeeklySteps.setMax(weeklySteps);
                mProgressBarWeeklySteps.setProgress(mWeeklyStepsTaken);
            }
            if (monthlySteps != -1) {
                mProgressBarMonthlySteps.setVisibility(View.VISIBLE);
                mProgressBarMonthlySteps.setMax(monthlySteps);
                mProgressBarMonthlySteps.setProgress(mMonthlyStepsTaken);
            }
        }
        boolean isDurationGoalSet = false;
        if (dailyDuration != -1 || weeklyDuration != -1 || monthlyDuration != -1) {
            isDurationGoalSet = true;
            if (dailyDuration != -1) {
                mProgressBarDailyDuration.setVisibility(View.VISIBLE);
            }
            if (weeklyDuration != -1) {
                mProgressBarWeeklyDuration.setVisibility(View.VISIBLE);
            }
            if (monthlyDuration != -1) {
                mProgressBarMonthlyDuration.setVisibility(View.VISIBLE);
            }
        }

        if (isStepsGoalSet) {
            mStepsNotSetLabel.setVisibility(View.GONE);
            mSetStepsButton.setVisibility(View.GONE);
        }
        if (isDurationGoalSet) {
            mDurationNotSetLabel.setVisibility(View.GONE);
            mSetDurationButton.setVisibility(View.GONE);
        }
    }

    private void increaseDailyStepCount(int increment) {
        mDailyStepsTaken = mDailyStepsTaken + increment;
    }

    private void increaseWeeklyStepCount(int increment) {
        mWeeklyStepsTaken = mWeeklyStepsTaken + increment;
    }

    private void increaseMonthlyStepCount(int increment) {
        mMonthlyStepsTaken = mMonthlyStepsTaken + increment;
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
