// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class MainActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.MainActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558443, "field 'mCaloriesLabel'");
    target.mCaloriesLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558444, "field 'mProgressBar'");
    target.mProgressBar = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558442, "field 'mCaloriesContainer'");
    target.mCaloriesContainer = (android.widget.LinearLayout) view;
    view = finder.findRequiredView(source, 2131558441, "field 'mDrawerLayout'");
    target.mDrawerLayout = (android.support.v4.widget.DrawerLayout) view;
    view = finder.findRequiredView(source, 2131558448, "field 'mDrawerList'");
    target.mDrawerList = (android.widget.ListView) view;
    view = finder.findRequiredView(source, 2131558446, "field 'mStepsLabel'");
    target.mStepsLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558447, "field 'mStepsTargetLabel'");
    target.mStepsTargetLabel = (android.widget.TextView) view;
  }

  public static void reset(ui.activities.MainActivity target) {
    target.mCaloriesLabel = null;
    target.mProgressBar = null;
    target.mCaloriesContainer = null;
    target.mDrawerLayout = null;
    target.mDrawerList = null;
    target.mStepsLabel = null;
    target.mStepsTargetLabel = null;
  }
}
