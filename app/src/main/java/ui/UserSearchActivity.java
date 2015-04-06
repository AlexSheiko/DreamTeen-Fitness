package ui;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
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

public class UserSearchActivity extends ListActivity {

    private ArrayAdapter<String> mUserListAdapter;
    private List<ParseUser> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_user_search);

        mUserListAdapter = new ArrayAdapter<>(this,
                R.layout.user_list_item,
                android.R.id.text1,
                new ArrayList<String>());

        setListAdapter(mUserListAdapter);

        setProgressBarIndeterminateVisibility(true);
        queryUsers();
    }

    private void queryUsers() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);


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
            setProgressBarIndeterminateVisibility(false);
        }
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    setProgressBarIndeterminateVisibility(false);
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
                PreferenceManager.getDefaultSharedPreferences(this)
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        List<String> friends = currentUser.getList("friends");

        if (friends == null) {
            friends = new ArrayList<>();
        }

        friends.add(mUsers.get(position).getUsername());
        currentUser.put("friends", friends);
        currentUser.saveEventually();

        Toast.makeText(this, String.format("%s is now your friend!",
                        mUserListAdapter.getItem(position)), Toast.LENGTH_LONG).show();

        recreate();
    }
}
