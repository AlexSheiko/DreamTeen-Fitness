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
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;

public class SummaryActivity extends Activity {
    public static final String TAG = SummaryActivity.class.getSimpleName();
    public static final String SESSION_NAME = "Run";
    private static final int REQUEST_OAUTH = 1;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;

    private String mSessionIdentifier;

    private float mDistance;
    private String mDuration;
    private String mDateTime;

    private SharedPreferences sharedPrefs;

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
        mSessionIdentifier = sharedPrefs.getString("Identifier", "");

        // Get run info
        mDuration = sharedPrefs.getString("Duration", "00:00");
        mDateTime = sharedPrefs.getString("DateTime", "Unspecified");

        EditText tripNameField = (EditText) findViewById(R.id.tripNameField);
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

            // Build a data read request
            DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();

            // Invoke the Sessions API to fetch the data with the query and wait for the result
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mClient, dataReadRequest).await(1, TimeUnit.MINUTES);

            // Process the data sets for this session
            DataSet dataSet = dataReadResult.getDataSet(DataType.TYPE_DISTANCE_DELTA);
            for (DataPoint dp : dataSet.getDataPoints()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                for (Field field : dp.getDataType().getFields()) {
                    float increment = dp.getValue(field).asFloat() * 0.000621371192f;
                    Log.i(TAG, "Increment by " + increment);
                    incrementDistance(increment);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            // Update UI with run info
            ((TextView) findViewById(R.id.TripLabelDistance)).setText(
                    String.format("%.2f", mDistance)
            );
        }
    }

    private void incrementDistance(float increment) {
        mDistance =+ increment;
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
        ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();

        if (shareActionProvider != null)
            shareActionProvider.setShareIntent(shareIntent());

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
