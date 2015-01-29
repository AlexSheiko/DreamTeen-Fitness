package ui.activities;

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

import java.util.ArrayList;
import java.util.HashSet;

import bellamica.tech.dreamteenfitness.R;


public class FriendsActivity extends Activity
        implements OnClickListener {

    private ArrayList<String> mEmails;
    private String[] mRecipients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Button shareButton = (Button) findViewById(R.id.share_button);
        shareButton.setOnClickListener(this);
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
                            // write your code when user checked the checkbox
                            seletedItems.add(indexSelected);
                        } else if (seletedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            // write your code when user Uchecked the checkbox
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

    public void sendEmail(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");  //set the email recipient
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, mRecipients);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Install DreamTeen Fitness and challenge me! \nhttp://goo.gl/6Kqzvs");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Invitation");
        //let the user choose what email client to use
        startActivity(Intent.createChooser(emailIntent, "Send mail using...")); }

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

