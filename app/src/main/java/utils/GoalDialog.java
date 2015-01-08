package utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import bellamica.tech.dreamfit.R;
import ui.MainActivity;

public class GoalDialog extends Activity {

    private Spinner mActivityTypeSpinner;
    private Spinner mGoalWeightSpinner;
    private Spinner mGoalDateSpinner;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goal);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Parse.initialize(GoalDialog.this,
                "ygyWodAYEDqiQ795p1V4Jxs2yRm9KTiBKsGSnakD",
                "IGTbu2n4KePoggvgXmBUS4k6cg5wQH8lQOA3Uo3k");

        initializeSpinners();
    }

    private void initializeSpinners() {
        mActivityTypeSpinner = (Spinner) findViewById(R.id.activityTypeSpinner);
        ArrayAdapter<CharSequence> mActivityTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.activity_type_spinner_values, android.R.layout.simple_spinner_item);

        mActivityTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mActivityTypeSpinner.setAdapter(mActivityTypeAdapter);

        mGoalWeightSpinner = (Spinner) findViewById(R.id.goalWeightSpinner);
        ArrayAdapter<CharSequence> mGoalWeightAdapter = ArrayAdapter.createFromResource(this,
                R.array.goal_weight_values, android.R.layout.simple_spinner_item);
        mGoalWeightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGoalWeightSpinner.setAdapter(mGoalWeightAdapter);

        mGoalDateSpinner = (Spinner) findViewById(R.id.goalDateSpinner);
        ArrayAdapter<CharSequence> mGoalDateAdapter = ArrayAdapter.createFromResource(this,
                R.array.goal_date_values, android.R.layout.simple_spinner_item);
        mGoalDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGoalDateSpinner.setAdapter(mGoalDateAdapter);
    }

    public void setGoal(View view) {
        String mDeviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        // Update user's current weight to desired
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PhoneReg");
        query.whereEqualTo("deviceId", mDeviceId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    int mCurrentWeight = parseObject.getInt("weight");

                    int mDesiredDelta = -1;
                    if (sharedPrefs.getString("pref_units", "1").equals("1")) {
                        mDesiredDelta = (int) (Integer.parseInt(
                                mGoalWeightSpinner.getSelectedItem().toString().replace(" kg", "")) * 2.205);
                    } else {
                        mDesiredDelta = (Integer.parseInt(
                                mGoalWeightSpinner.getSelectedItem().toString().replace(" kg", "")));
                    }

                    if (mActivityTypeSpinner.getSelectedItem().toString().equals("Lose")) {
                        parseObject.put("weight", mCurrentWeight - mDesiredDelta);
                    } else if (mActivityTypeSpinner.getSelectedItem().toString().equals("Gain")) {
                        parseObject.put("weight", mCurrentWeight + mDesiredDelta);
                    }
                    parseObject.put("weightDelta", mDesiredDelta);

                    int daysLeft = -1;
                    switch (mGoalDateSpinner.getSelectedItemPosition()) {
                        case 0: daysLeft = 14; break;
                        case 1: daysLeft = 30; break;
                        case 2: daysLeft = 60; break;
                        case 3: daysLeft = 90; break;
                        case 4: daysLeft = 180; break;
                        case 5: daysLeft = 365; break;
                    }
                    parseObject.put("daysLeft", daysLeft);

                    parseObject.put("isGoalSet", true);
                    parseObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                navigateToMainScreen();
                            }
                        }
                    });
                }
            }
        });
    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
