package ui.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import ui.fragments.MapPane;
import ui.fragments.MapPane.WorkoutStateListener;


public class RunActivity extends Activity
        implements WorkoutStateListener {

    public static final String TAG = "DreamTeen Fitness";
    private static final int REQUEST_OAUTH = 1;

    private static final int WORKOUT_START = 1;
    private static final int WORKOUT_PAUSE = 2;
    private static final int WORKOUT_FINISH = 3;

    private boolean is25notified = false;
    private boolean is50notified = false;
    private boolean is75notified = false;
    private boolean is100notified = false;

    // Track whether an authorization activity is stacking over the current activity
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;
    private OnDataPointListener mLocationListener;
    private OnDataPointListener mStepsListener;

    private SharedPreferences mSharedPrefs;
    private int mStepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();

        MapPane mapFragment = new MapPane();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mapFragment);
        ft.commit();
    }

    @Override
    public void onWorkoutStateChanged(int state) {
        switch (state) {
            case WORKOUT_START:
                // Connect to the Fitness API
                mClient.connect();
                break;

            case WORKOUT_PAUSE:
                if (mClient.isConnected()) {
                    unregisterLocationListener();
                }
                break;

            case WORKOUT_FINISH:
                if (mClient.isConnected()) {
                    insertCaloriesAndSteps();
                    mClient.disconnect();
                }
                startActivity(new Intent(this, SummaryActivity.class)
                .putExtra("step_count", mStepCount));

//                onGameOverCloseButtonTouched();
                break;
        }
    }

//    private void onGameOverCloseButtonTouched() {
//        // check if the user wants to post their score to facebook
//        // which requires the publish_actions permissions
//
//        Session session = Session.getActiveSession();
//        if (session == null || !session.isOpened()) {
//            return;
//        }
//        List<String> permissions = session.getPermissions();
//
//        // check to see that the user granted the publish_actions permission.
//        if (!permissions.contains("publish_actions")) {
//            // the user didn't grant this permission, so we need to prompt them.
//            askForPublishActionsForScores();
//            return;
//        } else {
//            // Save score and hide the gameOverContainer
//            postScore();
//            gameOverContainer.setVisibility(View.INVISIBLE);
//
//
//        }
//    }

    // Show a dialog prompting the user with an explanation of why we're ablish_actions permission in order to save their scores.
//    private void askForPublishActionsForScores() {
//        new AlertDialog.Builder(this)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User hit OK. Request Facebook friends permission.
//                        requestPublishPermissions();
//                    }
//                })
//                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User hit cancel.
//                        // Hide the gameOverContainer
//                        gameOverContainer.setVisibility(View.INVISIBLE);
//                    }
//                })
//                .setTitle("Save Score")
//                .setMessage("Do you want to save your score on Facebook")
//                .show();
//    }

    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
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
                                findLocationDataSources();
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
                                            RunActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        authInProgress = true;
                                        result.startResolutionForResult(RunActivity.this,
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

    private void insertCaloriesAndSteps() {
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
        float totalDistance = mSharedPrefs.getFloat("Distance", 0);
        float caloriesExpanded =
                132 * // weight in pounds;
                        0.63f * // run factor
                        totalDistance; // distance in miles

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_CALORIES).setFloat(caloriesExpanded);
        dataSet.add(dataPoint);

        // Invoke the History API to insert the data
        Fitness.HistoryApi.insertData(mClient, dataSet);

        // Create a data source
        dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName(TAG + " - step count")
                .setType(DataSource.TYPE_RAW)
                .build();

        // Create a data set
        mStepCount = (int) (totalDistance * 2000);

        dataSet = DataSet.create(dataSource);
        dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(mStepCount);
        dataSet.add(dataPoint);

        // Invoke the History API to insert the data
        Fitness.HistoryApi.insertData(mClient, dataSet);
    }

    /**
     * Find available data sources and attempt to register on a specific {@link DataType}.
     */
    private void findLocationDataSources() {
        // [START find_data_sources]
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE)
                .setDataSourceTypes(DataSource.TYPE_RAW, DataSource.TYPE_RAW)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            //Let's register a listener to receive Activity data!
                            if (dataSource.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE)
                                    && mLocationListener == null) {
                                registerLocationListener(dataSource,
                                        DataType.TYPE_LOCATION_SAMPLE);
                            }
                        }
                    }
                });
        // [END find_data_sources]
    }

    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
    private void registerLocationListener(DataSource dataSource, final DataType dataType) {
        // [START register_data_listener]
        mLocationListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                Double mLatitude = 0.0;
                Double mLongitude = 0.0;
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    // Set latitude or longitude
                    if (field.getName().equals("latitude")) {
                        mLatitude = (double) val.asFloat();
                    } else if (field.getName().equals("longitude")) {
                        mLongitude = (double) val.asFloat();
                    }
                }
                // Callback to update map's focus
                sendLocation(mLatitude, mLongitude);
            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(10, TimeUnit.SECONDS)
                        .build(),
                mLocationListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            Log.i(TAG, "Listener not registered.");
                        }
                    }
                });


        //            final int stepsTarget = mSharedPrefs.getInt("steps_target",
        //                    Integer.parseInt(getResources().getString(R.string.steps_target_default_value)));
        //            mStepsListener = new OnDataPointListener() {
        //
        //                private int stepsTaken = 0;
        //
        //                @Override
        //                public void onDataPoint(DataPoint dataPoint) {
        //                    for (Field field : dataPoint.getDataType().getFields()) {
        //                        Value value = dataPoint.getValue(field);
        //                        if (field.equals(Field.FIELD_STEPS)) {
        //                            stepsTaken = stepsTaken + value.asInt();
        //                            if (stepsTaken > stepsTarget * 0.25 && !is25notified) {
        //                                showNotification(25);
        //                                is25notified = true;
        //                            } else if (stepsTaken > stepsTarget * 0.5 && !is50notified) {
        //                                showNotification(50);
        //                                is50notified = true;
        //                            } else if (stepsTaken > stepsTarget * 0.75 && !is75notified) {
        //                                showNotification(75);
        //                                is75notified = true;
        //                            } else if (stepsTaken >= stepsTarget && !is100notified) {
        //                                showNotification(100);
        //                                is100notified = true;
        //                            }
        //                        }
        //                    }
        //                }
        //
        //                private void showNotification(int progress) {
        //                    String title;
        //                    if (progress == 100) {
        //                        title = "Daily step goal reached!";
        //                    } else {
        //                        title = "Step goal is " + progress + "% reached";
        //                    }
        //
        //                    NotificationCompat.Builder mBuilder =
        //                            new NotificationCompat.Builder(RunActivity.this)
        //                                    .setSmallIcon(R.drawable.ic_launcher)
        //                                    .setContentTitle(title);
        //
        //                    // Sets an ID for the notification
        //                    int mNotificationId = 1;
        //                    // Gets an instance of the NotificationManager service
        //                    NotificationManager mNotifyMgr =
        //                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //                    // Builds the notification and issues it.
        //                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
        //                }
        //            };
        // [END register_data_listener]
    }

    private void sendLocation(Double latitude, Double longitude) {
        Intent intent = new Intent("location-update");
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Unregister the listener with the Sensors API.
     */
    private void unregisterLocationListener() {
        if (mLocationListener == null || mStepsListener == null) return;

        Fitness.SensorsApi.remove(
                mClient,
                mLocationListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            Log.i(TAG, "Failed to remove location listener.");
                        }
                    }
                });
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
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
}