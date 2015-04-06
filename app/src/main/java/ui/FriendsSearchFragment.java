package ui;

import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import bellamica.tech.dreamteenfitness.R;

public class FriendsSearchFragment extends ListFragment {

    private ArrayAdapter<String> mUserListAdapter;
    private List<ParseUser> mUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_search, parent, false);

        mUserListAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.user_list_item,
                android.R.id.text1,
                new ArrayList<String>());

        setListAdapter(mUserListAdapter);

        getActivity().setProgressBarIndeterminateVisibility(true);
        queryUsers();

        return rootView;
    }

    private void queryUsers() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());


        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            login();
            return;
        }
        List<String> friends = currentUser.getList("friends");

        String email = sharedPrefs.getString("email", "");

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", email);
        if (friends != null) {
            query.whereNotContainedIn("username", friends);
        } else {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    getActivity().setProgressBarIndeterminateVisibility(false);
                    mUsers = users;
                    for (ParseUser user : users) {
                        mUserListAdapter.add(user.getString("personName"));
                    }
                } else {
                    login();
                }
            }
        });
    }

    private void login() {
        String username =
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString("email", "");

        ParseUser.logInInBackground(username, "123", new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    queryUsers();
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        List<String> friends = currentUser.getList("friends");

        if (friends == null) {
            friends = new ArrayList<>();
        }

        friends.add(mUsers.get(position).getUsername());
        currentUser.put("friends", friends);
        currentUser.saveEventually();

        Toast.makeText(getActivity(), String.format("%s is now your friend!",
                        mUserListAdapter.getItem(position)), Toast.LENGTH_LONG).show();

        getActivity().recreate();
    }
}
