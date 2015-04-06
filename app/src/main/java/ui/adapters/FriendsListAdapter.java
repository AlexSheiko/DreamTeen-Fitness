package ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import bellamica.tech.dreamteenfitness.R;
import ui.SendMessageActivity;
import ui.utils.CircleTransform;

public class FriendsListAdapter extends ArrayAdapter<ParseUser> {

    public FriendsListAdapter(Context context, List<ParseUser> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ParseUser user = getItem(position);

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.friend_list_item, parent, false);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SendMessageActivity.class);
                intent.putExtra(Intent.EXTRA_EMAIL, user.getUsername());
                getContext().startActivity(intent);
            }
        });

        ImageView avatar = (ImageView) rootView.findViewById(android.R.id.icon1);
        Picasso.with(getContext())
                .load(user.getString("avatarUrl"))
                .transform(new CircleTransform())
                .error(R.drawable.avatar_default)
                .into(avatar);

        TextView name = (TextView) rootView.findViewById(android.R.id.text1);
        name.setText(user.getString("personName"));

        return rootView;
    }
}
