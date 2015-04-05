package ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import bellamica.tech.dreamteenfitness.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
