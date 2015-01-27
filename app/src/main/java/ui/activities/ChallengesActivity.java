package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Score;
import com.sromku.simple.fb.listeners.OnScoresListener;

import java.util.List;

import bellamica.tech.dreamteenfitness.R;

public class ChallengesActivity extends Activity {

    private static final String TAG = ChallengesActivity.class.getSimpleName();

    private SimpleFacebook mSimpleFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
    }

    OnScoresListener onScoresListener = new OnScoresListener() {
        @Override
        public void onComplete(List<Score> scores) {
            Log.i(TAG, "Number of scores = " + scores.size());
        }

    /*
     * You can override other methods here:
     * onThinking(), onFail(String reason), onException(Throwable throwable)
     */
    };

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
        mSimpleFacebook.getScores(onScoresListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
