package model;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import bellamica.tech.dreamteenfitness.R;

public class Exercise {
    private List<String> mTitles;
    private List<String> mDescriptions;

    public Exercise(Context context, String category) {
        if (category.equals("ab")) {
            mTitles = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab));
            mDescriptions = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab_desc));
        } else if (category.equals("leg")) {
            mTitles = Arrays.asList(
                    context.getResources().getStringArray(R.array.leg));
            mDescriptions = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab_desc));
        } else if (category.equals("arm")) {
            mTitles = Arrays.asList(
                    context.getResources().getStringArray(R.array.arm));
            mDescriptions = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab_desc));
        } else if (category.equals("butt")) {
            mTitles = Arrays.asList(
                    context.getResources().getStringArray(R.array.butt));
            mDescriptions = Arrays.asList(
                    context.getResources().getStringArray(R.array.ab_desc));
        }
    }

    public String getTitle(int position) {
        if (position < 10) {
            return mTitles.get(position);
        }
        return null;
    }

    public String getDescription(int position) {
        if (position < 10) {
            return mDescriptions.get(position);
        }
        return null;
    }
}
