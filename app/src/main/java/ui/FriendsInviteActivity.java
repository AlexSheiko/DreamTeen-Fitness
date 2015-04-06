package ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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

import java.util.ArrayList;
import java.util.HashSet;

import bellamica.tech.dreamteenfitness.R;


public class FriendsInviteActivity extends Activity implements OnClickListener {

    protected static final String TAG = FriendsInviteActivity.class.getSimpleName();

    private ArrayList<String> mEmails;
    private String[] mRecipients;

    private SimpleFacebook mSimpleFacebook;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friends_invite, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_my_friends) {
            startActivity(new Intent(this, FriendsListActivity.class));
            return true;
        } else if (id == R.id.action_search) {
            startActivity(new Intent(this, UserSearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_invite);

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getString(R.string.facebook_app_id))
                .setNamespace("dreamteen_fitness")
                .build();
        SimpleFacebook.setConfiguration(configuration);

        Button shareButton = (Button) findViewById(R.id.share_button);
        shareButton.setOnClickListener(this);

        Button facebookButton = (Button) findViewById(R.id.login_button);
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
            mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
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
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    private WebDialog dialog = null;

    private void showDialogWithoutNotificationBar(String action, Bundle params) {
        dialog = new WebDialog.Builder(this, Session.getActiveSession(), action, params).
                setOnCompleteListener(new WebDialog.OnCompleteListener() {
                    @Override
                    public void onComplete(Bundle values, FacebookException error) {
                        if (error != null) {
                            /* Toast.makeText(FriendsInviteActivity.this, getResources()
                                    .getString(R.string.network_error), Toast.LENGTH_SHORT).show(); */
                        } else {
                            Toast.makeText(FriendsInviteActivity.this, getString(R.string.request_sent_toast), Toast.LENGTH_SHORT).show();
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
                PlusShare.Builder builder = new PlusShare.Builder(this);

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

    public void chooseRecipients(View view) {
        //following code will be in your activity.java file

        mEmails = getNameEmailDetails();

        CharSequence[] items = mEmails.toArray(
                new CharSequence[mEmails.size()]);

        // arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose recipient or Go to Email");
        builder.setMultiChoiceItems(items, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    // indexSelected contains the index of item (of which checkbox checked)
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected,
                                        boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            seletedItems.add(indexSelected);
                        } else if (seletedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            seletedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                })
                // Set the action buttons
                .setPositiveButton("Go To Email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mRecipients = new String[seletedItems.size()];

                        for (int i = 0; i < seletedItems.size(); i++) {
                            mRecipients[i] = mEmails.get(Integer.parseInt(seletedItems.get(i) + ""));
                        }
                        sendEmail();
                    }
                });

        AlertDialog alertDialog = builder.create();//AlertDialog dialog; create like this outside onClick
        alertDialog.show();
    }

    public void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");  //set the email recipient
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, mRecipients);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Install DreamTeen Fitness and challenge me! \nhttp://goo.gl/6Kqzvs");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Invitation");
        //let the user choose what email client to use
        startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
    }

    public ArrayList<String> getNameEmailDetails() {
        ArrayList<String> emlRecs = new ArrayList<String>();
        HashSet<String> emlRecsHS = new HashSet<String>();
        ContentResolver cr = this.getContentResolver();
        String[] PROJECTION = new String[]{RawContacts._ID,
                Contacts.DISPLAY_NAME,
                Contacts.PHOTO_ID,
                Email.DATA,
                Photo.CONTACT_ID};
        String order = "CASE WHEN "
                + Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + Contacts.DISPLAY_NAME
                + ", "
                + Email.DATA
                + " COLLATE NOCASE";
        String filter = Email.DATA + " NOT LIKE ''";
        Cursor cur = cr.query(Email.CONTENT_URI, PROJECTION, filter, null, order);
        if (cur.moveToFirst()) {
            do {
                // names comes in hand sometimes
                String name = cur.getString(1);
                String emlAddr = cur.getString(3);

                // keep unique only
                if (emlRecsHS.add(emlAddr.toLowerCase())) {
                    emlRecs.add(emlAddr);
                }
            } while (cur.moveToNext());
        }

        cur.close();
        return emlRecs;
    }
}
