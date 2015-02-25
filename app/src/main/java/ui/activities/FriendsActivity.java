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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.Builder;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.plus.PlusShare;

import java.util.ArrayList;
import java.util.HashSet;

import bellamica.tech.dreamteenfitness.R;
import butterknife.ButterKnife;
import butterknife.InjectView;


public class FriendsActivity extends Activity implements OnClickListener {

    @InjectView(R.id.facebook_button)
    Button mFacebookButton;
    @InjectView(R.id.google_button)
    Button mGoogleButton;
    @InjectView(R.id.email_button)
    Button mEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        ButterKnife.inject(this);

        mFacebookButton.setOnClickListener(this);
        mGoogleButton.setOnClickListener(this);
        mEmailButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.facebook_button:
                // TODO Invite Facebook
                break;
            case R.id.google_button:
                inviteGoogle();
                break;
            case R.id.email_button:
                inviteEmail();
                break;
        }
    }

    private void inviteFacebook() {
        Bundle params = new Bundle();
        params.putString("message", "DreamTeen Fitness is pretty cool. Check it out on Google Play. See if you can beat my score! http://goo.gl/YdMFVk");

        WebDialog dialog = new Builder(this, Session.getActiveSession(), "apprequests", params).
                setOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(Bundle values, FacebookException error) {
                        if (error == null) {
                            Toast.makeText(FriendsActivity.this,
                                    getString(R.string.request_sent_toast), Toast.LENGTH_SHORT).show();
                        } else {
                            error.printStackTrace();
                        }
                        Session.getActiveSession().closeAndClearTokenInformation();
                    }
                }).build();

        Window dialog_window = dialog.getWindow();
        dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dialog.show();
    }

    private void inviteGoogle() {
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
    }

    private ArrayList<String> mEmailList;
    private String[] mRecipientNames;

    private void inviteEmail() {

        mEmailList = getFriendsEmails();

        CharSequence[] items = mEmailList.toArray(
                new CharSequence[mEmailList.size()]);

        // ArrayList to keep the selected items
        final ArrayList selectedItems = new ArrayList();

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
                            selectedItems.add(indexSelected);
                        } else if (selectedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            selectedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                })
                // Set the action buttons
                .setPositiveButton("Go To Email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mRecipientNames = new String[selectedItems.size()];

                        for (int i = 0; i < selectedItems.size(); i++) {
                            mRecipientNames[i] = mEmailList.get(Integer.parseInt(selectedItems.get(i) + ""));
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
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, mRecipientNames);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Install DreamTeen Fitness and challenge me! \nhttp://goo.gl/6Kqzvs");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Invitation");
        //let the user choose what email client to use
        startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
    }

    public ArrayList<String> getFriendsEmails() {
        ArrayList<String> emlRecs = new ArrayList<>();
        HashSet<String> emlRecsHS = new HashSet<>();
        ContentResolver resolver = this.getContentResolver();
        String[] projection = new String[] {Email.DATA};
        String filter = Email.DATA + " NOT LIKE ''";
        Cursor cursor = resolver.query(
                Email.CONTENT_URI,
                projection,
                filter,
                null,
                null);
        if (cursor.moveToFirst()) {
            do {
                String emailAddress = cursor.getString(0);
                // keep unique only
                if (emlRecsHS.add(emailAddress.toLowerCase())) {
                    emlRecs.add(emailAddress);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return emlRecs;
    }
}

