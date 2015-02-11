package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import bellamica.tech.dreamteenfitness.R;

public class AerobicActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerobic);
    }

    public void navigateToCategory(View view) {
        Intent intent = new Intent(this, WorkoutActivity.class);
        switch (view.getId()) {
            case R.id.ab:
                intent.putExtra("category", "ab");
                break;
            case R.id.leg:
                intent.putExtra("category", "leg");
                break;
            case R.id.arm:
                intent.putExtra("category", "arm");
                break;
            case R.id.glutes:
                intent.putExtra("category", "butt");
                break;
        }
        startActivity(intent);
    }
}
