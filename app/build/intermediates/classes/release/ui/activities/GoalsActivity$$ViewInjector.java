// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class GoalsActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.GoalsActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558443, "field 'mStepsNotSetLabel'");
    target.mStepsNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558447, "field 'mDurationNotSetLabel'");
    target.mDurationNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558451, "field 'mCaloriesNotSetLabel'");
    target.mCaloriesNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558444, "field 'mStepsButton'");
    target.mStepsButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558448, "field 'mDurationButton'");
    target.mDurationButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558452, "field 'mCaloriesButton'");
    target.mCaloriesButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558442, "field 'mPbSteps'");
    target.mPbSteps = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558446, "field 'mPbDuration'");
    target.mPbDuration = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558450, "field 'mPbCalories'");
    target.mPbCalories = (android.widget.ProgressBar) view;
  }

  public static void reset(ui.activities.GoalsActivity target) {
    target.mStepsNotSetLabel = null;
    target.mDurationNotSetLabel = null;
    target.mCaloriesNotSetLabel = null;
    target.mStepsButton = null;
    target.mDurationButton = null;
    target.mCaloriesButton = null;
    target.mPbSteps = null;
    target.mPbDuration = null;
    target.mPbCalories = null;
  }
}
