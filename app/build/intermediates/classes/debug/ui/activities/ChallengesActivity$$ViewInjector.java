// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class ChallengesActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.ChallengesActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558439, "field 'mStepsNotSetLabel'");
    target.mStepsNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558442, "field 'mDurationNotSetLabel'");
    target.mDurationNotSetLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558440, "field 'mSetStepsButton'");
    target.mSetStepsButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558443, "field 'mSetDurationButton'");
    target.mSetDurationButton = (android.widget.Button) view;
  }

  public static void reset(ui.activities.ChallengesActivity target) {
    target.mStepsNotSetLabel = null;
    target.mDurationNotSetLabel = null;
    target.mSetStepsButton = null;
    target.mSetDurationButton = null;
  }
}
