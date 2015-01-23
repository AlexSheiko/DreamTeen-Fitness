package ui.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
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
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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

    /**
     *  Track whether an authorization activity is stacking over the current activity
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;
    private OnDataPointListener mLocationListener;
    private Date mRunStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

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
                mRunStartTime = setStartTime(this, new Date());
                // Connect to the Fitness API
                mClient.connect();
                break;

            case WORKOUT_PAUSE:
                if (mClient.isConnected()) {
                    stopLocationUpdates();
                    cancelSubscription();
                    mClient.disconnect();
                }
                break;

            case WORKOUT_FINISH:
                // TODO: Insert datasets for steps and calories
                new InsertAndVerifyDataTask().execute();
                startActivity(new Intent(this, SummaryActivity.class));
                break;
        }
    }

    private Date setStartTime(Context context, Date currentDate) {
        String startTime =
                new SimpleDateFormat("dd MMM, hh:mm", Locale.US).format(currentDate).toLowerCase();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("start_time", startTime).apply();
        return currentDate;
    }

    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Start updating map focus
                                startLocationUpdates();
                                subscribe();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
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

    /**
     * Subscribe to an available {@link DataType}. Subscriptions can exist across application
     * instances (so data is recorded even after the application closes down).  When creating
     * a new subscription, it may already exist from a previous invocation of this app.  If
     * the subscription already exists, the method is a no-op.  However, you can check this with
     * a special success code.
     */
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        // [START subscribe_to_datatype]
        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            Log.i(TAG, "There was a problem subscribing.");
                        }
                    }
                });

        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            Log.i(TAG, "There was a problem subscribing.");
                        }
                    }
                });
        // [END subscribe_to_datatype]
    }

    /**
     * Cancel the ACTIVITY_SAMPLE subscription by calling unsubscribe on that {@link DataType}.
     */
    private void cancelSubscription() {
        // Invoke the Recording API to unsubscribe from the data type and specify a callback that
        // will check the result.
        // [START unsubscribe_from_datatype]
        Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            // Subscription not removed
                            Log.i(TAG, "Failed to unsubscribe.");
                        }
                    }
                });

        Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            // Subscription not removed
                            Log.i(TAG, "Failed to unsubscribe.");
                        }
                    }
                });
        // [END unsubscribe_from_datatype]
    }

    /**
     * Find available data sources and attempt to register on a specific {@link DataType}.
     */
    private void startLocationUpdates() {
        // [START find_data_sources]
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE)
                        // Can specify whether data type is raw or derived.
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            //Let's register a listener to receive Activity data!
                            if (dataSource.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE)
                                    && mLocationListener == null) {
                                registerLocationDataListener(dataSource,
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
    private void registerLocationDataListener(DataSource dataSource, final DataType dataType) {
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
    private void stopLocationUpdates() {
        if (mLocationListener == null) return;

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

    /**
     *  Create a {@link DataSet} to insert data into the History API, and
     *  then create and execute a {@link com.google.android.gms.fitness.request.DataReadRequest} to verify the insertion succeeded.
     *  By using an {@link AsyncTask}, we can schedule synchronous calls, so that we can query for
     *  data after confirming that our insert was successful. Using asynchronous calls and callbacks
     *  would not guarantee that the insertion had concluded before the read request was made.
     *  An example of an asynchronous call using a callback can be found in the example
     *  on deleting data below.
     */
    private class InsertAndVerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            //First, create a new dataset and insertion request.
            DataSet dataSet = insertFitnessData();

            // [START insert_dataset]
            // Then, invoke the History API to insert the data and await the result, which is
            // possible here because of the {@link AsyncTask}. Always include a timeout when calling
            // await() to prevent hanging that can occur from the service being shutdown because
            // of low memory or other conditions.
            Log.i(TAG, "Inserting the dataset in the History API");
            com.google.android.gms.common.api.Status insertStatus =
                    Fitness.HistoryApi.insertData(mClient, dataSet)
                            .await(1, TimeUnit.MINUTES);

            // Before querying the data, check to see if the insertion succeeded.
            if (!insertStatus.isSuccess()) {
                Log.i(TAG, "There was a problem inserting the dataset.");
                return null;
            }

            // At this point, the data has been inserted and can be read.
            Log.i(TAG, "Data insert was successful!");
            // [END insert_dataset]

            return null;
        }
    }

    /**
     * Create and return a {@link DataSet} of step count data for the History API.
     */
    private DataSet insertFitnessData() {
        Log.i(TAG, "Creating a new data insert request");

        // [START build_insert_data_request]
        // Set a start and end time for our data, using a start time of 1 hour before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName(TAG + " - step count")
                .setType(DataSource.TYPE_RAW)
                .build();

        // Create a data set
        int stepCountDelta = 1020;
        DataSet dataSet = DataSet.create(dataSource);
        // For each data point, specify a start time, end time, and the data value -- in this case,
        // the number of new steps.
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
        dataSet.add(dataPoint);
        // [END build_insert_data_request]

        return dataSet;
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