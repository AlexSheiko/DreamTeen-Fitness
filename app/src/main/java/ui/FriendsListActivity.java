package ui;

import android.app.ListActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;

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

    private FriendsListAdapter mFriendsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        mFriendsListAdapter = new FriendsListAdapter(this,
                new ArrayList<ParseUser>());

        setListAdapter(mFriendsListAdapter);

        queryFriends();
    }

    private void queryFriends() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            login();
            return;
        }
        List<String> friends = currentUser.getList("friends");

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn("username", friends);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    for (ParseUser user : users) {
                        mFriendsListAdapter.add(user);
                    }
                } else {
                    login();
                }
            }
        });
    }

    private void login() {
        String username =
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("email", "");

        ParseUser.logInInBackground(username, "123", new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    queryFriends();
                }
            }
        });
    }
}
