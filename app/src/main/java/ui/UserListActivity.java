package ui;

import android.app.ListActivity;
import android.os.Bundle;

import bellamica.tech.dreamteenfitness.R;

public class UserListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
    }
}
