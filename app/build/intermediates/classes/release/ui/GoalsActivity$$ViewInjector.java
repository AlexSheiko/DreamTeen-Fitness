// Generated code from Butter Knife. Do not modify!
package ui;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class GoalsActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.GoalsActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558441, "field 'mStepsNotSetLabel'");
    target.mStepsNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558445, "field 'mDurationNotSetLabel'");
    target.mDurationNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558449, "field 'mCaloriesNotSetLabel'");
    target.mCaloriesNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558442, "field 'mStepsButton'");
    target.mStepsButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558446, "field 'mDurationButton'");
    target.mDurationButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558450, "field 'mCaloriesButton'");
    target.mCaloriesButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558440, "field 'mPbSteps'");
    target.mPbSteps = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558444, "field 'mPbDuration'");
    target.mPbDuration = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558448, "field 'mPbCalories'");
    target.mPbCalories = (android.widget.ProgressBar) view;
  }

  public static void reset(ui.GoalsActivity target) {
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
