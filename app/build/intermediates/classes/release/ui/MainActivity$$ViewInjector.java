// Generated code from Butter Knife. Do not modify!
package ui;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class MainActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.MainActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558452, "field 'mCaloriesLabel'");
    target.mCaloriesLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558453, "field 'mPbCalories'");
    target.mPbCalories = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558447, "field 'mCaloriesContainer'");
    target.mCaloriesContainer = (android.widget.LinearLayout) view;
    view = finder.findRequiredView(source, 2131558439, "field 'mStepsContainer'");
    target.mStepsContainer = (android.widget.LinearLayout) view;
    view = finder.findRequiredView(source, 2131558451, "field 'mDrawerLayout'");
    target.mDrawerLayout = (android.support.v4.widget.DrawerLayout) view;
    view = finder.findRequiredView(source, 2131558456, "field 'mDrawerList'");
    target.mDrawerList = (android.widget.ListView) view;
    view = finder.findRequiredView(source, 2131558454, "field 'mStepsTakenLabel'");
    target.mStepsTakenLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558455, "field 'mStepProgressLabel'");
    target.mStepProgressLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558449, "field 'mCalNotSetLabel'");
    target.mCalNotSetLabel = (android.widget.TextView) view;
  }

  public static void reset(ui.MainActivity target) {
    target.mCaloriesLabel = null;
    target.mPbCalories = null;
    target.mCaloriesContainer = null;
    target.mStepsContainer = null;
    target.mDrawerLayout = null;
    target.mDrawerList = null;
    target.mStepsTakenLabel = null;
    target.mStepProgressLabel = null;
    target.mCalNotSetLabel = null;
  }
}
