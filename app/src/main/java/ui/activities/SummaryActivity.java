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

import bellamica.tech.dreamfit.R;

public class SummaryActivity extends Activity {

    private String mDistance;
    private String mDuration;
    private String mDateTime;

    private EditText tripNameField;
    private SharedPreferences sharedPrefs;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary);
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
