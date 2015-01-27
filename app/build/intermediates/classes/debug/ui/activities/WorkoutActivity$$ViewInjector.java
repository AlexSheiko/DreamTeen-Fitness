// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class WorkoutActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.WorkoutActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558463, "field 'mStartButton'");
    target.mStartButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558464, "field 'mFinishButton'");
    target.mFinishButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558459, "field 'mPauseButton'");
    target.mPauseButton = (android.widget.ImageButton) view;
    view = finder.findRequiredView(source, 2131558462, "field 'mDurationCounter'");
    target.mDurationCounter = (android.widget.TextView) view;
  }

  public static void reset(ui.activities.WorkoutActivity target) {
    target.mStartButton = null;
    target.mFinishButton = null;
    target.mPauseButton = null;
    target.mDurationCounter = null;
  }
}
