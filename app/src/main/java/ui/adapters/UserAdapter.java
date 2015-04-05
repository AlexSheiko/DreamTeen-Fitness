package ui.adapters;

import android.content.Context;
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
import ui.utils.CircleTransform;

public class UserAdapter extends ArrayAdapter<ParseUser> {

    public UserAdapter(Context context, List<ParseUser> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ParseUser user = getItem(position);

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.user_list_item, parent, false);

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
