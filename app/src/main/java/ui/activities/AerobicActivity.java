package ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import bellamica.tech.dreamteenfitness.R;

public class AerobicActivity extends Activity {

    public static final String TAG = "DreamTeen Fitness";
    private static final int REQUEST_OAUTH = 1;

    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;

    private Spinner mMonthSpinner;
    private Spinner mDaySpinner;
    private NumberPicker mNumberPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerobic);
        initializeViews();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();
    }

    /**
     * Build a GoogleApiClient that will authenticate the user and allow the application
     * to connect to Fitness APIs.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                // Now you can make calls to the Fitness APIs.
                                insertCalories();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            AerobicActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        authInProgress = true;
                                        result.startResolutionForResult(AerobicActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    private void insertCalories() {
        // Set a start and end time for our data, using a start time of 1 hour before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        // TODO: Restore duration from input before releasing
        // cal.add(Calendar.MINUTE, -mNumberPicker.getValue());
        cal.add(Calendar.SECOND, -1);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setName(TAG + " - calories expended")
                .setType(DataSource.TYPE_RAW)
                .build();

        // Create a data set
        float caloriesExpended = 111.0f;
        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_CALORIES).setFloat(caloriesExpended);
        dataSet.add(dataPoint);

        // Invoke the History API to insert the data
        Fitness.HistoryApi.insertData(mClient, dataSet);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

    private void initializeViews() {
        mMonthSpinner = (Spinner) findViewById(R.id.monthSpinner);
        ArrayAdapter<CharSequence> mMonthAdapter = ArrayAdapter.createFromResource(this,
                R.array.month_values, android.R.layout.simple_spinner_item);
        mMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonthSpinner.setAdapter(mMonthAdapter);
        mMonthSpinner.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        mDaySpinner = (Spinner) findViewById(R.id.daySpinner);
        ArrayAdapter<CharSequence> mDayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_values, android.R.layout.simple_spinner_item);
        mDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDaySpinner.setAdapter(mDayAdapter);
        mDaySpinner.setSelection(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1);

        mNumberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        mNumberPicker.setMinValue(5);
        mNumberPicker.setMaxValue(90);
        mNumberPicker.setValue(20);
        mNumberPicker.setWrapSelectorWheel(false);
    }

    public void saveData(View view) {
        Calendar calendar = Calendar.getInstance();
        String mYear = calendar.get(Calendar.YEAR) + "";
        String mMonth = mMonthSpinner.getSelectedItem().toString();
        String mDay = mDaySpinner.getSelectedItem().toString();
        String mHour = calendar.get(Calendar.HOUR) + "";
        String mMinute = calendar.get(Calendar.MINUTE) + "";
        String mSecond = calendar.get(Calendar.SECOND) + "";

        String dateString =
                mMonth + "." + mDay + "." + mYear + " " +
                        mHour + ":" + mMinute + ":" + mSecond;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM.d.yyyy k:m:s", Locale.US);
        Date convertedDate;
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.w(TAG, "Parse string date failed. Exception: " + e.getMessage());
            convertedDate = new Date();
        }
        calendar.setTime(convertedDate);

        if (!mClient.isConnected()) {
            mClient.connect();
        }

        navigateToMainScreen();
    }

    private void navigateToMainScreen() {
        Toast.makeText(this, "Workout saved", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
