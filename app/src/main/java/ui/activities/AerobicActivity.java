package ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import bellamica.tech.dreamteenfitness.R;

public class AerobicActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerobic);
    }

    public void navigateToCategory(View view) {
        Toast.makeText(this, view.getId() + "", Toast.LENGTH_SHORT).show();
        switch (view.getId()) {
            case R.id.ab:

                break;
            case R.id.leg:

                break;
            case R.id.arm:

                break;

            case R.id.butt:

                break;
        }
    }
}
