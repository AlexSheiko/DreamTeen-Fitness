package ui;

import android.app.ListActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import bellamica.tech.dreamteenfitness.R;
import ui.adapters.FriendsListAdapter;

public class FriendsListActivity extends ListActivity {

    public String LOG_TAG = FriendsListActivity.class.getSimpleName();

    private FriendsListAdapter mFriendsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_friends_list);

        mFriendsListAdapter = new FriendsListAdapter(this,
                new ArrayList<ParseUser>());

        setListAdapter(mFriendsListAdapter);

        setProgressBarIndeterminateVisibility(true);
        queryFriends();
    }

    private void queryFriends() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            login();
            return;
        }
        List<String> friends = currentUser.getList("friends");
        if (friends == null) {
            friends = new ArrayList<>();
        }

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn("username", friends);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    setProgressBarIndeterminateVisibility(false);
                    for (ParseUser user : users) {
                        mFriendsListAdapter.add(user);
                    }
                } else {
                    Log.i(LOG_TAG, "Failed to list friends. Repeat login method.");
                    login();
                }
            }
        });
    }

    private void login() {
        String username =
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("email", "");
        Log.v(LOG_TAG, "Login #1. Email = " + username);

        ParseUser.logInInBackground(username, "123", new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    Log.v(LOG_TAG, "Login #2. Logged in successfully. Query friends again.");
                    queryFriends();
                } else {
                    Log.w(LOG_TAG, "Login #2. Failed to login. Cause: " + e.getMessage());
                }
            }
        });
    }
}
