package ui.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Rect;
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
import ui.fragments.ChallengeGoalDialog.OnChallengeValueChanged;

public class GoalsActivity extends Activity
        implements OnChallengeValueChanged {

    private SharedPreferences mSharedPrefs;

    private int mDailyStepsTaken;
    private int mWeeklyStepsTaken;
    private int mMonthlyStepsTaken;

    private long mDailyDuration;
    private long mWeeklyDuration;
    private long mMonthlyDuration;

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
        setContentView(R.layout.activity_goals);
        ButterKnife.inject(this);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateProgressBar();
    }

    private void updateProgressBar() {
        int dailySteps = mSharedPrefs.getInt("daily_steps", -1);
        int weeklySteps = mSharedPrefs.getInt("weekly_steps", -1);
        int monthlySteps = mSharedPrefs.getInt("monthly_steps", -1);
        int dailyDuration = mSharedPrefs.getInt("daily_duration", -1);
        int weeklyDuration = mSharedPrefs.getInt("weekly_duration", -1);
        int monthlyDuration = mSharedPrefs.getInt("monthly_duration", -1);

        boolean isStepsGoalSet = false;
        if (dailySteps != -1 || weeklySteps != -1 || monthlySteps != -1) {
            isStepsGoalSet = true;
            if (dailySteps != -1) {
                mProgressBarDailySteps.setVisibility(View.VISIBLE);
                mProgressBarDailySteps.setMax(dailySteps);
                mProgressBarDailySteps.setProgress(mDailyStepsTaken);
                if (mDailyStepsTaken >= dailySteps) {
                    Rect bounds = mProgressBarDailySteps.getProgressDrawable().getBounds();
                    mProgressBarDailySteps.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_calories_goal_reached));
                    mProgressBarDailySteps.getProgressDrawable().setBounds(bounds);
                } else {
                    Rect bounds = mProgressBarDailySteps.getProgressDrawable().getBounds();
                    mProgressBarDailySteps.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_daily_steps));
                    mProgressBarDailySteps.getProgressDrawable().setBounds(bounds);
                }
            }
            if (weeklySteps != -1) {
                mProgressBarWeeklySteps.setVisibility(View.VISIBLE);
                mProgressBarWeeklySteps.setMax(weeklySteps);
                mProgressBarWeeklySteps.setProgress(mWeeklyStepsTaken);
                if (mWeeklyStepsTaken >= weeklySteps) {
                    Rect bounds = mProgressBarWeeklySteps.getProgressDrawable().getBounds();
                    mProgressBarWeeklySteps.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_calories_goal_reached));
                    mProgressBarWeeklySteps.getProgressDrawable().setBounds(bounds);
                } else {
                    Rect bounds = mProgressBarWeeklySteps.getProgressDrawable().getBounds();
                    mProgressBarWeeklySteps.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_weekly_steps));
                    mProgressBarWeeklySteps.getProgressDrawable().setBounds(bounds);
                }
            }
            if (monthlySteps != -1) {
                mProgressBarMonthlySteps.setVisibility(View.VISIBLE);
                mProgressBarMonthlySteps.setMax(monthlySteps);
                mProgressBarMonthlySteps.setProgress(mMonthlyStepsTaken);
                if (mMonthlyStepsTaken >= monthlySteps) {
                    Rect bounds = mProgressBarMonthlySteps.getProgressDrawable().getBounds();
                    mProgressBarMonthlySteps.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_calories_goal_reached));
                    mProgressBarMonthlySteps.getProgressDrawable().setBounds(bounds);
                } else {
                    Rect bounds = mProgressBarMonthlySteps.getProgressDrawable().getBounds();
                    mProgressBarMonthlySteps.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_monthly_steps));
                    mProgressBarMonthlySteps.getProgressDrawable().setBounds(bounds);
                }
            }
        }
        boolean isDurationGoalSet = false;
        if (dailyDuration != -1 || weeklyDuration != -1 || monthlyDuration != -1) {
            isDurationGoalSet = true;
            if (dailyDuration != -1) {
                mProgressBarDailyDuration.setVisibility(View.VISIBLE);
                mProgressBarDailyDuration.setMax(dailyDuration * 60); // Minutes in seconds
                mProgressBarDailyDuration.setProgress((int) mDailyDuration);
                if (mDailyDuration >= dailyDuration * 60) {
                    Rect bounds = mProgressBarDailyDuration.getProgressDrawable().getBounds();
                    mProgressBarDailyDuration.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_calories_goal_reached));
                    mProgressBarDailyDuration.getProgressDrawable().setBounds(bounds);
                } else {
                    Rect bounds = mProgressBarDailyDuration.getProgressDrawable().getBounds();
                    mProgressBarDailyDuration.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_daily_duration));
                    mProgressBarDailyDuration.getProgressDrawable().setBounds(bounds);
                }
            }
            if (weeklyDuration != -1) {
                mProgressBarWeeklyDuration.setVisibility(View.VISIBLE);
                mProgressBarWeeklyDuration.setMax(weeklyDuration * 60);
                mProgressBarWeeklyDuration.setProgress((int) mWeeklyDuration);
                if (mWeeklyDuration >= weeklyDuration * 60) {
                    Rect bounds = mProgressBarWeeklyDuration.getProgressDrawable().getBounds();
                    mProgressBarWeeklyDuration.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_calories_goal_reached));
                    mProgressBarWeeklyDuration.getProgressDrawable().setBounds(bounds);

                } else {
                    Rect bounds = mProgressBarWeeklyDuration.getProgressDrawable().getBounds();
                    mProgressBarWeeklyDuration.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_weekly_duration));
                    mProgressBarWeeklyDuration.getProgressDrawable().setBounds(bounds);
                }
            }
            if (monthlyDuration != -1) {
                mProgressBarMonthlyDuration.setVisibility(View.VISIBLE);
                mProgressBarMonthlyDuration.setMax(monthlyDuration * 60);
                mProgressBarMonthlyDuration.setProgress((int) mMonthlyDuration);
                if (mMonthlyDuration >= monthlyDuration * 60) {
                    Rect bounds = mProgressBarMonthlyDuration.getProgressDrawable().getBounds();
                    mProgressBarMonthlyDuration.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_calories_goal_reached));
                    mProgressBarMonthlyDuration.getProgressDrawable().setBounds(bounds);
                } else {
                    Rect bounds = mProgressBarMonthlyDuration.getProgressDrawable().getBounds();
                    mProgressBarMonthlyDuration.setProgressDrawable(
                            getResources().getDrawable(R.drawable.progress_bar_monthly_duration));
                    mProgressBarMonthlyDuration.getProgressDrawable().setBounds(bounds);
                }
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

        int id = view.getId();
        if (id == R.id.stepsContainer || id == R.id.setStepsButton) {
            bundle.putString("key", "steps");
        } else if (id == R.id.durationContainer || id == R.id.setDurationButton) {
            bundle.putString("key", "duration");
        }
        DialogFragment newFragment = new ChallengeGoalDialog();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "dialog_challenge_goal");
    }

    @Override
    public void onChallengeValueChanged() {
        updateProgressBar();
    }
}
