package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;

public class AerobicActivity extends Activity {

    public static final String TAG = "DreamTeen Fitness";
    public static final String SAMPLE_SESSION_NAME = "DreamTeen Fitness-aerobic-session";
    private static final int REQUEST_OAUTH = 1;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;

    private Spinner mMonthSpinner;
    private Spinner mDaySpinner;
    private NumberPicker mNumberPicker;
    private int mWorkoutDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerobic);
        initializeViews();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();
    }

    /**
     * Build a {@link com.google.android.gms.common.api.GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or
     * having multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addConnectionCallbacks(
                        new ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Look at some data!!
                                new InsertAndVerifySessionTask().execute();
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
                                            AerobicActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        authInProgress = true;
                                        result.startResolutionForResult(AerobicActivity.this,
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

    /**
     * Create and execute a {@link com.google.android.gms.fitness.request.SessionInsertRequest} to insert a session into the History API,
     * and then create and execute a {@link com.google.android.gms.fitness.request.SessionReadRequest} to verify the insertion succeeded.
     * By using an AsyncTask to make our calls, we can schedule synchronous calls, so that we can
     * query for sessions after confirming that our insert was successful. Using asynchronous calls
     * and callbacks would not guarantee that the insertion had concluded before the read request
     * was made. An example of an asynchronous call using a callback can be found in the example
     * on deleting sessions below.
     */
    private class InsertAndVerifySessionTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            //First, create a new session and an insertion request.
            SessionInsertRequest insertRequest = insertFitnessSession();

            // [START insert_session]
            // Then, invoke the Sessions API to insert the session and await the result,
            // which is possible here because of the AsyncTask. Always include a timeout when
            // calling await() to avoid hanging that can occur from the service being shutdown
            // because of low memory or other conditions.
            Log.i(TAG, "Inserting the session in the History API");
            com.google.android.gms.common.api.Status insertStatus =
                    Fitness.SessionsApi.insertSession(mClient, insertRequest)
                            .await(1, TimeUnit.MINUTES);

            // Before querying the session, check to see if the insertion succeeded.
            if (!insertStatus.isSuccess()) {
                Log.i(TAG, "There was a problem inserting the session: " +
                        insertStatus.getStatusMessage());
                return null;
            }

            // At this point, the session has been inserted and can be read.
            Log.i(TAG, "Session insert was successful!");
            // [END insert_session]

            // Begin by creating the query.
            SessionReadRequest readRequest = readFitnessSession();

            // [START read_session]
            // Invoke the Sessions API to fetch the session with the query and wait for the result
            // of the read request.
            SessionReadResult sessionReadResult =
                    Fitness.SessionsApi.readSession(mClient, readRequest)
                            .await(1, TimeUnit.MINUTES);

            // Get a list of the sessions that match the criteria to check the result.
            Log.i(TAG, "Session read was successful. Number of returned sessions is: "
                    + sessionReadResult.getSessions().size());
            for (Session session : sessionReadResult.getSessions()) {
                // Process the session
                dumpSession(session);

                // Process the data sets for this session
                List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }

            return null;
        }
    }

    /**
     * Create a {@link SessionInsertRequest} for a run.
     * <p/>
     * {@link Session}s are time intervals that are associated with all Fit data that falls into
     * that time interval. This data can be inserted when inserting a session or independently,
     * without affecting the association between that data and the session. Future queries for
     * that session will return all data relevant to the time interval created by the session.
     * <p/>
     * Sessions may contain {@link DataSet}s, which are comprised of {@link com.google.android.gms.fitness.data.DataPoint}s and a
     * {@link com.google.android.gms.fitness.data.DataSource}.
     * A {@link com.google.android.gms.fitness.data.DataPoint} is associated with a Fit {@link com.google.android.gms.fitness.data.DataType}, which may be
     * derived from the {@link com.google.android.gms.fitness.data.DataSource}, as well as a time interval, and a value. A given
     * {@link DataSet} may only contain data for a single data type, but a {@link Session} can
     * contain multiple {@link DataSet}s.
     */
    private SessionInsertRequest insertFitnessSession() {
        Log.i(TAG, "Creating a new session for an aerobic workout");
        // Setting start and end times for our run.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        // Set a range of the run, using a start time of workout duration before this moment.
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -mWorkoutDuration);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource caloriesDataSource = new DataSource.Builder()
                .setAppPackageName(this.getPackageName())
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setName(TAG + "-aerobic")
                // TODO: Change it to «raw» after first test
                .setType(DataSource.TYPE_DERIVED)
                .build();

        float caloriesBurnedTotal = 386 / 60 * mWorkoutDuration; // Hourly average for teenage girl / 60 minutes * workout duration in minutes
        // Create a data set of the run speeds to include in the session.
        DataSet caloriesDataSet = DataSet.create(caloriesDataSource);

        DataPoint workoutCaloriesBurned = caloriesDataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        workoutCaloriesBurned.getValue(Field.FIELD_CALORIES).setFloat(caloriesBurnedTotal);
        caloriesDataSet.add(workoutCaloriesBurned);

        // [START build_insert_session_request]
        // Create a session with metadata about the activity.
        Session session = new Session.Builder()
                .setName(SAMPLE_SESSION_NAME)
                .setDescription(TAG + " — Aerobic Workout")
                .setIdentifier(SAMPLE_SESSION_NAME + "-" +
                        new SimpleDateFormat("dd MMM, hh:mm").format(new Date()).toLowerCase())
                .setActivity(FitnessActivities.AEROBICS)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS)
                .build();

        // Build a session insert request
        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(session)
                .addDataSet(caloriesDataSet)
                .build();
        // [END build_insert_session_request]
        // [END build_insert_session_request_with_activity_segments]

        return insertRequest;
    }

    /**
     * Return a {@link com.google.android.gms.fitness.request.SessionReadRequest} for all speed data in the past week.
     */
    private SessionReadRequest readFitnessSession() {
        Log.i(TAG, "Reading History API results for session: " + SAMPLE_SESSION_NAME);
        // [START build_read_session_request]
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_SPEED)
                .setSessionName(SAMPLE_SESSION_NAME)
                .build();
        // [END build_read_session_request]

        return readRequest;
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    private void dumpSession(Session session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Log.i(TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }

    private void initializeViews() {
        mMonthSpinner = (Spinner) findViewById(R.id.monthSpinner);
        ArrayAdapter<CharSequence> mMonthAdapter = ArrayAdapter.createFromResource(this,
                R.array.month_values, android.R.layout.simple_spinner_item);
        mMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonthSpinner.setAdapter(mMonthAdapter);
        mMonthSpinner.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        mDaySpinner = (Spinner) findViewById(R.id.daySpinner);
        ArrayAdapter<CharSequence> mDayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_values, android.R.layout.simple_spinner_item);
        mDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDaySpinner.setAdapter(mDayAdapter);
        mDaySpinner.setSelection(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1);

        mNumberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        mNumberPicker.setMinValue(5);
        mNumberPicker.setMaxValue(90);
        mNumberPicker.setValue(20);
        mNumberPicker.setWrapSelectorWheel(false);
    }

    public void saveData(View view) {

        mWorkoutDuration = mNumberPicker.getValue();

        Calendar calendar = Calendar.getInstance();
        String mYear = calendar.get(Calendar.YEAR) + "";
        String mMonth = mMonthSpinner.getSelectedItem().toString();
        String mDay = mDaySpinner.getSelectedItem().toString();
        String mHour = calendar.get(Calendar.HOUR) + "";
        String mMinute = calendar.get(Calendar.MINUTE) + "";
        String mSecond = calendar.get(Calendar.SECOND) + "";

        String dateString =
                mMonth + "." + mDay + "." + mYear + " " +
                        mHour + ":" + mMinute + ":" + mSecond;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM.d.yyyy k:m:s", Locale.US);
        Date convertedDate;
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.w(TAG, "Parse string date failed. Exception: " + e.getMessage());
            convertedDate = new Date();
        }
        calendar.setTime(convertedDate);

        if (!mClient.isConnected()) {
            mClient.connect();
        }

        navigateToMainScreen();
    }

    private void navigateToMainScreen() {
        Toast.makeText(this, "Workout saved", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
