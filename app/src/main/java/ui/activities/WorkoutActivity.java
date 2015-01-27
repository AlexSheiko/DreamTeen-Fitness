package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class WorkoutActivity extends Activity {

    @InjectView(R.id.startButton)
    Button mStartButton;
    @InjectView(R.id.finishButton)
    Button mFinishButton;
    @InjectView(R.id.pauseButton)
    ImageButton mPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        ButterKnife.inject(this);

        mStartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartButton.setVisibility(View.GONE);
                mPauseButton.setVisibility(View.VISIBLE);
                mFinishButton.setVisibility(View.VISIBLE);

                if (getActionBar() != null) {
                    getActionBar().hide();
                }
                // startUiCountdown();
            }
        });

        mPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActionBar() != null) {
                    getActionBar().show();
                }
                mStartButton.setVisibility(View.VISIBLE);
                mPauseButton.setVisibility(View.GONE);
                mFinishButton.setVisibility(View.GONE);

                // stopUiCountdown();
            }
        });

        mFinishButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(WorkoutActivity.this, "Workout saved",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(WorkoutActivity.this,
                        MainActivity.class));
            }
        });
    }
}
