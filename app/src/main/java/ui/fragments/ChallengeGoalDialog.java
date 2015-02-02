package ui.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import bellamica.tech.dreamteenfitness.R;

public class ChallengeGoalDialog extends DialogFragment {

    private EditText mDailyValueField;
    private EditText mWeeklyValueField;
    private EditText mMonthlyValueField;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_challenge_goal, null);

        final SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Bundle bundle = getArguments();
        final String key = bundle.getString("key");
        mDailyValueField = (EditText) view.findViewById(R.id.dailyField);
        mWeeklyValueField = (EditText) view.findViewById(R.id.weeklyField);
        mMonthlyValueField = (EditText) view.findViewById(R.id.monthlyField);

        String dailySteps = mSharedPrefs.getInt("daily_steps", -1) + "";
        String weeklySteps = mSharedPrefs.getInt("weekly_steps", -1) + "";
        String monthlySteps = mSharedPrefs.getInt("monthly_steps", -1) + "";
        String dailyDuration = mSharedPrefs.getInt("daily_duration", -1) + "";
        String weeklyDuration = mSharedPrefs.getInt("weekly_duration", -1) + "";
        String monthlyDuration = mSharedPrefs.getInt("monthly_duration", -1) + "";

        if (key.equals("steps")) {
            if (!dailySteps.equals("-1")) {
                mDailyValueField.setText(dailySteps);
            }
            if (!weeklySteps.equals("-1")) {
                mWeeklyValueField.setText(weeklySteps);
            }
            if (!monthlySteps.equals("-1")) {
                mMonthlyValueField.setText(monthlySteps);
            }
        } else if (key.equals("duration")) {
            if (!dailyDuration.equals("-1")) {
                mDailyValueField.setText(dailyDuration);
            }
            if (!weeklyDuration.equals("-1")) {
                mWeeklyValueField.setText(weeklyDuration);
            }
            if (!monthlyDuration.equals("-1")) {
                mMonthlyValueField.setText(monthlyDuration);
            }
        }

        String title = null;
        if (key.equals("steps")) {
            title = "Step count, goal";
        } else if (key.equals("duration")) {
            title = "Run duration, min";
        }
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle(title)
                        // Add action buttons
                .setPositiveButton("Set", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!mDailyValueField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    mDailyValueField.getText().toString());
                            if (key.equals("steps")) {
                                mSharedPrefs.edit().putInt("daily_steps", newValue).apply();
                            } else if (key.equals("duration")) {
                                mSharedPrefs.edit().putInt("daily_duration", newValue).apply();
                            }
                        }
                        if (!mWeeklyValueField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    mWeeklyValueField.getText().toString());
                            if (key.equals("steps")) {
                                mSharedPrefs.edit().putInt("weekly_steps", newValue).apply();
                            } else if (key.equals("duration")) {
                                mSharedPrefs.edit().putInt("weekly_duration", newValue).apply();
                            }
                        }
                        if (!mMonthlyValueField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    mMonthlyValueField.getText().toString());
                            if (key.equals("steps")) {
                                mSharedPrefs.edit().putInt("monthly_steps", newValue).apply();
                            } else if (key.equals("duration")) {
                                mSharedPrefs.edit().putInt("monthly_duration", newValue).apply();
                            }
                        }
                        ChallengeGoalDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
