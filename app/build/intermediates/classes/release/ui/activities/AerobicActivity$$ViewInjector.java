// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class AerobicActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.AerobicActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558434, "field 'mMonthSpinner'");
    target.mMonthSpinner = (android.widget.Spinner) view;
    view = finder.findRequiredView(source, 2131558435, "field 'mDaySpinner'");
    target.mDaySpinner = (android.widget.Spinner) view;
    view = finder.findRequiredView(source, 2131558436, "field 'mNumberPicker'");
    target.mNumberPicker = (android.widget.NumberPicker) view;
  }

  public static void reset(ui.activities.AerobicActivity target) {
    target.mMonthSpinner = null;
    target.mDaySpinner = null;
    target.mNumberPicker = null;
  }
}
