package ui.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionStopResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import ui.fragments.MapPane;
import ui.fragments.MapPane.OnWorkoutStateChanged;


public class RunActivity extends Activity
        implements OnWorkoutStateChanged {
    public static final String TAG = "BasicSessions";
    public static final String SESSION_NAME = "Run";
    public static String SESSION_IDENTIFIER = "dreamteenfitness.RUN.";
    private static final int REQUEST_OAUTH = 1;

    private static final int WORKOUT_START = 1;
    private static final int WORKOUT_PAUSE = 2;
    private static final int WORKOUT_FINISH = 3;

    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;
    private Session mSession;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        SESSION_IDENTIFIER += new SimpleDateFormat("dd MMM, hh:mm:ss").format(new Date()).toLowerCase();

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
                // and start new Session
                mClient.connect();
                break;

            case WORKOUT_PAUSE:
                stopSession();
                break;

            case WORKOUT_FINISH:
                startActivity(new Intent(this, SummaryActivity.class));
                break;
        }
    }

    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     */
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
                                startSession();
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

    private void startSession() {
        new StartSessionTask().execute();
    }

    private void stopSession() {
        new StopSessionTask().execute();
    }

    private class StartSessionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Setting start and end times for our run.
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long startTime = cal.getTimeInMillis();

            // 1. Subscribe to fitness data
            Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_ACTIVITY_SAMPLE)
                    .setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                        @Override
                        public void onResult(com.google.android.gms.common.api.Status status) {
                            if (!status.isSuccess()) {
                                Log.i(TAG, "There was a problem subscribing.");
                            }
                        }
                    });

            // 2. Create a session object
            // (providing a name, identifier, description and start time)
            mSession = new Session.Builder()
                    .setName(SESSION_NAME)
                    .setIdentifier(SESSION_IDENTIFIER)
                    .setDescription(SESSION_NAME)
                    .setStartTime(startTime, TimeUnit.MILLISECONDS)
                    // optional - if your app knows what activity:
                    .setActivity(FitnessActivities.RUNNING)
                    .build();

            // 3. Invoke the Sessions API with:
            // - The Google API client object
            // - The request object
            PendingResult<com.google.android.gms.common.api.Status> pendingResult =
                    Fitness.SessionsApi.startSession(mClient, mSession);

            sharedPrefs.edit()
                    .putString("Identifier", SESSION_IDENTIFIER).apply();
            return null;
        }
    }

    private class StopSessionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // 1. Invoke the Sessions API with:
            // - The Google API client object
            // - The name of the session
            // - The session identifier
            PendingResult<SessionStopResult> pendingResult =
                    Fitness.SessionsApi.stopSession(mClient, mSession.getIdentifier());

            // 2. Unsubscribe from fitness data (see Recording Fitness Data)
            Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_ACTIVITY_SAMPLE)
                    .setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                        @Override
                        public void onResult(com.google.android.gms.common.api.Status status) {
                            if (!status.isSuccess()) {
                                // Subscription not removed
                                Log.i(TAG, "Failed to unsubscribe for data type: " + DataType.TYPE_ACTIVITY_SAMPLE);
                            }
                            if (mClient.isConnected())
                                mClient.disconnect();
                        }
                    });
            return null;
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