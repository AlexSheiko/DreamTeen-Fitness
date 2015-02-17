// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class WorkoutActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.WorkoutActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558475, "field 'mStartButton'");
    target.mStartButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558476, "field 'mFinishButton'");
    target.mFinishButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558473, "field 'mPauseButton'");
    target.mPauseButton = (android.widget.ImageButton) view;
    view = finder.findRequiredView(source, 2131558474, "field 'mDurationCounter'");
    target.mDurationCounter = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558470, "field 'mPositionLabel'");
    target.mPositionLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558468, "field 'mTitleTextView'");
    target.mTitleTextView = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558472, "field 'mDescriptionTextView'");
    target.mDescriptionTextView = (android.widget.TextView) view;
  }

  public static void reset(ui.activities.WorkoutActivity target) {
    target.mStartButton = null;
    target.mFinishButton = null;
    target.mPauseButton = null;
    target.mDurationCounter = null;
    target.mPositionLabel = null;
    target.mTitleTextView = null;
    target.mDescriptionTextView = null;
  }
}
