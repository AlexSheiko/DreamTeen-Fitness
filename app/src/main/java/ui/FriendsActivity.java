package ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;

import bellamica.tech.dreamteenfitness.R;

public class FriendsActivity extends Activity {

    PagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    private ArrayList<String> mEmails;
    private String[] mRecipients;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mPagerAdapter =
                new PagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        actionBar.addTab(actionBar.newTab()
                            .setText("Invite")
                            .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab()
                            .setText("Search")
                            .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab()
                            .setText("My friends")
                            .setTabListener(tabListener));
    }


    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new FriendsInviteFragment();
                    break;
                case 1:
                    fragment = new FriendsSearchFragment();
                    break;
                case 2:
                    fragment = new FriendsListFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
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
        ContentResolver cr = getContentResolver();
        String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID};
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
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
