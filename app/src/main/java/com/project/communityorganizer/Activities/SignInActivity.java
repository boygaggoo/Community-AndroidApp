package com.project.communityorganizer.Activities;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.project.communityorganizer.JSON.models.UserJSONModel;
import com.project.communityorganizer.R;
import com.project.communityorganizer.Services.DeviceManager;
import com.project.communityorganizer.Services.LocationService;
import com.project.communityorganizer.Services.RestService;
import com.project.communityorganizer.Services.SaveSharedPreference;
import com.project.communityorganizer.Services.passwordHash;
import com.project.communityorganizer.sqlite.models.User;

import java.security.NoSuchAlgorithmException;

/**
 * Created by
 * @author Seshagiri on 19/2/15.
 */
public class SignInActivity extends Activity  implements OnClickListener {
	private EditText eMailText, passwordText;
    private Button btnCancel, btnSign;
    DeviceManager deviceManager;
    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        setTitle("Sign In");
        deviceManager = new DeviceManager();
        findViewsById();
        deviceManager.showConnectivity(getApplicationContext());
        btnClick();
    }

    /**
     * Handles button click events
     */
    private void btnClick() {
        btnSign.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validate()) {
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                } else {
                    String email = eMailText.getText().toString().trim();
                    String plainText = passwordText.getText().toString();
                    passwordHash hash = new passwordHash();
                    String password = null;
                    try {
                        password = hash.findHash(plainText);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    new validateUserTask().execute(email, password);
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(SignInActivity.this);
            }
        });
    }

    /**
     * Validates the form
     * @return boolean
     */
    private boolean validate() {
        if(eMailText.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(),R.string.fill_email_field,
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (passwordText.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(),R.string.fill_password_field,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Identifies elements in the view by its ID
     */
    private void findViewsById() {
        passwordText = (EditText) findViewById(R.id.etPass);
        eMailText = (EditText) findViewById(R.id.etEmail);
        btnSign = (Button) findViewById(R.id.btnSignIn);
        btnCancel = (Button) findViewById(R.id.btnCancel);
    }

    /**
     * {@inheritDoc}
     * @param v
     */
    @Override
    public void onClick(View v) {

    }

    /**
     * Asynchronous task to validate user credentials from local db
     */
    private class validateUserTask extends AsyncTask<String, Integer, Boolean> {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * {@inheritDoc}
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(String... params) {
            if (User.checkCredentials(params[0], params[1])) {
                SaveSharedPreference.setUserEmail(SignInActivity.this, params[0]);
                UserJSONModel userJSONModel = User.getUserJSONObject(params[0]);
                SaveSharedPreference
                        .setUserEmail(SignInActivity.this,
                                userJSONModel.getEmail());
                SaveSharedPreference
                        .setUserName(SignInActivity.this,
                                userJSONModel.getDisplay_name());
                LocationService locationService = new LocationService(SignInActivity.this);
                locationService.startLocationService();
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         * @param s
         */
        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if (s) {
                RestService service = new RestService();
                service.fetchFriendList(SaveSharedPreference.getUserEmail(SignInActivity.this));
                service.fetchGeofenceList();
                service.fetchOpenEventList();
                Intent i = new Intent(SignInActivity.this, HomeActivity.class);
                startActivity(i);
                SignInActivity.this.finish();
            } else {
                // TODO for a registered user
                Toast toast = Toast.makeText(SignInActivity.this,
                        "Username and Password doesn't match", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

}