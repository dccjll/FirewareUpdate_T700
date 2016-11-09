package com.dsm.t700.update;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A login screen that offers login via mobile/password.
 */
public class LoginActivity extends Activity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
//    private static final String[] DUMMY_CREDENTIALS = new String[]{
//            "foo@example.com:hello", "bar@example.com:world"
//    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mMobileView;
    private EditText mPasswordView;
//    private View mProgressView;
//    private View mLoginFormView;

    private final static int BluetoothAdapter_ACTION_REQUEST_ENABLE = 0x0001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(!isBLESupport()){
            Toast.makeText(this, "当前手机不支持蓝牙ble功能", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up the login form.
        mMobileView = (AutoCompleteTextView) findViewById(R.id.mobile);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mmobileSignInButton = (Button) findViewById(R.id.mobile_sign_in_button);
        mmobileSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

//        mLoginFormView = findViewById(R.id.login_form);
//        mProgressView = findViewById(R.id.login_progress);
    }

    private boolean isBLESupport(){
        // 检查当前手机是否支持ble
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid mobile, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mMobileView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String mobile = mMobileView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid mobile address.
        if (TextUtils.isEmpty(mobile)) {
            mMobileView.setError(getString(R.string.error_field_required));
            focusView = mMobileView;
            cancel = true;
        } else if (!isMobileValid(mobile)) {
            mMobileView.setError(getString(R.string.error_invalid_mobile));
            focusView = mMobileView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
            mAuthTask = new UserLoginTask(mobile, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isMobileValid(String mobile) {
        //TODO: Replace this with your own logic
//        return mobile.contains("@");
        return regexMobile(mobile);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    //正则通用验证
    public static boolean regexCheck(String res, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(res);
        if(matcher.matches()){
            return true;
        }
        return false;
    }

    //正则验证 11位数字手机号码
    public static boolean regexMobile(String res){
//		if(regexCheck(res, "^[1]([3][0-9]{1}|59|58|88|89)[0-9]{8}$")){
//			return true;
//		}
//		return false;
        if(regexCheck(res, "^[1]([3]|[4]|[5]|[7]|[8])[0-9]{9}$")){
            return true;
        }
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    public void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mmobile;
        private final String mPassword;

        UserLoginTask(String mobile, String password) {
            mmobile = mobile;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mmobile)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
//            if(!"18888888888".equals(mmobile) || !"123456".equals(mPassword)){
//                return false;
//            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
//            showProgress(false);

            if (success) {
//                finish();
                Toast.makeText(LoginActivity.this, "login success", Toast.LENGTH_SHORT).show();
                /*if(!enableBluetooth()){
                    LoginActivity.this.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BluetoothAdapter_ACTION_REQUEST_ENABLE);
                    return;
                }*/
                startActivity(new Intent(LoginActivity.this, DeviceListActivity.class));
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
//            showProgress(false);
        }
    }

    private boolean enableBluetooth(){
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null || !bluetoothAdapter.enable()){
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case BluetoothAdapter_ACTION_REQUEST_ENABLE:
                switch (resultCode){
                    case RESULT_OK:
                        startActivity(new Intent(LoginActivity.this, DeviceListActivity.class));
                        break;
                    default:
                        Toast.makeText(LoginActivity.this, "不允许开启蓝牙", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

