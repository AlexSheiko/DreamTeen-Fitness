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

public class MainGoalsDialog extends DialogFragment {

    private static final int CALORIES_DEFAULT = 2150;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface CaloriesDialogListener {
        public void onCaloriesGoalChanged(DialogFragment dialog, int newValue);
        public void onStepsGoalChanged(DialogFragment dialog, int newValue);
    }

    // Use this instance of the interface to deliver action events
    CaloriesDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (CaloriesDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement CaloriesDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_run_goal, null);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Bundle bundle = getArguments();
        final String key = bundle.getString("key");
        final EditText mValueField = (EditText) view.findViewById(R.id.valueFieldDay);
        if (key.equals("calories")) {
            mValueField.setHint(sharedPrefs.getInt("calories_norm", CALORIES_DEFAULT) + "");
        } else if (key.equals("steps")) {
            int stepsDefault = Integer.parseInt(getResources().getString(R.string.steps_target_default_value));
            mValueField.setHint(sharedPrefs.getInt("daily_steps", stepsDefault) + "");
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle("Daily goal, " + key)
                        // Add action buttons
                .setPositiveButton("Set", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        if (!mValueField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    mValueField.getText().toString());
                            if (key.equals("calories")) {
                                mListener.onCaloriesGoalChanged(MainGoalsDialog.this, newValue);
                            } else if (key.equals("steps")) {
                                mListener.onStepsGoalChanged(MainGoalsDialog.this, newValue);
                            }
                        }
                        MainGoalsDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
