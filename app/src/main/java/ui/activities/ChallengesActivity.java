package ui.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ui.fragments.ChallengeGoalDialog;

public class ChallengesActivity extends Activity {

    @InjectView(R.id.stepsNotSetLabel)
    TextView mStepsNotSetLabel;
    @InjectView(R.id.durationNotSetLabel)
    TextView mDurationNotSetLabel;
    @InjectView(R.id.setStepsButton)
    Button mSetStepsButton;
    @InjectView(R.id.setDurationButton)
    Button mSetDurationButton;
    @InjectView(R.id.progressBarDailySteps)
    ProgressBar mProgressBarDailySteps;
    @InjectView(R.id.progressBarWeeklySteps)
    ProgressBar mProgressBarWeeklySteps;
    @InjectView(R.id.progressBarMonthlySteps)
    ProgressBar mProgressBarMonthlySteps;
    @InjectView(R.id.progressBarDailyDuration)
    ProgressBar mProgressBarDailyDuration;
    @InjectView(R.id.progressBarWeeklyDuration)
    ProgressBar mProgressBarWeeklyDuration;
    @InjectView(R.id.progressBarMonthlyDuration)
    ProgressBar mProgressBarMonthlyDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        ButterKnife.inject(this);
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        int dailySteps = sharedPrefs.getInt("daily_steps", -1);
        int weeklySteps = sharedPrefs.getInt("weekly_steps", -1);
        int monthlySteps = sharedPrefs.getInt("monthly_steps", -1);
        int dailyDuration = sharedPrefs.getInt("daily_duration", -1);
        int weeklyDuration = sharedPrefs.getInt("weekly_duration", -1);
        int monthlyDuration = sharedPrefs.getInt("monthly_duration", -1);

        boolean isStepsGoalSet = false;
        if (dailySteps != -1 || weeklySteps != -1 || monthlySteps != -1) {
            isStepsGoalSet = true;
            if (dailySteps != -1) {
                mProgressBarDailySteps.setVisibility(View.VISIBLE);
            }
            if (weeklySteps != -1) {
                mProgressBarWeeklySteps.setVisibility(View.VISIBLE);
            }
            if (monthlySteps != -1) {
                mProgressBarMonthlySteps.setVisibility(View.VISIBLE);
            }
        }
        boolean isDurationGoalSet = false;
        if (dailyDuration != -1 || weeklyDuration != -1 || monthlyDuration != -1) {
            isDurationGoalSet = true;
            if (dailyDuration != -1) {
                mProgressBarDailyDuration.setVisibility(View.VISIBLE);
            }
            if (weeklyDuration != -1) {
                mProgressBarWeeklyDuration.setVisibility(View.VISIBLE);
            }
            if (monthlyDuration != -1) {
                mProgressBarMonthlyDuration.setVisibility(View.VISIBLE);
            }
        }

        if (isStepsGoalSet) {
            mStepsNotSetLabel.setVisibility(View.GONE);
            mSetStepsButton.setVisibility(View.GONE);
        }
        if (isDurationGoalSet) {
            mDurationNotSetLabel.setVisibility(View.GONE);
            mSetDurationButton.setVisibility(View.GONE);
        }
    }

    public void addChallengeGoal(View view) {
        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.stepsContainer:
                bundle.putString("key", "steps");
                break;
            case R.id.durationContainer:
                bundle.putString("key", "duration");
                break;
        }
        DialogFragment newFragment = new ChallengeGoalDialog();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "dialog_challenge_goal");
    }
}
