package ui;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.google.android.gms.plus.PlusShare;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.listeners.OnLoginListener;

import bellamica.tech.dreamteenfitness.R;


public class FriendsInviteFragment extends Fragment implements OnClickListener {

    protected static final String TAG = FriendsInviteFragment.class.getSimpleName();

    private SimpleFacebook mSimpleFacebook;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_invite, parent, false);

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getString(R.string.facebook_app_id))
                .setNamespace("dreamteen_fitness")
                .build();
        SimpleFacebook.setConfiguration(configuration);

        Button shareButton = (Button) rootView.findViewById(R.id.share_button);
        shareButton.setOnClickListener(this);

        Button facebookButton = (Button) rootView.findViewById(R.id.login_button);
        facebookButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Session.getActiveSession() == null || !Session.getActiveSession().isOpened()) {
                    mSimpleFacebook.login(onLoginListener);
                } else {
                    Bundle params = new Bundle();
                    params.putString("message", "DreamTeen Fitness is pretty cool. Check it out on Google Play. See if you can beat my score! http://goo.gl/YdMFVk");
                    showDialogWithoutNotificationBar("apprequests", params);
                }
            }
        });

        return rootView;
    }

    OnLoginListener onLoginListener = new OnLoginListener() {
        @Override
        public void onLogin() {
            // change the state of the button or do whatever you want
            Log.i(TAG, "Logged in");

            Bundle params = new Bundle();
            params.putString("message", "DreamTeen Fitness is pretty cool. Check it out on Google Play. See if you can beat my score! http://goo.gl/YdMFVk");
            showDialogWithoutNotificationBar("apprequests", params);
        }

        @Override
        public void onNotAcceptingPermissions(Permission.Type type) {
            // user didn't accept READ or WRITE permission
            Log.w(TAG, String.format("You didn't accept %s permissions", type.name()));
        }

        @Override
        public void onThinking() {
        }

        @Override
        public void onException(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onFail(String s) {
            Log.e(TAG, "Failed to authenticate. Cause: " + s);
        }

    /*
     * You can override other methods here:
     * onThinking(), onFail(String reason), onException(Throwable throwable)
     */
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            mSimpleFacebook.onActivityResult(getActivity(), requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        } finally {
            mSimpleFacebook.login(onLoginListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(getActivity());
    }

    private WebDialog dialog = null;

    private void showDialogWithoutNotificationBar(String action, Bundle params) {
        dialog = new WebDialog.Builder(getActivity(), Session.getActiveSession(), action, params).
                setOnCompleteListener(new WebDialog.OnCompleteListener() {
                    @Override
                    public void onComplete(Bundle values, FacebookException error) {
                        if (error != null) {
                            /* Toast.makeText(FriendsInviteFragment.this, getResources()
                                    .getString(R.string.network_error), Toast.LENGTH_SHORT).show(); */
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.request_sent_toast), Toast.LENGTH_SHORT).show();
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
                PlusShare.Builder builder = new PlusShare.Builder(getActivity());

                // Set call-to-action metadata.
                builder.addCallToAction(
                        "INSTALL_APP", /** call-to-action button label */
                        Uri.parse("https://play.google.com/store/apps/details?id=bellamica.tech.dreamteenfitness"),
                        /** call-to-action url (for desktop use) */
                        "https://play.google.com/store/apps/details?id=bellamica.tech.dreamteenfitness"
                        /** call to action deep-link ID (for mobile use), 512 characters or fewer */);

                // Set the content url (for desktop use).
                builder.setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=bellamica.tech.dreamteenfitness"));

                // Set the target deep-link ID (for mobile use).
                builder.setContentDeepLinkId("https://play.google.com/store/apps/details?id=bellamica.tech.dreamteenfitness",
                        null, null, null);

                // Set the share text.
                builder.setText("Install DreamTeen Fitness and challenge me!");

                startActivityForResult(builder.getIntent(), 0);
                break;
        }
    }
}
