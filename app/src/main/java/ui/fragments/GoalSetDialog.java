package ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
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

    public interface OnGoalChanged {
        public void onGoalChanged();
    }

    // Use this instance of the interface to deliver action events
    OnGoalChanged mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mCallback = (OnGoalChanged) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement OnGoalChanged");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_goal, null);
        final EditText field = (EditText) view.findViewById(R.id.field);

        final SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bundle bundle = getArguments();
        final String key =
                bundle.getString("key");

        switch (key) {
            case "steps":
                String dailySteps =
                        sharedPrefs.getInt("daily_steps", -1) + "";
                if (!dailySteps.equals("-1")) {
                    field.setText(dailySteps);
                }
                sharedPrefs.edit()
                        .putBoolean("isSteps50notified", false)
                        .putBoolean("isSteps100notified", false).apply();
                break;
            case "duration":
                String duration =
                        sharedPrefs.getInt("weekly_duration", -1) + "";
                if (!duration.equals("-1")) {
                    field.setText(duration);
                }
                sharedPrefs.edit()
                        .putBoolean("isRun50notified", false)
                        .putBoolean("isRun100notified", false).apply();
                break;
            case "calories":
                String calories =
                        sharedPrefs.getInt("calories_norm", -1) + "";
                if (!calories.equals("-1")) {
                    field.setText(calories);
                }
                sharedPrefs.edit()
                        .putBoolean("isCal50notified", false)
                        .putBoolean("isCal100notified", false).apply();
                break;
        }

        String title = null;
        switch (key) {
            case "steps":
                title = "Daily step count goal";
                break;
            case "duration":
                title = "Weekly run duration, min";
                break;
            case "calories":
                title = "Daily calories goal";
                break;
            case "aerobic":
                title = "Daily aerobic duration, min";
                break;
        }

        Builder builder = new Builder(
                getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle(title)
                        // Add action buttons
                .setPositiveButton("Set", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        switch (key) {
                            case "steps":
                                if (!field.getText().toString().isEmpty()) {
                                    int newValue = Integer.parseInt(
                                            field.getText().toString());
                                    int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                                    sharedPrefs.edit()
                                            .putInt("daily_steps", newValue)
                                            .putInt("daily_steps_time", day + 1)
                                            .apply();
                                } else {
                                    sharedPrefs.edit()
                                            .putInt("daily_steps", -1)
                                            .putInt("daily_steps_time", -1)
                                            .apply();
                                }
                                break;
                            case "duration":
                                if (!field.getText().toString().isEmpty()) {
                                    int newValue = Integer.parseInt(
                                            field.getText().toString());
                                    int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                                    sharedPrefs.edit()
                                            .putInt("weekly_duration", newValue)
                                            .putInt("weekly_duration_time", day + 30)
                                            .apply();

                                    int durationGoal = sharedPrefs.getInt("weekly_duration", -1);
                                    long durationCurrent = sharedPrefs.getLong("mDuration", -1);

                                    if (durationCurrent >= durationGoal * 60) {
                                        sharedPrefs.edit().putInt("notify_run_100", 1).apply();
                                    } else if (durationCurrent >= durationGoal * 60 * 0.5) {
                                        sharedPrefs.edit().putInt("notify_run_50", 1).apply();
                                    }
                                } else {
                                    sharedPrefs.edit()
                                            .putInt("weekly_duration", -1)
                                            .putInt("weekly_duration_time", -1)
                                            .putInt("notify_run_50", 2)
                                            .putInt("notify_run_100", 2)
                                            .apply();
                                }
                                break;
                            case "calories":
                                if (!field.getText().toString().isEmpty()) {
                                    int newValue = Integer.parseInt(
                                            field.getText().toString());
                                    int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                                    sharedPrefs.edit()
                                            .putInt("calories_norm", newValue)
                                            .putInt("calories_norm_time", day + 1)
                                            .apply();
                                } else {
                                    sharedPrefs.edit()
                                            .putInt("calories_norm", -1)
                                            .putInt("calories_norm_time", -1)
                                            .apply();
                                }
                                break;
                            case "aerobic":
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                        .putBoolean("isGoalSet", true).apply();
                                startActivity(new Intent(getActivity(), AerobicActivity.class));
                                break;
                        }
                        mCallback.onGoalChanged();
                        GoalSetDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
