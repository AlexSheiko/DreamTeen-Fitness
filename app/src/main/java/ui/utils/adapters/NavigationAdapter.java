package ui.utils.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import bellamica.tech.dreamteenfitness.R;


public class NavigationAdapter extends ArrayAdapter<String> {

    private final Activity mContext;
    private final String[] mItemName;
    private final Integer[] mImgId;

    public NavigationAdapter(Activity context, String[] itemname, Integer[] imgid) {
        super(context, R.layout.nav_item, itemname);

        mContext = context;
        mItemName = itemname;
        mImgId = imgid;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View rowView;
        if (position <= 3) {
            rowView = inflater.inflate(R.layout.nav_item, null, true);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.image1);
            imageView.setImageResource(mImgId[position]);
        } else {
            rowView = inflater.inflate(R.layout.nav_item_small, null, true);
        }
        TextView txtTitle = (TextView) rowView.findViewById(R.id.text1);
        txtTitle.setText(mItemName[position]);
        return rowView;
    }
}
