package ui.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import bellamica.tech.dreamfit.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
