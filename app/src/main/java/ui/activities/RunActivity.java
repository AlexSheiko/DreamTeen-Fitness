package ui.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
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
                    unregisterDataSources();
                    Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                    Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_CALORIES_EXPENDED);
                    mClient.disconnect();
                }
                break;

            case WORKOUT_FINISH:
                // TODO: Delete this after implementing read query in MainActivity
                insertSteps();
                startActivity(new Intent(this, SummaryActivity.class));
                break;
        }
    }

    private void insertSteps() {
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
                .setDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setName(TAG + " - step count")
                .setType(DataSource.TYPE_RAW)
                .build();

        // Create a data set
        int stepCount = 111;
        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCount);
        dataSet.add(dataPoint);

        // Invoke the History API to insert the data
        Fitness.HistoryApi.insertData(mClient, dataSet).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (mClient.isConnected()) {
                    mClient.disconnect();
                }
            }
        });
    }

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
                                findFitnessDataSources();
                                // Subscribe to steps and calories
                                Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                                Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_CALORIES_EXPENDED);
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

    /**
     * Find available data sources and attempt to register on a specific {@link DataType}.
     */
    private void findFitnessDataSources() {
        // [START find_data_sources]
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_RAW, DataSource.TYPE_DERIVED)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            //Let's register a listener to receive Activity data!
                            if (dataSource.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE)
                                    && mLocationListener == null) {
                                registerDataListeners(dataSource,
                                        DataType.TYPE_LOCATION_SAMPLE);
                            } else if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                    && mStepsListener == null) {
                                registerDataListeners(dataSource,
                                        DataType.TYPE_STEP_COUNT_CUMULATIVE);
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
    private void registerDataListeners(DataSource dataSource, final DataType dataType) {
        // [START register_data_listener]
        if (dataType.equals(DataType.TYPE_LOCATION_SAMPLE)) {
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
        } else if (dataType.equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)) {
            final int stepsTarget = mSharedPrefs.getInt("steps_target",
                    Integer.parseInt(getResources().getString(R.string.steps_target_default_value)));
            mStepsListener = new OnDataPointListener() {

                private int stepsTaken = 0;

                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    for (Field field : dataPoint.getDataType().getFields()) {
                        Value value = dataPoint.getValue(field);
                        if (field.equals(Field.FIELD_STEPS)) {
                            stepsTaken = stepsTaken + value.asInt();
                            if (stepsTaken > stepsTarget * 0.25 && !is25notified) {
                                showNotification(25);
                                is25notified = true;
                            } else if (stepsTaken > stepsTarget * 0.5 && !is50notified) {
                                showNotification(50);
                                is50notified = true;
                            } else if (stepsTaken > stepsTarget * 0.75 && !is75notified) {
                                showNotification(75);
                                is75notified = true;
                            } else if (stepsTaken >= stepsTarget && !is100notified) {
                                showNotification(100);
                                is100notified = true;
                            }
                        }
                    }
                }

                private void showNotification(int progress) {
                    String title;
                    if (progress == 100) {
                        title = "Daily step goal reached!";
                    } else {
                        title = "Step goal is " + progress + "% reached";
                    }

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(RunActivity.this)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle(title);

                    // Sets an ID for the notification
                    int mNotificationId = 1;
                    // Gets an instance of the NotificationManager service
                    NotificationManager mNotifyMgr =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    // Builds the notification and issues it.
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
            };

            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(1, TimeUnit.SECONDS)
                            .build(),
                    mStepsListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (!status.isSuccess()) {
                                Log.i(TAG, "Listener not registered.");
                            }
                        }
                    });
        }


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
    private void unregisterDataSources() {
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

        Fitness.SensorsApi.remove(
                mClient,
                mStepsListener)
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