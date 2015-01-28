package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Score;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPublishListener;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class SummaryActivity extends Activity {

    private static final String TAG = SummaryActivity.class.getSimpleName();

    private float mDistance;
    private int mStepCount;
    private SimpleFacebook mSimpleFacebook;

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

        if (getIntent() != null) {
            mStepCount = getIntent().getIntExtra("step_count", 0);
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void saveRun(View view) {
        mSharedPrefs.edit().putFloat("Distance", 0).apply();

        mSimpleFacebook = SimpleFacebook.getInstance(this);
        mSimpleFacebook.login(onLoginListener);

        startActivity(new Intent(SummaryActivity.this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    OnLoginListener onLoginListener = new OnLoginListener() {
        @Override
        public void onLogin() {
            Score score = new Score.Builder()
                    .setScore(mStepCount)
                    .build();
            mSimpleFacebook.publish(score, onPublishListener);
        }

        @Override
        public void onNotAcceptingPermissions(Permission.Type type) {
            // user didn't accept READ or WRITE permission
            Log.w(TAG, String.format("You didn't accept %s permissions", type.name()));
        }

        @Override
        public void onException(Throwable throwable) {
            Log.e(TAG, throwable.getMessage());
        }

        @Override
        public void onFail(String s) {
            Log.e(TAG, s);
        }

        @Override
        public void onThinking() {
            Log.i(TAG, "Thinking...");
        }
    };

    OnPublishListener onPublishListener = new OnPublishListener() {
        @Override
        public void onComplete(String postId) {
            Log.i(TAG, "Published successfully");
        }

        @Override
        public void onException(Throwable throwable) {
            super.onException(throwable);
            Log.e(TAG, throwable.getMessage());
        }

        @Override
        public void onFail(String reason) {
            super.onFail(reason);
            Log.e(TAG, "Failed to post score: " + reason);
        }

        @Override
        public void onThinking() {
            super.onThinking();
        }
    };

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
