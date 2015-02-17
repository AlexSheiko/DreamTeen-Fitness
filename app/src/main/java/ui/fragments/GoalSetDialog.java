package ui.fragments;

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
import butterknife.ButterKnife;
import butterknife.InjectView;
import ui.activities.AerobicActivity;

public class GoalSetDialog extends DialogFragment {

    private View mView;

    @InjectView(R.id.field)
    EditText mField;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mView = view;

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        inflater.inflate(R.layout.dialog_set_goal, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

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
                    mField.setText(dailySteps);
                }
                sharedPrefs.edit()
                        .putBoolean("isSteps50notified", false)
                        .putBoolean("isSteps100notified", false).apply();
                break;
            case "duration":
                String duration =
                        sharedPrefs.getInt("weekly_duration", -1) + "";
                if (!duration.equals("-1")) {
                    mField.setText(duration);
                }
                sharedPrefs.edit()
                        .putBoolean("isRun50notified", false)
                        .putBoolean("isRun100notified", false).apply();
                break;
            case "calories":
                String calories =
                        sharedPrefs.getInt("calories_norm", -1) + "";
                if (!calories.equals("-1")) {
                    mField.setText(calories);
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
        builder.setView(mView)
                .setTitle(title)
                        // Add action buttons
                .setPositiveButton("Set", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        switch (key) {
                            case "steps":
                                if (!mField.getText().toString().isEmpty()) {
                                    int newValue = Integer.parseInt(
                                            mField.getText().toString());
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
                                if (!mField.getText().toString().isEmpty()) {
                                    int newValue = Integer.parseInt(
                                            mField.getText().toString());
                                    int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                                    sharedPrefs.edit()
                                            .putInt("weekly_duration", newValue)
                                            .putInt("weekly_duration_time", day + 30)
                                            .apply();
                                } else {
                                    sharedPrefs.edit()
                                            .putInt("weekly_duration", -1)
                                            .putInt("weekly_duration_time", -1)
                                            .apply();
                                }
                                break;
                            case "calories":
                                if (!mField.getText().toString().isEmpty()) {
                                    int newValue = Integer.parseInt(
                                            mField.getText().toString());
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
                        GoalSetDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
