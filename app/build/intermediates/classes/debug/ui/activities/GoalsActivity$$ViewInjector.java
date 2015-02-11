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
    view = finder.findRequiredView(source, 2131558444, "field 'mSetStepsButton'");
    target.mSetStepsButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558448, "field 'mSetDurationButton'");
    target.mSetDurationButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558442, "field 'mProgressBarDailySteps'");
    target.mProgressBarDailySteps = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558446, "field 'mProgressBarWeeklyDuration'");
    target.mProgressBarWeeklyDuration = (android.widget.ProgressBar) view;
  }

  public static void reset(ui.activities.GoalsActivity target) {
    target.mStepsNotSetLabel = null;
    target.mDurationNotSetLabel = null;
    target.mSetStepsButton = null;
    target.mSetDurationButton = null;
    target.mProgressBarDailySteps = null;
    target.mProgressBarWeeklyDuration = null;
  }
}
