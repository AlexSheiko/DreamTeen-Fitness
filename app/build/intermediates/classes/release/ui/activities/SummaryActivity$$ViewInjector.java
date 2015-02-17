// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class SummaryActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.SummaryActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558461, "field 'mUnitsLabel'");
    target.mUnitsLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558460, "field 'mDistanceLabel'");
    target.mDistanceLabel = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131558462, "field 'mNameField'");
    target.mNameField = (android.widget.EditText) view;
    view = finder.findRequiredView(source, 2131558466, "field 'mDiscardButton'");
    target.mDiscardButton = (android.widget.TextView) view;
  }

  public static void reset(ui.activities.SummaryActivity target) {
    target.mUnitsLabel = null;
    target.mDistanceLabel = null;
    target.mNameField = null;
    target.mDiscardButton = null;
  }
}
