package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import model.Exercise;

public class WorkoutActivity extends Activity {

    public static final String TAG = WorkoutActivity.class.getSimpleName();

    private CountDownTimer mTimer;
    private int mCurrentPosition = 0;
    private String mCategory;
    private int mDuration = 0;
    private GoogleApiClient mClient = null;
    private static final int REQUEST_OAUTH = 1;
    private boolean authInProgress = false;

    @InjectView(R.id.startButton)
    Button mStartButton;
    @InjectView(R.id.finishButton)
    Button mFinishButton;
    @InjectView(R.id.pauseButton)
    ImageButton mPauseButton;
    @InjectView(R.id.durationCounter)
    TextView mDurationCounter;
    @InjectView(R.id.positionLabel)
    TextView mPositionLabel;
    @InjectView(R.id.title)
    TextView mTitleTextView;
    @InjectView(R.id.description)
    TextView mDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        ButterKnife.inject(this);

        mCategory = getIntent().getStringExtra("category");
        updateExercise();

        mStartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartButton.setVisibility(View.GONE);
                mPauseButton.setVisibility(View.VISIBLE);
                mFinishButton.setVisibility(View.VISIBLE);
                mDurationCounter.setTextColor(
                        getResources().getColor(R.color.time_counter_dark));

                if (getActionBar() != null) {
                    getActionBar().hide();
                }

                mTimer = new CountDownTimer(3 * 60 * 1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        mDurationCounter.setText(
                                convertSecondsToMmSs(millisUntilFinished / 1000));
                        incrementDuration();
                    }

                    public void onFinish() {
                        mDurationCounter.setText("03:00");
                        if (mCurrentPosition < 9) {
                            mCurrentPosition++;
                            updateExercise();
                            mTimer.start();
                        } else {
                            Toast.makeText(WorkoutActivity.this, "Workout saved",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(WorkoutActivity.this,
                                    MainActivity.class));
                        }
                    }
                }.start();
            }
        });

        mPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActionBar() != null) {
                    getActionBar().show();
                }
                mStartButton.setVisibility(View.VISIBLE);
                mPauseButton.setVisibility(View.GONE);
                mFinishButton.setVisibility(View.GONE);
                mDurationCounter.setTextColor(
                        getResources().getColor(R.color.time_counter_light));

                mTimer.cancel();
            }
        });

        mFinishButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Insert calories
                buildFitnessClient();
                mClient.connect();

                Toast.makeText(WorkoutActivity.this, "Workout saved",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(WorkoutActivity.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }

    private String convertSecondsToMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void updateExercise() {
        Exercise exercise = new Exercise(this, mCategory);

        mTitleTextView.setText(exercise.getTitle(mCurrentPosition));
        mDescriptionTextView.setText(exercise.getDescription(mCurrentPosition));
        int exercisePosition = mCurrentPosition + 1;
        mPositionLabel.setText("Exercise " + exercisePosition + "/10");
    }

    public void nextExercise(View view) {
        if (mCurrentPosition < 9) {
            mCurrentPosition++;
            updateExercise();
        } else {
            mCurrentPosition = 0;
            updateExercise();
        }
    }

    public void previousExercise(View view) {
        if (mCurrentPosition != 0) {
            mCurrentPosition--;
            updateExercise();
        } else {
            mCurrentPosition = 9;
            updateExercise();
        }
    }

    private void incrementDuration() {
        mDuration = mDuration + 1;
    }

    /**
     * Build a {@link com.google.android.gms.common.api.GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Start updating map focus and counting steps
                                insertCalories();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            WorkoutActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        authInProgress = true;
                                        result.startResolutionForResult(WorkoutActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    private void insertCalories() {
        // Set a start and end time for our data, using a start time of 1 hour before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.SECOND, -1);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setName(TAG + " - calories expended")
                .setType(DataSource.TYPE_RAW)
                .build();

        // Create a data set
        float caloriesExpanded = 4 * (mDuration / 60);

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_CALORIES).setFloat(caloriesExpanded);
        dataSet.add(dataPoint);

        // Invoke the History API to insert the data
        Fitness.HistoryApi.insertData(mClient, dataSet);
    }
}
