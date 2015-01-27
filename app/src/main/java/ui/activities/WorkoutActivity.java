package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class WorkoutActivity extends Activity {

    private CountDownTimer mTimer;

    @InjectView(R.id.startButton)
    Button mStartButton;
    @InjectView(R.id.finishButton)
    Button mFinishButton;
    @InjectView(R.id.pauseButton)
    ImageButton mPauseButton;
    @InjectView(R.id.durationCounter)
    TextView mDurationCounter;

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
                mDurationCounter.setTextColor(
                        getResources().getColor(R.color.time_counter_dark));

                if (getActionBar() != null) {
                    getActionBar().hide();
                }
                mTimer = new CountDownTimer(5 * 60 * 1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        mDurationCounter.setText(
                                convertSecondsToMmSs(millisUntilFinished / 1000));
                    }

                    public void onFinish() {
                        // TODO: Show new exercise
                    }
                }.start();
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
                mDurationCounter.setTextColor(
                        getResources().getColor(R.color.time_counter_light));

                mTimer.cancel();
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

    private String convertSecondsToMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        return String.format("%02d:%02d", m, s);
    }
}
