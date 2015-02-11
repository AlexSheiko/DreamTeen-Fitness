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

import java.util.Date;

import bellamica.tech.dreamteenfitness.R;

public class SetGoalsDialog extends DialogFragment {

    private EditText mValueField;

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
        View view = inflater.inflate(R.layout.dialog_set_goal, null);

        final SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Bundle bundle = getArguments();
        final String key = bundle.getString("key");
        mValueField = (EditText) view.findViewById(R.id.field);

        String dailySteps = mSharedPrefs.getInt("daily_steps", -1) + "";
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
                        if (!mValueField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    mValueField.getText().toString());
                            if (key.equals("steps")) {
                                mSharedPrefs.edit()
                                        .putInt("daily_steps", newValue)
                                        .putString("daily_steps_time", new Date().toString())
                                        .apply();
                            } else if (key.equals("duration")) {
                                mSharedPrefs.edit()
                                        .putInt("weekly_duration", newValue)
                                        .putString("weekly_duration_time", new Date().toString())
                                        .apply();
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
