package ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import ui.adapters.NavigationAdapter;
import bellamica.tech.dreamteenfitness.R;
import ui.fragments.BodyParamsInput;
import ui.fragments.GoalDialog;


public class MainActivity extends Activity {

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        setCaloriesNorm();
        addSideNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sharedPrefs.getBoolean("pref_track_calories", true))
            findViewById(R.id.caloriesContainer).setVisibility(View.GONE);
    }

    private int mAge;
    private int mHeight;
    private int mWeight;
    private String mGender;

    public Void setCaloriesNorm() {
        if (sharedPrefs.getString("pref_units", "1").equals("1"))
            ((TextView) findViewById(R.id.weightToGoLabel)).setText("lb to go");

        return null;
    }

    private int calculateDailyCaloriesNorm(int age, float height, float weight, String gender) {
        if (gender != null) {
            if (gender.equals("male")) {
                return (int) (65 + (6.2 * weight) + (12.7 * height) - (6.8 * age));
            } else if (gender.equals("female")) {
                return (int) (655 + (4.3 * weight) + (4.3 * height) - (4.7 * age));
            }
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
        if (mAge != 0 && mHeight != 0 && mWeight != 0 && !mGender.equals("")) {
            startActivity(new Intent(this, GoalDialog.class));
        } else {
            startActivity(new Intent(this, BodyParamsInput.class));
            Toast.makeText(this, "Please enter body params first", Toast.LENGTH_SHORT).show();
        }
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
