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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest.Builder;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ui.fragments.GoalSetDialog;
import ui.utils.adapters.NavigationAdapter;
import ui.utils.helpers.Constants;


public class MainActivity extends Activity {

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";

    private GoogleApiClient mClient;
    private OnDataPointListener mStepsListener;
    private boolean authInProgress = false;

    private int mCaloriesExpended;
    private int mStepsTaken;
    private long mDailyDuration;

    private SharedPreferences mSharedPrefs;

    @InjectView(R.id.caloriesLabel)
    TextView mCaloriesLabel;
    @InjectView(R.id.progressBar)
    ProgressBar mPbCalories;
    @InjectView(R.id.caloriesContainer)
    LinearLayout mCaloriesContainer;
    @InjectView(R.id.stepsContainer)
    LinearLayout mStepsContainer;
    @InjectView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.drawerList)
    ListView mDrawerList;
    @InjectView(R.id.stepsLabel)
    TextView mStepsTakenLabel;
    @InjectView(R.id.stepsTargetLabel)
    TextView mStepProgressLabel;
    @InjectView(R.id.caloriesNotSetLabel)
    TextView mCalNotSetLabel;

    private WakeLock mWakeLock;

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

    private void buildFitnessClient() {
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
                .addScope(Fitness.SCOPE_LOCATION_READ)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(
                        new ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {

                                readStepsAndCalories();
                                readDuration();

                                registerStepsListener();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                if (!result.hasResolution()) {
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            MainActivity.this, 0).show();
                                    return;
                                }
                                if (!authInProgress) {
                                    try {
                                        authInProgress = true;
                                        result.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException ignored) {
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mClient.connect();

        if (!mSharedPrefs.getBoolean("pref_track_calories", true))
            mCaloriesContainer.setVisibility(View.GONE);
        if (!mSharedPrefs.getBoolean("pref_track_steps", true))
            mStepsContainer.setVisibility(View.GONE);
    }

    @Override protected void onDestroy() {
        super.onDestroy();

        unregisterStepsListener();

        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    private void registerStepsListener() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        mWakeLock.acquire();

        mStepsListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    int increment = dataPoint.getValue(field).asInt();
                    insertSteps(increment);
                    mStepsTaken += increment;
                    mStepsTakenLabel.setText(
                            String.format("%s", mStepsTaken));
                }
            }
        };

        Fitness.SensorsApi.add(mClient, new SensorRequest.Builder()
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                mStepsListener);
    }

    private void unregisterStepsListener() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        if (mClient != null && mStepsListener != null && mClient.isConnected()) {
            Fitness.SensorsApi.remove(mClient, mStepsListener);
        }
    }

    private void insertSteps(int increment) {
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
                .setName("Steps taken")
                .setType(DataSource.TYPE_DERIVED)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(increment);
        dataSet.add(dataPoint);

        // Invoke the History API to insert the data
        Fitness.HistoryApi.insertData(mClient, dataSet);
    }

    private void readStepsAndCalories() {
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
                    public void onResult(DataReadResult result) {
                        for (DataSet dataSet : result.getDataSets()) {

                            if (dataSet.getDataType().equals(DataType.TYPE_CALORIES_EXPENDED)) {
                                dumpCalories(dataSet);
                            } else if (dataSet.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                                dumpSteps(dataSet);
                            }
                        }
                        updateUi();
                    }
                });
    }

    private void dumpCalories(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                int increment = Math.round(dp.getValue(field).asFloat());
                increaseCalories(increment);
            }
        }
    }

    private void dumpSteps(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                int increment = dp.getValue(field).asInt();
                increaseSteps(increment);
            }
        }
        // Submit steps to leaderboard
        final String LEADERBOARD_STEPS_ID =
                getResources().getString(R.string.leaderboard_steps_taken);
        Games.Leaderboards.submitScore(
                mClient, LEADERBOARD_STEPS_ID, mStepsTaken);
    }

    private void increaseCalories(int increment) {
        mCaloriesExpended += increment;
    }

    private void increaseSteps(int increment) {
        mStepsTaken += increment;
    }

    private void updateUi() {
        //[START Calories]
        mCaloriesLabel.setText(mCaloriesExpended + "");

        // Set progress bar visibility
        int caloriesGoal = mSharedPrefs.getInt("calories_norm", -1);
        if (caloriesGoal != -1) {
            mPbCalories.setVisibility(View.VISIBLE);
            mPbCalories.setMax(caloriesGoal);
            mPbCalories.setProgress(mCaloriesExpended);

            // Set progress bar color
            if (mCaloriesExpended >= caloriesGoal) {
                setPbColor(mPbCalories, R.drawable.pb_reached);
            } else {
                setPbColor(mPbCalories, R.drawable.pb_calories);
            }
            notifyCalories(mCaloriesExpended, caloriesGoal);

            mCalNotSetLabel.setVisibility(View.GONE);
        } else {
            mPbCalories.setVisibility(View.GONE);
            mCalNotSetLabel.setVisibility(View.VISIBLE);
        }
        //[END Calories]

        //[START Steps]
        mStepsTakenLabel.setText(mStepsTaken + "");

        int stepsGoal = mSharedPrefs.getInt("daily_steps", -1);
        if (stepsGoal != -1) {
            int stepsLeft = stepsGoal - mStepsTaken;
            if (stepsLeft < 0) {
                stepsLeft = 0;
            }
            mStepProgressLabel.setText(stepsLeft + " steps to goal");
            notifySteps(stepsGoal);
        }

        // Reset step goal if needed
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int stepsExpireDay = mSharedPrefs.getInt("daily_steps_time", -1);
        if (stepsExpireDay != -1 && currentDay >= stepsExpireDay) {
            mSharedPrefs.edit()
                    .putInt("daily_steps", -1)
                    .putInt("daily_steps_time", -1)
                    .apply();
            mStepProgressLabel.setText("Goal not set");
            updateUi();
        }
        //[END Steps]

        //[START Duration]
        int notifyRun100 = mSharedPrefs.getInt("notify_run_100", 0);
        int notifyRun50 = mSharedPrefs.getInt("notify_run_50", 0);
        if (notifyRun100 == 1) {
            showNotification("Run", 100);
            mSharedPrefs.edit()
                    .putInt("notify_run_100", 2).apply();
        } else if (notifyRun50 == 1) {
            showNotification("Run", 50);
            mSharedPrefs.edit()
                    .putInt("notify_run_50", 2).apply();
        }

        int durationExpireDay = mSharedPrefs.getInt("weekly_duration_time", -1);
        if (durationExpireDay != -1 && currentDay >= durationExpireDay) {
            mSharedPrefs.edit()
                    .putInt("weekly_duration", -1)
                    .putInt("weekly_duration_time", -1)
                    .apply();
        }
        //[END Duration]

        //[START Calories]
        int caloriesExpireDay = mSharedPrefs.getInt("calories_norm_time", -1);
        if (caloriesExpireDay != -1 && currentDay >= caloriesExpireDay) {
            mSharedPrefs.edit()
                    .putInt("calories_norm", -1)
                    .putInt("calories_norm_time", -1)
                    .apply();
            updateUi();
        }
        //[END Calories]
    }

    private void setPbColor(ProgressBar pb, int drawableId) {
        Rect bounds = pb.getProgressDrawable().getBounds();
        pb.setProgressDrawable(getResources()
                .getDrawable(drawableId));
        pb.getProgressDrawable().setBounds(bounds);
    }

    private void notifySteps(int goal) {

        boolean isSteps100notified =
                mSharedPrefs.getBoolean("isSteps100notified", false);
        boolean isSteps50notified =
                mSharedPrefs.getBoolean("isSteps50notified", false);

        if (mStepsTaken >= goal
                && !isSteps100notified) {
            showNotification("Steps", 100);
            mSharedPrefs.edit()
                    .putBoolean("isSteps100notified", true).apply();
        } else if (mStepsTaken >= goal * 0.5
                && mStepsTaken < goal
                && !isSteps50notified) {
            showNotification("Steps", 50);
            mSharedPrefs.edit()
                    .putBoolean("isSteps50notified", true).apply();
        }
    }

    private void notifyCalories(int calories, int goal) {
        boolean isCal50notified =
                mSharedPrefs.getBoolean("isCal50notified", false);
        if (calories >= goal * 0.5
                && calories < goal
                && !isCal50notified) {
            showNotification("Calories", 50);
            mSharedPrefs.edit()
                    .putBoolean("isCal50notified", true).apply();
        }

        boolean isCal100notified =
                mSharedPrefs.getBoolean("isCal100notified", false);
        if (calories >= goal
                && !isCal100notified) {
            showNotification("Calories", 100);
            mSharedPrefs.edit()
                    .putBoolean("isCal100notified", true).apply();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == RESULT_OK) {
                authInProgress = false;
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnected() && !mClient.isConnecting()) {
                    mClient.connect();
                }
            } else if (resultCode == RESULT_FIRST_USER) {
                AccountManager acm = AccountManager.get(getApplicationContext());
                acm.addAccount("com.google", null, null, null, MainActivity.this, null, null);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    // Navigation drawer
    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private void addSideNavigation() {
        mActionBar = getActionBar();
        mTitle = mDrawerTitle = getTitle();
        String[] mActionTitles =
                getResources().getStringArray(R.array.action_titles);

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
                if (mClient != null && mClient.isConnected()) {
                    final int REQUEST_LEADERBOARD = 2;
                    startActivityForResult(Games.Leaderboards
                            .getAllLeaderboardsIntent(mClient), REQUEST_LEADERBOARD);
                } else {
                    Toast.makeText(MainActivity.this,
                            "Leaderboards not available", Toast.LENGTH_SHORT).show();
                }
            } else if (position == 3) {
                startActivity(new Intent(MainActivity.this, GoalsActivity.class));
            } else if (position == 4) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        }
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
            Bundle bundle = new Bundle();
            bundle.putString("key", "aerobic");
            DialogFragment dialog = new GoalSetDialog();
            dialog.setArguments(bundle);
            dialog.show(getFragmentManager(), "dialog_aerobic_goal");
        }
    }

    private void readDuration() {
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
}