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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import bellamica.tech.dreamteenfitness.R;
import ui.activities.AerobicActivity;

public class AerobicGoalDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_aerobic_goal, null);
        final SharedPreferences mSharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        ArrayAdapter<CharSequence> mTypeSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.goal_type_values, R.layout.spinner_item);
        mTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner mGoalTypeSpinner = (Spinner) view.findViewById(R.id.goalTypeSpinner);
        mGoalTypeSpinner.setAdapter(mTypeSpinnerAdapter);

        final EditText valueField = (EditText) view.findViewById(R.id.valueFieldDay);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle("Daily goal")
                        // Add action buttons
                .setPositiveButton("Set", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        if (!valueField.getText().toString().isEmpty()) {
                            int newValue = Integer.parseInt(
                                    valueField.getText().toString());

                            switch (mGoalTypeSpinner.getSelectedItemPosition()) {
                                case 0:
                                    mSharedPrefs.edit().putInt("duration_target", newValue).apply();
                                    break;
                                case 1:
                                    mSharedPrefs.edit().putInt("calories_norm", newValue).apply();
                                    break;
                            }
                            mSharedPrefs.edit().putBoolean("isGoalSet", true).apply();
                        }
                        startActivity(new Intent(getActivity(), AerobicActivity.class));
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
