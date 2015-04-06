package ui;

import android.app.ListFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import bellamica.tech.dreamteenfitness.R;
import ui.adapters.FriendsListAdapter;

public class FriendsListFragment extends ListFragment {

    public String LOG_TAG = FriendsListFragment.class.getSimpleName();

    private FriendsListAdapter mFriendsListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_list, parent, false);

        mFriendsListAdapter = new FriendsListAdapter(getActivity(),
                new ArrayList<ParseUser>());

        setListAdapter(mFriendsListAdapter);

        getActivity().setProgressBarIndeterminateVisibility(true);
        queryFriends();

        return rootView;
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
                    getActivity().setProgressBarIndeterminateVisibility(false);
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
                PreferenceManager.getDefaultSharedPreferences(getActivity())
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
