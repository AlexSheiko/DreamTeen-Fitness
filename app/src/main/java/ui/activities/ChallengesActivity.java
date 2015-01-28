package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnNewPermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bellamica.tech.dreamteenfitness.R;
import ui.utils.adapters.StatsListAdapter;

public class ChallengesActivity extends Activity {

    private static final String TAG = ChallengesActivity.class.getSimpleName();

    private SimpleFacebook mSimpleFacebook;

    public static final String NAME_COLUMN = "First";
    public static final String SCORE_COLUMN = "Second";

    private ArrayList<HashMap> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSimpleFacebook = SimpleFacebook.getInstance(this);
        mSimpleFacebook.login(onLoginListener);

        setContentView(R.layout.activity_challenges);
    }

    OnLoginListener onLoginListener = new OnLoginListener() {
        @Override
        public void onLogin() {
            Permission[] permissions = new Permission[]{
                    Permission.PUBLISH_ACTION,
                    Permission.USER_FRIENDS
            };

            mSimpleFacebook.requestNewPermissions(permissions, false, onNewPermissionsListener);
        }

        @Override
        public void onNotAcceptingPermissions(Permission.Type type) {
            // user didn't accept READ or WRITE permission
            Log.w(TAG, String.format("You didn't accept %s permissions", type.name()));
        }

        @Override
        public void onThinking() {
        }

        @Override
        public void onException(Throwable throwable) {
            Log.e(TAG, throwable.getMessage());
        }

        @Override
        public void onFail(String s) {
            Log.e(TAG, "Failed. Reason: " + s);
        }
    };

    OnNewPermissionsListener onNewPermissionsListener = new OnNewPermissionsListener() {

        @Override
        public void onSuccess(String s, List<Permission> permissions) {
            Session session = mSimpleFacebook.getSession();
            /* make the API call */
            new Request(
                    session,
                    "/562228307244897/scores",
                    null,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            /* handle the result */
                            try {
                                JSONObject responseBody = new JSONObject(response.getRawResponse());
                                JSONArray dataArray = responseBody.getJSONArray("data");

                                list = new ArrayList<>();
                                for(int i = 0; i < dataArray.length(); i++) {
                                    JSONObject scoreObj = dataArray.getJSONObject(i);
                                    String score = scoreObj.getString("score");
                                    JSONObject userObj = scoreObj.getJSONObject("user");
                                    String name = userObj.getString("name");

                                    HashMap temp = new HashMap();
                                    temp.put(NAME_COLUMN, name);
                                    temp.put(SCORE_COLUMN, score);
                                    list.add(temp);
                                }

                                StatsListAdapter adapter = new StatsListAdapter(ChallengesActivity.this, list);
                                ListView mListView = (ListView) findViewById(R.id.stats_list);
                                mListView.setAdapter(adapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).executeAsync();
        }

        @Override
        public void onNotAcceptingPermissions(Permission.Type type) {
            // user didn't accept READ or WRITE permission
            Log.w(TAG, String.format("You didn't accept %s permissions", type.name()));
        }

        @Override
        public void onThinking() {
        }

        @Override
        public void onException(Throwable throwable) {
            Log.e(TAG, throwable.getMessage());
        }

        @Override
        public void onFail(String s) {
            Log.i(TAG, "Failed to request new permissions. Cause: " + s);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
