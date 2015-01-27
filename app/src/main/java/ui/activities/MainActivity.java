package ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ui.fragments.AerobicGoalDialog;
import ui.fragments.RunGoalDialog;
import ui.fragments.RunGoalDialog.CaloriesDialogListener;
import ui.utils.adapters.NavigationAdapter;


public class MainActivity extends Activity
        implements CaloriesDialogListener {

    public static final String TAG = "BasicHistoryApi";

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mClient;

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

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();
        addSideNavigation();

        Permission[] permissions = new Permission[] {
                Permission.PUBLISH_ACTION
        };

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId("562228307244897")
                .setNamespace("dreamteen-fitness")
                .setPermissions(permissions)
                .build();

        SimpleFacebook.setConfiguration(configuration);
    }

    /**
     * Build a GoogleApiClient that will authenticate the user and allow the application
     * to connect to Fitness APIs.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Now you can make calls to the Fitness APIs.
                                readExpandedCalories();
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
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            MainActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(MainActivity.this,
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

    private void readExpandedCalories() {
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
        updateUiCounters();
    }

    private void dumpSteps(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                increaseStepCount(
                        dp.getValue(field).asInt());
            }
        }
        updateUiCounters();
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

        if (mCaloriesExpandedTotal >= mSharedPrefs.getInt("calories_norm", CALORIES_DEFAULT)) {
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

        int stepsTarget = mSharedPrefs.getInt("steps_target",
                Integer.parseInt(getResources().getString(R.string.steps_target_default_value)));

        mStepsLabel.setText(mStepsTaken + "");

        int stepsLeft = stepsTarget - mStepsTaken;
        if (stepsLeft < 0) stepsLeft = 0;
        mStepsTargetLabel.setText(stepsLeft + "");
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
                .putInt("steps_target", newValue).apply();
        updateUiCounters();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
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
    protected void onResume() {
        super.onResume();
        if (!mSharedPrefs.getBoolean("pref_track_calories", true))
            mCaloriesContainer.setVisibility(View.GONE);
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
                R.drawable.ic_nav_challenges, R.drawable.ic_nav_settings
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
                startActivity(new Intent(MainActivity.this, ChallengesActivity.class));
            } else if (position == 3) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (position == 4) {
                if (mClient.isConnected()) {
                    // 1. Invoke the Config API with the Google API client object
                    Fitness.ConfigApi.disableFit(mClient);
                    // 2. Disconnect GoogleApiClient
                    mClient.disconnect();
                }
                // 3. Go to splash screen to re-login
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
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
        DialogFragment newFragment = new RunGoalDialog();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "dialog_run_goal");
    }
}
