package ui.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import bellamica.tech.dreamteenfitness.R;
import ui.fragments.ChallengeGoalDialog;

public class ChallengesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

    }

    public void addChallengeGoal(View view) {
        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.stepsContainer:
                bundle.putString("key", "steps");
                break;
            case R.id.durationContainer:
                bundle.putString("key", "duration");
                break;
        }
        DialogFragment newFragment = new ChallengeGoalDialog();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "dialog_challenge_goal");
    }
}
