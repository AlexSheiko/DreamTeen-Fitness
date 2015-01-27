package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;
import model.Exercise;

public class WorkoutActivity extends Activity {

    private CountDownTimer mTimer;
    private int mCurrentPosition = 0;
    private String mCategory;
    private static final int MAX_COUNT_EXERCISES = 3;

    @InjectView(R.id.startButton)
    Button mStartButton;
    @InjectView(R.id.finishButton)
    Button mFinishButton;
    @InjectView(R.id.pauseButton)
    ImageButton mPauseButton;
    @InjectView(R.id.durationCounter)
    TextView mDurationCounter;
    @InjectView(R.id.image)
    ImageView mImage;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.positionLabel)
    TextView mPositionLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        ButterKnife.inject(this);

        mCategory = getIntent().getStringExtra("category");
        updateExercise();

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
                mTimer = new CountDownTimer(10 * 1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        mDurationCounter.setText(
                                convertSecondsToMmSs(millisUntilFinished / 1000));
                    }

                    public void onFinish() {
                        mDurationCounter.setText("03:00");
                        if (mCurrentPosition < MAX_COUNT_EXERCISES) {
                            mCurrentPosition++;
                            updateExercise();
                            mTimer.start();
                        } else {
                            Toast.makeText(WorkoutActivity.this, "Workout saved",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(WorkoutActivity.this,
                                    MainActivity.class));
                        }
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

    private void updateExercise() {
        Exercise exercise = new Exercise(this, mCategory);
        mImage.setImageBitmap(
                BitmapFactory.decodeResource(getResources(),
                        getResources().getIdentifier(
                                mCategory + "_" + mCurrentPosition,
                                "drawable",
                                "bellamica.tech.dreamteenfitness")));

        mTitle.setText(exercise.getTitle(mCurrentPosition));
        int exercisePosition = mCurrentPosition + 1;
        mPositionLabel.setText("Exercise " + exercisePosition + "/8");
    }

    public void nextExercise(View view) {
        mCurrentPosition++;
        updateExercise();
    }

    public void previousExercise(View view) {
        if (mCurrentPosition != 0) {
            mCurrentPosition--;
            updateExercise();
        }
    }
}
