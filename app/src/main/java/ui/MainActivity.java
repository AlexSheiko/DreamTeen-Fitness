package ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import bellamica.tech.dreamfit.R;
import utils.GoalDialog;
import utils.NavigationAdapter;


public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mClient = null;

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Parse.initialize(MainActivity.this,
                "ygyWodAYEDqiQ795p1V4Jxs2yRm9KTiBKsGSnakD",
                "IGTbu2n4KePoggvgXmBUS4k6cg5wQH8lQOA3Uo3k");

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        setCaloriesNorm();
        addSideNavigation();
        buildFitnessClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sharedPrefs.getBoolean("pref_track_calories", true))
            findViewById(R.id.caloriesContainer).setVisibility(View.GONE);
    }

    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Log.i(TAG, "Connected!!!");
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                    Log.i(TAG, "Connection lost.");
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new OnConnectionFailedListener() {
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
                                    } catch (SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .useDefaultAccount()
                .build();
    }

    public Void setCaloriesNorm() {
        if (sharedPrefs.getString("pref_units", "1").equals("1"))
            ((TextView) findViewById(R.id.weightToGoLabel)).setText("lb to go");

        String mDeviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PhoneReg");
        query.whereEqualTo("deviceId", mDeviceId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    int mAge = parseObject.getInt("age");
                    int mHeight = parseObject.getInt("height");
                    int mWeight = parseObject.getInt("weight");
                    String mGender = parseObject.getString("gender");

                    updateProgressBar(calculateDailyCaloriesNorm(mAge, mHeight, mWeight, mGender));

                    if (parseObject.getBoolean("isGoalSet")) {
                        findViewById(R.id.goalWeightContainer).setVisibility(View.VISIBLE);
                        findViewById(R.id.setGoalWeightContainer).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.weightDeltaToGo)).setText(parseObject.getInt("weightDelta") + "");

                        String DATE_FORMAT = "dd MMM";
                        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

                        Calendar cal = Calendar.getInstance();
                        Date now = new Date();
                        cal.setTime(now);
                        cal.add(Calendar.DAY_OF_YEAR, parseObject.getInt("daysLeft"));
                        long endTime = cal.getTimeInMillis();

                        if (sharedPrefs.getString("pref_units", "1").equals("1")) {
                            ((TextView) findViewById(R.id.goalHintLabel)).setText(
                                    mWeight + " " + "lb" + " until " + dateFormat.format(endTime).toLowerCase());
                        } else {
                            ((TextView) findViewById(R.id.goalHintLabel)).setText(
                                    Math.round(mWeight * 0.45) + " " + "kg" + " until " + dateFormat.format(endTime).toLowerCase());
                        }
                    }
                }
            }
        });
        return null;
    }

    private int calculateDailyCaloriesNorm(int age, float height, float weight, String gender) {
        if (gender.equals("male")) {
            return (int) (65 + (6.2 * weight) + (12.7 * height) - (6.8 * age));
        } else if (gender.equals("female")) {
            return (int) (655 + (4.3 * weight) + (4.3 * height) - (4.7 * age));
        }
        return -1;
    }

    private void updateProgressBar(int caloriesToExpandDaily) {

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(caloriesToExpandDaily);

        // Average expansion by day
        int caloriesBurnedByDefault = Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY) * 1465 / 24;

        ((TextView) findViewById(R.id.caloriesValueLabel))
                .setText(caloriesBurnedByDefault + "");

        progressBar.setProgress(caloriesBurnedByDefault);
    }

    public void addFitnessGoal(View view) {
        startActivity(new Intent(this, GoalDialog.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }


    // Navigation drawer
    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private void addSideNavigation() {
        mActionBar = getActionBar();
        mTitle = mDrawerTitle = getTitle();
        String[] mActionTitles = getResources().getStringArray(R.array.action_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

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
                comingSoonToast();
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
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }

}
