package model;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import bellamica.tech.dreamteenfitness.R;

public class Exercise {
    private String mCategory;
    private List<String> mTitles;
    private List<Integer> mImageIds;

    public Exercise(Context context, String category) {
        if (category.equals("ab")) {
            mTitles = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab));
        } else if (category.equals("leg")) {
            mTitles = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab));
        } else if (category.equals("arm")) {
            mTitles = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab));
        } else if (category.equals("butt")) {
            mTitles = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab));
        }
        for (int i = 0; i < mTitles.size(); i++) {
            mImageIds.add(context.getResources().getIdentifier(
                    category + "_" + i,
                    "drawable",
                    "bellamica.tech.dreamteenfitness"));
        }
    }

    public List<String> getTitles() {
        return mTitles;
    }

    public List<Integer> getImageIds() {
        return mImageIds;
    }
}
