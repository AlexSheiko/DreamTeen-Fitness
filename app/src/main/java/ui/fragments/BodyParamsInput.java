package ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import bellamica.tech.dreamteenfitness.R;
import ui.activities.MainActivity;

public class BodyParamsInput extends Activity {

    private Spinner mHeightSpinner;
    private Spinner mWeightSpinner;

    private String mGender = "male";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_params);

        initializeSpinners();
    }

    private void initializeSpinners() {
        mHeightSpinner = (Spinner) findViewById(R.id.heightSpinner);
        ArrayAdapter<CharSequence> mHeightAdapter = ArrayAdapter.createFromResource(this,
                R.array.height_spinner_units, android.R.layout.simple_spinner_item);
        mHeightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mHeightSpinner.setAdapter(mHeightAdapter);

        mWeightSpinner = (Spinner) findViewById(R.id.weightSpinner);
        ArrayAdapter<CharSequence> mWeightAdapter = ArrayAdapter.createFromResource(this,
                R.array.weight_spinner_values, android.R.layout.simple_spinner_item);
        mWeightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWeightSpinner.setAdapter(mWeightAdapter);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_male:
                if (checked)
                    mGender = "male";
                break;
            case R.id.radio_female:
                if (checked)
                    mGender = "female";
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds items to the action bar
        getMenuInflater().inflate(R.menu.menu_body_params, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle skip button click
        Intent intent = new Intent(BodyParamsInput.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return true;
    }

    private int mAge = -1;
    private int mHeight;
    private int mWeight;

    public void saveData(View view) {
        String mDeviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        mAge = Integer.parseInt(((EditText) findViewById(R.id.ageField)).getText().toString());
        mHeight = Integer.parseInt(((EditText) findViewById(R.id.heightField)).getText().toString());
        mWeight = Integer.parseInt(((EditText) findViewById(R.id.weightField)).getText().toString());

        if (mHeightSpinner.getSelectedItem().toString().equals("cm"))
            mHeight = (int) (mHeight * 0.394);

        if (mWeightSpinner.getSelectedItem().toString().equals("kg"))
            mWeight = (int) (mWeight * 2.205);
    }

    private void navigateToMainScreen() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}