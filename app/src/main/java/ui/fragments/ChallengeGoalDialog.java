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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_challenge_goal, null);

        final SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Bundle bundle = getArguments();
        final String key = bundle.getString("key");
        final EditText mDailyValueField = (EditText) view.findViewById(R.id.dailyField);
        final EditText mWeeklyValueField = (EditText) view.findViewById(R.id.weeklyField);
        final EditText mMonthlyValueField = (EditText) view.findViewById(R.id.monthlyField);

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
