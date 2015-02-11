package ui.fragments;

import android.app.Activity;
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

public class SetGoalsDialog extends DialogFragment {

    private EditText mDailyValueField;
    private EditText mWeeklyValueField;
    private EditText mMonthlyValueField;

    public interface OnChallengeValueChanged {
        public void onChallengeValueChanged();
    }

    // Use this instance of the interface to deliver action events
    OnChallengeValueChanged mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (OnChallengeValueChanged) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement OnChallengeValueChanged");
        }
    }

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
            mSharedPrefs.edit().putBoolean("isSteps50notified", false).apply();
            mSharedPrefs.edit().putBoolean("isSteps75notified", false).apply();
            mSharedPrefs.edit().putBoolean("isSteps100notified", false).apply();
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
            mSharedPrefs.edit().putBoolean("isRun50notified", false).apply();
            mSharedPrefs.edit().putBoolean("isRun75notified", false).apply();
            mSharedPrefs.edit().putBoolean("isRun100notified", false).apply();
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
                            mListener.onChallengeValueChanged();
                        }
                        if (!mWeeklyValueField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    mWeeklyValueField.getText().toString());
                            if (key.equals("steps")) {
                                mSharedPrefs.edit().putInt("weekly_steps", newValue).apply();
                            } else if (key.equals("duration")) {
                                mSharedPrefs.edit().putInt("weekly_duration", newValue).apply();
                            }
                            mListener.onChallengeValueChanged();
                        }
                        if (!mMonthlyValueField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    mMonthlyValueField.getText().toString());
                            if (key.equals("steps")) {
                                mSharedPrefs.edit().putInt("monthly_steps", newValue).apply();
                            } else if (key.equals("duration")) {
                                mSharedPrefs.edit().putInt("monthly_duration", newValue).apply();
                            }
                            mListener.onChallengeValueChanged();
                        }
                        SetGoalsDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
