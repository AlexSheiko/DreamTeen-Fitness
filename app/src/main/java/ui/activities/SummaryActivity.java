package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class SummaryActivity extends Activity {

    private float mDistance;
    private int mStepCount;

    private SharedPreferences mSharedPrefs;

    @InjectView(R.id.unitsLabel)
    TextView mUnitsLabel;
    @InjectView(R.id.distanceLabel)
    TextView mDistanceLabel;
    @InjectView(R.id.nameField)
    EditText mNameField;
    @InjectView(R.id.discardButton)
    TextView mDiscardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        ButterKnife.inject(this);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (mSharedPrefs.getString("pref_units", "1").equals("1")) {
            mUnitsLabel.setText("miles");
        } else {
            mUnitsLabel.setText("km");
        }
        // Get run info
        mDistance = mSharedPrefs.getFloat("Distance", 0);
        mStepCount = (int) (mDistance * 2000);

        // Update UI with run info
        mDistanceLabel.setText(String.format("%.2f", mDistance));
        mNameField.setHint("Run on " +
                mSharedPrefs.getString("start_time", ""));

        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SummaryActivity.super.onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_summary, menu);
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();
        if (shareActionProvider != null)
            shareActionProvider.setShareIntent(shareIntent());

        return true;
    }

    private Intent shareIntent() {
        String duration = mSharedPrefs.getString("Duration", "00:00");

        String measureUnits;
        if (mSharedPrefs.getString("pref_units", "1").equals("1")) {
            measureUnits = "miles";
        } else {
            measureUnits = "km";
        }

        String message = "Just finished a run of " + String.format("%.2f", mDistance) + " " + measureUnits + " " +
                "in " + duration + ".\n\nAny challengers? ;)\n#DreamTeen Fitness";

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        return intent;
    }
}
