package ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.Calendar;

import bellamica.tech.dreamteenfitness.R;
import ui.activities.AerobicActivity;

public class GoalSetDialog extends DialogFragment {

    private Context mContext;

    private EditText mValueField;

    public interface OnGoalChanged {
        public void onValueChanged();
    }

    // Use this instance of the interface to deliver action events
    OnGoalChanged mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (OnGoalChanged) activity;
        } catch (ClassCastException ignored) {
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_goal, null);

        final SharedPreferences mSharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        Bundle bundle = getArguments();
        final String key = bundle.getString("key");
        mValueField = (EditText) view.findViewById(R.id.field);

        final String dailySteps = mSharedPrefs.getInt("daily_steps", -1) + "";
        String weeklyDuration = mSharedPrefs.getInt("weekly_duration", -1) + "";

        if (key.equals("steps")) {
            if (!dailySteps.equals("-1")) {
                mValueField.setText(dailySteps);
            }
            mSharedPrefs.edit().putBoolean("isSteps50notified", false).apply();
            mSharedPrefs.edit().putBoolean("isSteps75notified", false).apply();
            mSharedPrefs.edit().putBoolean("isSteps100notified", false).apply();
        } else if (key.equals("duration")) {
            if (!weeklyDuration.equals("-1")) {
                mValueField.setText(weeklyDuration);
            }
            mSharedPrefs.edit().putBoolean("isRun50notified", false).apply();
            mSharedPrefs.edit().putBoolean("isRun75notified", false).apply();
            mSharedPrefs.edit().putBoolean("isRun100notified", false).apply();
        }

        String title = null;
        if (key.equals("steps")) {
            title = "Daily step count goal";
        } else if (key.equals("duration")) {
            title = "Weekly run duration, min";
        } else if (key.equals("aerobic")) {
            title = "Daily aerobic duration, min";
        }
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle(title)
                        // Add action buttons
                .setPositiveButton("Set", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        switch (key) {
                            case "steps":
                                if (!mValueField.getText().toString().isEmpty()) {
                                    int newValue = Integer.parseInt(
                                            mValueField.getText().toString());
                                    int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                                    mSharedPrefs.edit()
                                            .putInt("daily_steps", newValue)
                                            .putInt("daily_steps_time", day + 1)
                                            .apply();
                                } else {
                                    mSharedPrefs.edit()
                                            .putInt("daily_steps", -1)
                                            .putInt("daily_steps_time", -1)
                                            .apply();
                                }
                                break;
                            case "duration":
                                if (!mValueField.getText().toString().isEmpty()) {
                                    int newValue = Integer.parseInt(
                                            mValueField.getText().toString());
                                    int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                                    mSharedPrefs.edit()
                                            .putInt("weekly_duration", newValue)
                                            .putInt("weekly_duration_time", day + 30)
                                            .apply();
                                } else {
                                    mSharedPrefs.edit()
                                            .putInt("weekly_duration", -1)
                                            .putInt("weekly_duration_time", -1)
                                            .apply();
                                }
                                break;
                            case "aerobic":
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                        .putBoolean("isGoalSet", true).apply();
                                startActivity(new Intent(getActivity(), AerobicActivity.class));
                                break;
                        }
                        if (mListener != null) {
                            mListener.onValueChanged();
                        }
                        GoalSetDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
