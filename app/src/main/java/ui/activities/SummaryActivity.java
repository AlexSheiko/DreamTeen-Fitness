package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;

public class SummaryActivity extends Activity {
    public static final String TAG = SummaryActivity.class.getSimpleName();
    public static final String SESSION_NAME = "DreamTeen Fitness run";
    private static final int REQUEST_OAUTH = 1;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;
    private Session mSession;

    private String mDistance;
    private String mDuration;
    private String mDateTime;

    private EditText tripNameField;
    private SharedPreferences sharedPrefs;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();
        mClient.connect();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getString("pref_units", "1").equals("1")) {
            ((TextView) findViewById(R.id.TripLabelUnits)).setText("miles");
        } else {
            ((TextView) findViewById(R.id.TripLabelUnits)).setText("pref_units");
        }

        // Get trip info
        mDistance = sharedPrefs.getString("Distance", "0.00");
        mDuration = sharedPrefs.getString("Duration", "00:00");
        mDateTime = sharedPrefs.getString("DateTime", "Unspecified");

        // Update counters with trip info
        ((TextView) findViewById(R.id.TripLabelDistance)).setText(mDistance);

        tripNameField = (EditText) findViewById(R.id.tripNameField);
        tripNameField.setHint("Run on " + mDateTime);

        TextView discardButton = (TextView) findViewById(R.id.discardButton);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SummaryActivity.super.onBackPressed();
            }
        });
    }

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Play with some sessions!!
                                new ReadSessionTask().execute();
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
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            SummaryActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(SummaryActivity.this,
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

    private class ReadSessionTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
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
                    .read(DataType.TYPE_ACTIVITY_SAMPLE)
                    .setSessionName(SESSION_NAME)
                    .build();

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

    public void saveRun(View view) {
        startActivity(new Intent(SummaryActivity.this, MainActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_summary, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        if (mShareActionProvider != null)
            mShareActionProvider.setShareIntent(shareIntent());

        // Return true to display menu
        return true;
    }

    private Intent shareIntent() {

        String mMessage;
        if (sharedPrefs.getString("pref_units", "1").equals("1")) {
            mMessage = "Just finished a run of " + mDistance + " miles " +
                    "in " + mDuration + ".\n\nAny challengers? ;)\n#DreamFit";
        } else {
            mMessage = "Just finished a run of " + mDistance + " km " +
                    "in " + mDuration + ".\n\nAny challengers? ;)\n#DreamFit";
        }

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, mMessage);
        return intent;
    }
}
