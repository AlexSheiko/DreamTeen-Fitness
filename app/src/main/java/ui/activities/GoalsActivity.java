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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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
import ui.fragments.GoalSetDialog;
import ui.fragments.GoalSetDialog.OnGoalChanged;

public class GoalsActivity extends Activity
        implements OnGoalChanged {

    public static final String SESSION_NAME = "Afternoon run";

    private int mSteps;
    private long mDuration;
    private int mCaloriesExpended;

    private static final int REQUEST_OAUTH = 1;
    private GoogleApiClient mClient;

    @InjectView(R.id.stepsNotSetLabel)
    TextView mStepsNotSetLabel;
    @InjectView(R.id.durationNotSetLabel)
    TextView mDurationNotSetLabel;
    @InjectView(R.id.caloriesNotSetLabel)
    TextView mCaloriesNotSetLabel;
    @InjectView(R.id.stepsButton)
    Button mStepsButton;
    @InjectView(R.id.durationButton)
    Button mDurationButton;
    @InjectView(R.id.caloriesButton)
    Button mCaloriesButton;
    @InjectView(R.id.pbSteps)
    ProgressBar mPbSteps;
    @InjectView(R.id.pbDuration)
    ProgressBar mPbDuration;
    @InjectView(R.id.pbCalories)
    ProgressBar mPbCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);
        ButterKnife.inject(this);

        buildFitnessClient();
    }

    public void addGoal(View view) {
        Bundle bundle = new Bundle();
        int id = view.getId();

        if (id == R.id.stepsContainer || id == R.id.stepsButton) {
            bundle.putString("key", "steps");
        } else if (id == R.id.durationContainer || id == R.id.durationButton) {
            bundle.putString("key", "duration");
        } else if (id == R.id.caloriesContainer || id == R.id.caloriesButton) {
            bundle.putString("key", "calories");
        }
        DialogFragment dialog = new GoalSetDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "dialog_challenge_goal");
    }

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
                .addScope(Fitness.SCOPE_LOCATION_READ)
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Now you can make calls to the Fitness APIs.
                                readDailyStepsAndCalories();
                                readWeeklyDuration();
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
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            GoalsActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                try {
                                    result.startResolutionForResult(GoalsActivity.this,
                                            REQUEST_OAUTH);
                                } catch (IntentSender.SendIntentException ignored) {
                                }
                            }
                        }
                )
                .build();
    }

    private void readDailyStepsAndCalories() {
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
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        // Invoke the History API to fetch the data with the query
        Fitness.HistoryApi.readData(mClient, readCaloriesRequest).setResultCallback(
                new ResultCallback<DataReadResult>() {
                    @Override
                    public void onResult(DataReadResult dataReadResult) {
                        for (DataSet dataSet : dataReadResult.getDataSets()) {
                            if (dataSet.getDataType()
                                    .equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                                dumpSteps(dataSet);
                            } else if (dataSet.getDataType()
                                    .equals(DataType.TYPE_CALORIES_EXPENDED)) {
                                dumpCalories(dataSet);
                            }
                        }
                        updateCounters();
                    }
                });
    }

    private void dumpSteps(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                int increment = dp.getValue(field).asInt();
                increaseSteps(increment);
            }
        }
    }

    private void dumpCalories(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                int increment = Math.round(dp.getValue(field).asFloat());
                increaseCalories(increment);
            }
        }
    }

    private void increaseSteps(int increment) {
        mSteps += increment;
    }

    private void increaseCalories(int increment) {
        mCaloriesExpended += increment;
    }

    private SessionReadRequest readWeeklyDuration() {
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
                            dumpDuration(session);
                        }
                        updateCounters();
                    }
                });
        return readRequest;
    }

    private void dumpDuration(Session session) {
        long startTime = session.getStartTime(TimeUnit.SECONDS);
        long endTime = session.getEndTime(TimeUnit.SECONDS);
        long increment = endTime - startTime;
        if (increment > 0) {
            increaseDuration(increment);
        }
    }

    private void increaseDuration(long increment) {
        mDuration += increment;
    }

    private void updateCounters() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        int steps = sharedPrefs.getInt("daily_steps", -1);
        if (steps != -1) {
            mPbSteps.setVisibility(View.VISIBLE);
            mPbSteps.setMax(steps);
            mPbSteps.setProgress(mSteps);
            if (mSteps >= steps) {
                setPbColor(mPbSteps, R.drawable.pb_reached);
            } else {
                setPbColor(mPbSteps, R.drawable.pb_steps);
            }
            mStepsNotSetLabel.setVisibility(View.GONE);
            mStepsButton.setVisibility(View.GONE);
        } else {
            mStepsNotSetLabel.setVisibility(View.VISIBLE);
            mStepsButton.setVisibility(View.VISIBLE);
            mPbSteps.setVisibility(View.GONE);
        }

        int duration = sharedPrefs.getInt("weekly_duration", -1);
        sharedPrefs.edit().putLong("mDuration", mDuration).apply();
        if (duration != -1) {
            mPbDuration.setVisibility(View.VISIBLE);
            mPbDuration.setMax(duration * 60);
            mPbDuration.setProgress((int) mDuration);
            if (mDuration >= duration * 60) {
                setPbColor(mPbDuration, R.drawable.pb_reached);
            } else {
                setPbColor(mPbDuration, R.drawable.pb_duration);
            }
            mDurationNotSetLabel.setVisibility(View.GONE);
            mDurationButton.setVisibility(View.GONE);
        } else {
            mDurationNotSetLabel.setVisibility(View.VISIBLE);
            mDurationButton.setVisibility(View.VISIBLE);
            mPbDuration.setVisibility(View.GONE);
        }

        int caloriesGoal = sharedPrefs.getInt("calories_norm", -1);
        if (caloriesGoal != -1) {
            mPbCalories.setVisibility(View.VISIBLE);
            mPbCalories.setMax(caloriesGoal);
            mPbCalories.setProgress(mCaloriesExpended);

            Log.i("TAG", "Max: " + caloriesGoal);
            Log.i("TAG", "Progress: " + mCaloriesExpended);

            if (mCaloriesExpended >= caloriesGoal) {
                setPbColor(mPbCalories, R.drawable.pb_reached);
            } else {
                setPbColor(mPbCalories, R.drawable.pb_calories);
            }
            mCaloriesNotSetLabel.setVisibility(View.GONE);
            mCaloriesButton.setVisibility(View.GONE);
        } else {
            mCaloriesNotSetLabel.setVisibility(View.VISIBLE);
            mCaloriesButton.setVisibility(View.VISIBLE);
            mPbCalories.setVisibility(View.GONE);
        }
    }

    private void setPbColor(ProgressBar pb, int drawableId) {
        Rect bounds = pb.getProgressDrawable().getBounds();
        pb.setProgressDrawable(
                getResources().getDrawable(drawableId));
        pb.getProgressDrawable().setBounds(bounds);
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
        if (requestCode == REQUEST_OAUTH
                && resultCode == RESULT_OK) {
            // Make sure the app is not already connected or attempting to connect
            if (!mClient.isConnecting() && !mClient.isConnected()) {
                mClient.connect();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onGoalChanged() {
        updateCounters();
    }
}