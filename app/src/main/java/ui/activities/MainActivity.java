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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ui.fragments.SetCaloriesDialog;
import ui.fragments.SetCaloriesDialog.CaloriesDialogListener;
import ui.utils.adapters.NavigationAdapter;


public class MainActivity extends Activity
        implements CaloriesDialogListener {

    public static final String TAG = "BasicHistoryApi";

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mClient;

    private SharedPreferences sharedPrefs;
    private int mCaloriesExpanded = 0;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();
        addSideNavigation();
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
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        // Invoke the History API to fetch the data with the query
        Fitness.HistoryApi.readData(mClient, readRequest).setResultCallback(
                new ResultCallback<DataReadResult>() {
                    @Override
                    public void onResult(DataReadResult dataReadResult) {
                        if (dataReadResult.getBuckets().size() > 0) {
                            Log.i(TAG, "Number of returned buckets of DataSets is: "
                                    + dataReadResult.getBuckets().size());
                            for (Bucket bucket : dataReadResult.getBuckets()) {
                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet dataSet : dataSets) {
                                    dumpDataSet(dataSet);
                                }
                            }
                        } else if (dataReadResult.getDataSets().size() > 0) {
                            for (DataSet dataSet : dataReadResult.getDataSets()) {
                                dumpDataSet(dataSet);
                            }
                        }
                    }
                });
    }

    private void dumpDataSet(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                increaseCaloriesExpanded(
                        Math.round(dp.getValue(field).asFloat()));
            }
        }
        updateProgressBar();
    }

    @Override
    public void onDailyCaloriesNormChanged(DialogFragment dialog, int newValue) {
        sharedPrefs.edit()
                .putInt("calories_norm", newValue).apply();
        updateProgressBar();
    }

    private void increaseCaloriesExpanded(int increment) {
        mCaloriesExpanded = mCaloriesExpanded + increment;
    }

    private void updateProgressBar() {
        // Average expansion by day
        int mCaloriesBurnedByDefault = Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY) * 1465 / 24;
        int mCaloriesExpandedTotal =
                mCaloriesBurnedByDefault + mCaloriesExpanded;

        mCaloriesLabel.setText(mCaloriesExpandedTotal + "");
        mProgressBar.setMax(
                sharedPrefs.getInt("calories_norm", 1950));
        mProgressBar.setProgress(mCaloriesExpandedTotal);

        if (mCaloriesExpandedTotal >= sharedPrefs.getInt("calories_norm", 1950)) {
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
        if (!sharedPrefs.getBoolean("pref_track_calories", true))
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
                comingSoonToast();
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

        private void comingSoonToast() {
            Toast.makeText(MainActivity.this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
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
        startActivity(new Intent(this, AerobicActivity.class));
    }

    public void setDailyCaloriesNorm(View view) {
        DialogFragment newFragment = new SetCaloriesDialog();
        newFragment.show(getFragmentManager(), "calories");
    }
}
