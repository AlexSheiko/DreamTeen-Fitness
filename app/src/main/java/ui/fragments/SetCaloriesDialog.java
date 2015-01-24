package ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import bellamica.tech.dreamteenfitness.R;

public class SetCaloriesDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface CaloriesDialogListener {
        public void onDailyCaloriesNormChanged(DialogFragment dialog, int newValue);
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

    private EditText mCaloriesField;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View mView = inflater.inflate(R.layout.dialog_calories, null);
        mCaloriesField = (EditText) mView.findViewById(R.id.caloriesField);
        mCaloriesField.setHint(
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getInt("calories_norm", 2000) + "");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(mView)
                .setTitle("Daily norm, calories")
                        // Add action buttons
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        if (!mCaloriesField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    mCaloriesField.getText().toString());
                            mListener.onDailyCaloriesNormChanged(SetCaloriesDialog.this, newValue);
                        }
                        SetCaloriesDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
