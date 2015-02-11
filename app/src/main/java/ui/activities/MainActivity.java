package ui.activities;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest.Builder;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ui.fragments.AerobicGoalDialog;
import ui.fragments.MainGoalsDialog;
import ui.fragments.MainGoalsDialog.CaloriesDialogListener;
import ui.utils.adapters.NavigationAdapter;
import ui.utils.helpers.Constants;


public class MainActivity extends Activity
        implements CaloriesDialogListener {

    private int mDailySteps;
    private long mDailyDuration;

    private GoogleApiClient mClient;
    private boolean authInProgress = false;
    private static final String AUTH_PENDING = "auth_state_pending";

    private OnDataPointListener mStepsListener;
    private static final int REQUEST_OAUTH = 1;
    private static final int REQUEST_LEADERBOARD = 2;

    private SharedPreferences mSharedPrefs;
    private int mCaloriesExpanded = 0;
    private int mStepsTaken = 0;
    private static final int CALORIES_DEFAULT = 2150;

    @InjectView(R.id.caloriesLabel)
    TextView mCaloriesLabel;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.caloriesContainer)
    LinearLayout mCaloriesContainer;
    @InjectView(R.id.stepsContainer)
    LinearLayout mStepsContainer;
    @InjectView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.drawerList)
    ListView mDrawerList;
    @InjectView(R.id.stepsLabel)
    TextView mStepsLabel;
    @InjectView(R.id.stepsTargetLabel)
    TextView mStepsTargetLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();
        addSideNavigation();

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * Build a GoogleApiClient that will authenticate the user and allow the application
     * to connect to Fitness APIs
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addApi(Games.API)
                .addApi(Plus.API)
                .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
                .addScope(Fitness.SCOPE_LOCATION_READ)
                .addScope(Games.SCOPE_GAMES)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(
                        new ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Now make calls to the APIs
                                updateCaloriesAndSteps();
                                startListeningSteps();
                                readStepCount();
                                readMinutes();
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
                                            MainActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                if (!authInProgress) {
                                    try {
                                        authInProgress = true;
                                        result.startResolutionForResult(MainActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException ignored) {
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_OAUTH) {
                authInProgress = false;
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnected() && !mClient.isConnecting()) {
                    mClient.connect();
                }
            }
        } else if (resultCode == RESULT_FIRST_USER) {
            AccountManager acm = AccountManager.get(getApplicationContext());
            acm.addAccount("com.google", null, null, null, MainActivity.this, null, null);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startListeningSteps() {
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            //Let's register a listener to receive Activity data!
                            if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)
                                    && mStepsListener == null) {
                                registerStepsListener(dataSource,
                                        DataType.TYPE_STEP_COUNT_DELTA);
                            }
                        }
                    }
                });
    }

    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
    private void registerStepsListener(DataSource dataSource, DataType dataType) {
        // [START register_data_listener]
        mStepsListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    insertSteps(val.asInt());
                }
            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                mStepsListener);
        // [END register_data_listener]
    }

    private void stopListeningSteps() {
        Fitness.SensorsApi.remove(mClient, mStepsListener);
    }

    private void insertSteps(final int steps) {
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
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName(Constants.SESSION_NAME + " - step count")
                .setType(DataSource.TYPE_DERIVED)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(steps);
        dataSet.add(dataPoint);

        // Invoke the History API to insert the data
        Fitness.HistoryApi.insertData(mClient, dataSet).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    increaseStepCount(steps);
                    updateUiCounters();
                }
            }
        });
    }

    private void updateCaloriesAndSteps() {
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        // Get time from the start (00:00) of a day
        cal.add(Calendar.HOUR_OF_DAY, -Calendar.HOUR_OF_DAY);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readCaloriesRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        // Invoke the History API to fetch the data with the query
        Fitness.HistoryApi.readData(mClient, readCaloriesRequest).setResultCallback(
                new ResultCallback<DataReadResult>() {
                    @Override
                    public void onResult(DataReadResult dataReadResult) {
                        for (DataSet dataSet : dataReadResult.getDataSets()) {

                            if (dataSet.getDataType().equals(DataType.TYPE_CALORIES_EXPENDED)) {
                                dumpCalories(dataSet);
                            } else if (dataSet.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                                dumpSteps(dataSet);
                            }
                        }
                        updateUiCounters();
                    }
                });
    }

    private void dumpCalories(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                increaseCaloriesExpanded(
                        Math.round(dp.getValue(field).asFloat()));
            }
        }
    }

    private void dumpSteps(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                increaseStepCount(
                        dp.getValue(field).asInt());
            }
        }
    }

    private void increaseCaloriesExpanded(int increment) {
        mCaloriesExpanded = mCaloriesExpanded + increment;
    }

    private void increaseStepCount(int increment) {
        mStepsTaken = mStepsTaken + increment;
    }

    private void updateUiCounters() {
        // Average expansion by day
        int mCaloriesBurnedByDefault = Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY) * 1465 / 24;
        int mCaloriesExpandedTotal =
                mCaloriesBurnedByDefault + mCaloriesExpanded;

        mCaloriesLabel.setText(mCaloriesExpandedTotal + "");
        mProgressBar.setMax(
                mSharedPrefs.getInt("calories_norm", CALORIES_DEFAULT));
        mProgressBar.setProgress(mCaloriesExpandedTotal);

        int caloriesNorm = mSharedPrefs.getInt("calories_norm", CALORIES_DEFAULT);
        if (mCaloriesExpandedTotal >= caloriesNorm) {
            Rect bounds = mProgressBar.getProgressDrawable().getBounds();
            mProgressBar.setProgressDrawable(
                    getResources().getDrawable(R.drawable.progress_bar_calories_goal_reached));
            mProgressBar.getProgressDrawable().setBounds(bounds);
        } else {
            Rect bounds = mProgressBar.getProgressDrawable().getBounds();
            mProgressBar.setProgressDrawable(
                    getResources().getDrawable(R.drawable.progress_bar_calories));
            mProgressBar.getProgressDrawable().setBounds(bounds);
        }

        int stepsTarget = mSharedPrefs.getInt("daily_steps",
                Integer.parseInt(getResources().getString(R.string.steps_target_default_value)));

        boolean isSteps50notified = mSharedPrefs.getBoolean("isSteps50notified", false);
        if (mStepsTaken >= stepsTarget * 0.5
                && mStepsTaken < stepsTarget
                && !isSteps50notified) {
            showNotification("Steps", 50);
            mSharedPrefs.edit()
                    .putBoolean("isSteps50notified", true).apply();
        }

        mStepsLabel.setText(mStepsTaken + "");

        String LEADERBOARD_STEPS_ID = getResources().getString(
                R.string.leaderboard_steps_taken);
        Games.Leaderboards.submitScore(
                mClient, LEADERBOARD_STEPS_ID, mStepsTaken);

        boolean isSteps100notified = mSharedPrefs.getBoolean("isSteps100notified", false);
        int stepsLeft = stepsTarget - mStepsTaken;
        if (stepsLeft <= 0) {
            stepsLeft = 0;
            if (!isSteps100notified) {
                showNotification("Steps", 100);
                mSharedPrefs.edit()
                        .putBoolean("isSteps100notified", true).apply();
            }
        }
        mStepsTargetLabel.setText(stepsLeft + "");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long stepsEndTime = calendar.getTimeInMillis();

        String dailyStepsStr = mSharedPrefs.getString("daily_steps_time", "");
        long dailyStepsTime = 0;
        try {
            dailyStepsTime = Date.parse(dailyStepsStr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (dailyStepsTime > stepsEndTime) {
            mSharedPrefs.edit().putString("daily_steps_time", "").apply();
            updateUiCounters();
        }
    }

    @Override
    public void onCaloriesGoalChanged(DialogFragment dialog, int newValue) {
        mSharedPrefs.edit()
                .putInt("calories_norm", newValue).apply();
        updateUiCounters();
    }

    @Override
    public void onStepsGoalChanged(DialogFragment dialog, int newValue) {
        mSharedPrefs.edit()
                .putInt("daily_steps", newValue).apply();
        updateUiCounters();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        mClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListeningSteps();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mClient != null && mClient.isConnected()) {
            startListeningSteps();
        }
        if (!mSharedPrefs.getBoolean("pref_track_calories", true))
            mCaloriesContainer.setVisibility(View.GONE);
        if (!mSharedPrefs.getBoolean("pref_track_steps", true))
            mStepsContainer.setVisibility(View.GONE);
    }

    // Navigation drawer
    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private void addSideNavigation() {
        mActionBar = getActionBar();
        mTitle = mDrawerTitle = getTitle();
        String[] mActionTitles = getResources().getStringArray(R.array.action_titles);

        Integer[] mImageIds = new Integer[]{
                R.drawable.ic_nav_dashboard, R.drawable.ic_nav_friends,
                R.drawable.ic_nav_leaderboard, R.drawable.ic_nav_goals,
                R.drawable.ic_nav_settings
        };
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        NavigationAdapter adapter = new NavigationAdapter(this, mActionTitles, mImageIds);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                mActionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                mActionBar.setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    // The click listener for the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                mDrawerLayout.closeDrawers();
            } else if (position == 1) {
                startActivity(new Intent(MainActivity.this, FriendsActivity.class));
            } else if (position == 2) {
                if (isSignedIn()) {
                    startActivityForResult(
                            Games.Leaderboards.getAllLeaderboardsIntent(mClient), REQUEST_LEADERBOARD);
                } else {
                    Toast.makeText(MainActivity.this, "Leaderboards not available",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (position == 3) {
                startActivity(new Intent(MainActivity.this, GoalsActivity.class));
            } else if (position == 4) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        }
    }

    private boolean isSignedIn() {
        return (mClient != null && mClient.isConnected());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void navigateToRunning(View view) {
        startActivity(new Intent(this, RunActivity.class));
    }

    public void navigateToAerobic(View view) {
        if (mSharedPrefs.getBoolean("isGoalSet", false)) {
            startActivity(new Intent(this, AerobicActivity.class));
        } else {
            DialogFragment newFragment = new AerobicGoalDialog();
            newFragment.show(getFragmentManager(), "dialog_aerobic_goal");
        }
    }

    public void setFitnessGoal(View view) {
        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.caloriesContainer:
                bundle.putString("key", "calories");
                break;
            case R.id.stepsContainer:
                bundle.putString("key", "steps");
                break;
        }
        DialogFragment newFragment = new MainGoalsDialog();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "dialog_run_goal");
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
                        showNotificationIfNeeded();
                    }
                });
    }

    private void dumpDailySteps(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                increaseDailySteps(
                        dp.getValue(field).asInt());
            }
        }
    }

    private void increaseDailySteps(int increment) {
        mDailySteps = mDailySteps + increment;
    }

    private void readMinutes() {
        // [START build_read_session_request]
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -Calendar.DAY_OF_YEAR);
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .setSessionName(Constants.SESSION_NAME)
                .build();
        // [END build_read_session_request]

        Fitness.SessionsApi.readSession(mClient, readRequest)
                .setResultCallback(new ResultCallback<SessionReadResult>() {
                    @Override
                    public void onResult(SessionReadResult sessionReadResult) {
                        // Get a list of the sessions that match the criteria to check the result.
                        for (Session session : sessionReadResult.getSessions()) {
                            // Process the session
                            dumpDailyDuration(session);
                        }
                        showNotificationIfNeeded();
                    }
                });
    }

    private void dumpDailyDuration(Session session) {
        long startTime = session.getStartTime(TimeUnit.MILLISECONDS);
        long endTime = session.getEndTime(TimeUnit.MILLISECONDS);
        long diffInMs = endTime - startTime;
        long increment = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
        increaseDailyDuration(increment);
    }

    private void increaseDailyDuration(long increment) {
        mDailyDuration = mDailyDuration + increment;
    }

    private void showNotificationIfNeeded() {
        int dailySteps = mSharedPrefs.getInt("daily_steps", -1);
        int dailyDuration = mSharedPrefs.getInt("daily_duration", -1);

        boolean isSteps50notified = mSharedPrefs.getBoolean("isSteps50notified", false);
        boolean isSteps75notified = mSharedPrefs.getBoolean("isSteps75notified", false);
        boolean isSteps100notified = mSharedPrefs.getBoolean("isSteps100notified", false);
        boolean isRun50notified = mSharedPrefs.getBoolean("isRun50notified", false);
        boolean isRun75notified = mSharedPrefs.getBoolean("isRun75notified", false);
        boolean isRun100notified = mSharedPrefs.getBoolean("isRun100notified", false);

        if (dailySteps != -1) {
            if (mDailySteps >= dailySteps * 0.5
                    && mDailySteps < dailySteps * 0.75
                    && !isSteps50notified) {
                showNotification("Steps", 50);
                mSharedPrefs.edit()
                        .putBoolean("isSteps50notified", true).apply();
            } else if (mDailySteps >= dailySteps * 0.75
                    && mDailySteps < dailySteps
                    && !isSteps75notified) {
                showNotification("Steps", 75);
                mSharedPrefs.edit()
                        .putBoolean("isSteps75notified", true).apply();
            } else if (mDailySteps >= dailySteps
                    && !isSteps100notified) {
                showNotification("Steps", 100);
                mSharedPrefs.edit()
                        .putBoolean("isSteps100notified", true).apply();
            }
        }
        if (dailyDuration != -1) {
            if (mDailyDuration >= dailyDuration * 60 * 0.5
                    && mDailyDuration < dailyDuration * 60 * 0.75
                    && !isRun50notified) {
                showNotification("Run", 50);
                mSharedPrefs.edit()
                        .putBoolean("isRun50notified", true).apply();
            } else if (mDailyDuration >= dailyDuration * 60 * 0.75
                    && mDailyDuration < dailyDuration * 60
                    && !isRun75notified) {
                showNotification("Run", 75);
                mSharedPrefs.edit()
                        .putBoolean("isRun75notified", true).apply();
            } else if (mDailyDuration >= dailyDuration * 60
                    && !isRun100notified) {
                showNotification("Run", 100);
                mSharedPrefs.edit()
                        .putBoolean("isRun100notified", true).apply();
            }
        }
    }

    private void showNotification(String type, int progress) {
        String title;
        if (progress == 100) {
            title = type + " goal is reached!";
        } else {
            title = type + " goal is " + progress + "% reached!";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo_small)
                        .setContentTitle(title)
                        .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cheer));

        // Sets an ID for the notification
        int mNotificationId = 123;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}