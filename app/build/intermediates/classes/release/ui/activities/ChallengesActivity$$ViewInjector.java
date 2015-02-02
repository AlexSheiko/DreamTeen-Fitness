// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class ChallengesActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.ChallengesActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558442, "field 'mStepsNotSetLabel'");
    target.mStepsNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558448, "field 'mDurationNotSetLabel'");
    target.mDurationNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558443, "field 'mSetStepsButton'");
    target.mSetStepsButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558449, "field 'mSetDurationButton'");
    target.mSetDurationButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558439, "field 'mProgressBarDailySteps'");
    target.mProgressBarDailySteps = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558440, "field 'mProgressBarWeeklySteps'");
    target.mProgressBarWeeklySteps = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558441, "field 'mProgressBarMonthlySteps'");
    target.mProgressBarMonthlySteps = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558445, "field 'mProgressBarDailyDuration'");
    target.mProgressBarDailyDuration = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558446, "field 'mProgressBarWeeklyDuration'");
    target.mProgressBarWeeklyDuration = (android.widget.ProgressBar) view;
    view = finder.findRequiredView(source, 2131558447, "field 'mProgressBarMonthlyDuration'");
    target.mProgressBarMonthlyDuration = (android.widget.ProgressBar) view;
  }

  public static void reset(ui.activities.ChallengesActivity target) {
    target.mStepsNotSetLabel = null;
    target.mDurationNotSetLabel = null;
    target.mSetStepsButton = null;
    target.mSetDurationButton = null;
    target.mProgressBarDailySteps = null;
    target.mProgressBarWeeklySteps = null;
    target.mProgressBarMonthlySteps = null;
    target.mProgressBarDailyDuration = null;
    target.mProgressBarWeeklyDuration = null;
    target.mProgressBarMonthlyDuration = null;
  }
}
