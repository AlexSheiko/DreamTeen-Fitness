package utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import bellamica.tech.dreamfit.R;
import ui.MainActivity;


public class SplashScreen extends Activity {

    private String mDeviceId;
    private ParseObject phoneReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Parse.initialize(this,
                "ygyWodAYEDqiQ795p1V4Jxs2yRm9KTiBKsGSnakD",
                "IGTbu2n4KePoggvgXmBUS4k6cg5wQH8lQOA3Uo3k");

        getDeviceId();
    }

    private Void getDeviceId() {
        mDeviceId = Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PhoneReg");
        query.whereEqualTo("deviceId", mDeviceId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    if (scoreList.size() == 0) {
                        // Register ID if doesn't exist
                        phoneReg = new ParseObject("PhoneReg");
                        phoneReg.put("deviceId", mDeviceId);
                        phoneReg.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                navigateToEnterBodyParams();
                            }
                        });
                    } else {
                        navigateToMainScreen();
                    }
                }

            }
        });
        return null;
    }

    private void navigateToMainScreen() {
        startActivity(new Intent(SplashScreen.this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void navigateToEnterBodyParams() {
        startActivity(new Intent(SplashScreen.this, BodyParamsInput.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }
}
