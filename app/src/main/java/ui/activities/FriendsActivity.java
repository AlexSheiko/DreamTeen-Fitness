package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.WebDialog;
import com.google.android.gms.plus.PlusShare;

import bellamica.tech.dreamteenfitness.R;


public class FriendsActivity extends Activity
        implements OnClickListener {

    private Button mShareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mShareButton = (Button) findViewById(R.id.share_button);
        mShareButton.setOnClickListener(this);
    }

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback =
            new Session.StatusCallback() {
                @Override
                public void call(Session session,
                                 SessionState state, Exception exception) {
                    onSessionStateChange(session);
                }
            };

    private void onSessionStateChange(Session session) {

        if (session.isOpened()) {
            Bundle params = new Bundle();
            params.putString("message", "I just smashed " + "3" +
                    " friends! Can you beat it?");
            showDialogWithoutNotificationBar("apprequests", params);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();

        Session.getActiveSession();
    }

    @Override
    public void onPause() {
        uiHelper.onPause();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private WebDialog dialog = null;

    private void showDialogWithoutNotificationBar(String action, Bundle params) {
        dialog = new WebDialog.Builder(this, Session.getActiveSession(), action, params).
                setOnCompleteListener(new WebDialog.OnCompleteListener() {
                    @Override
                    public void onComplete(Bundle values, FacebookException error) {
                        if (error != null) {
                            /* Toast.makeText(FriendsActivity.this, getResources()
                                    .getString(R.string.network_error), Toast.LENGTH_SHORT).show(); */
                        } else {
                            Toast.makeText(FriendsActivity.this, getString(R.string.request_sent_toast), Toast.LENGTH_SHORT).show();
                        }
                        dialog = null;
                        Session.getActiveSession().closeAndClearTokenInformation();
                    }
                }).build();

        Window dialog_window = dialog.getWindow();
        dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_button:
                // TODO: Configure string to use my credentials
                PlusShare.Builder builder = new PlusShare.Builder(this);

                // Set call-to-action metadata.
                builder.addCallToAction(
                        "CREATE_ITEM", /** call-to-action button label */
                        Uri.parse("http://plus.google.com/pages/create"), /** call-to-action url (for desktop use) */
                        "/pages/create" /** call to action deep-link ID (for mobile use), 512 characters or fewer */);

                // Set the content url (for desktop use).
                builder.setContentUrl(Uri.parse("https://plus.google.com/pages/"));

                // Set the target deep-link ID (for mobile use).
                builder.setContentDeepLinkId("/pages/",
                        null, null, null);

                // Set the share text.
                builder.setText("Create your Google+ Page too!");

                startActivityForResult(builder.getIntent(), 0);
                break;
        }
    }
}
