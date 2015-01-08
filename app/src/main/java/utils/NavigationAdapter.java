package utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import bellamica.tech.dreamfit.R;


public class NavigationAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final Integer[] imgid;

    public NavigationAdapter(Activity context, String[] itemname, Integer[] imgid) {
        super(context, R.layout.nav_item, itemname);

        this.context = context;
        this.itemname = itemname;
        this.imgid = imgid;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView;
        if (position <= 3) {
            rowView = inflater.inflate(R.layout.nav_item, null, true);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.image1);
            imageView.setImageResource(imgid[position]);
        } else {
            rowView = inflater.inflate(R.layout.nav_item_small, null, true);
        }
        TextView txtTitle = (TextView) rowView.findViewById(R.id.text1);
        txtTitle.setText(itemname[position]);
        return rowView;
    }

    ;
}
