package ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SendCallback;

import bellamica.tech.dreamteenfitness.R;

public class SendMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_send_message);

        if (getIntent() == null
                || getIntent().getStringExtra(Intent.EXTRA_EMAIL) == null) {
            Toast.makeText(this, "Can't find email for specified friend", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final String email = getIntent().getStringExtra(Intent.EXTRA_EMAIL);

        final EditText editText = (EditText) findViewById(R.id.message);
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                String message = editText.getText().toString();

                if (message.isEmpty()) {
                    Toast.makeText(SendMessageActivity.this, "Message cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                sendPush(email, message);
                return true;
            }
        });
    }

    private void sendPush(final String email, final String message) {
        setProgressBarIndeterminateVisibility(true);
        ParsePush push = new ParsePush();
        push.setChannel(email);
        push.setMessage(message);
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.w(SendMessageActivity.class.getSimpleName(),
                            "Can't send push message. Cause: " + e.getMessage());
                    login(email, message);
                } else {
                    Toast.makeText(SendMessageActivity.this,
                            "Message sent", Toast.LENGTH_SHORT).show();
                    setProgressBarIndeterminateVisibility(false);
                    finish();
                }
            }
        });
    }

    private void login(final String email, final String message) {
        String username =
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("email", "");

        ParseUser.logInInBackground(username, "123", new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    sendPush(email, message);
                }
            }
        });
    }
}
