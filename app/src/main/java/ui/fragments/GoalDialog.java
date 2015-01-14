package ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import bellamica.tech.dreamteenfitness.R;
import ui.activities.MainActivity;

public class GoalDialog extends Activity {

    private Spinner mActivityTypeSpinner;
    private Spinner mGoalWeightSpinner;
    private Spinner mGoalDateSpinner;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_goal);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

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
    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
