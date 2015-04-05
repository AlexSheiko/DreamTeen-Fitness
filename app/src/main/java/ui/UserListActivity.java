package ui;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import bellamica.tech.dreamteenfitness.R;
import ui.adapters.UserAdapter;

public class UserListActivity extends ListActivity {

    public String LOG_TAG = UserListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        final UserAdapter adapter = new UserAdapter(this,
                new ArrayList<ParseUser>());

        setListAdapter(adapter);

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        String email = sharedPrefs.getString("email", "");

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", email);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    for (ParseUser user : users) {
                        Log.v(LOG_TAG, user.getUsername());
                        adapter.add(user);
                    }
                }
            }
        });
    }
}
