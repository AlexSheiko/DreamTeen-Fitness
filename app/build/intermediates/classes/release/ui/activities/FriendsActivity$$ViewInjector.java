// Generated code from Butter Knife. Do not modify!
package ui.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class FriendsActivity$$ViewInjector {
  public static void inject(Finder finder, final ui.activities.FriendsActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558438, "field 'mFacebookButton'");
    target.mFacebookButton = (com.facebook.widget.LoginButton) view;
    view = finder.findRequiredView(source, 2131558439, "field 'mGoogleButton'");
    target.mGoogleButton = (android.widget.Button) view;
    view = finder.findRequiredView(source, 2131558440, "field 'mEmailButton'");
    target.mEmailButton = (android.widget.Button) view;
  }

  public static void reset(ui.activities.FriendsActivity target) {
    target.mFacebookButton = null;
    target.mGoogleButton = null;
    target.mEmailButton = null;
  }
}
