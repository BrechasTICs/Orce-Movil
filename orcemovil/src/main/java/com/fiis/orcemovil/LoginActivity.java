package com.fiis.orcemovil;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.fiis.orcemovil.util.CookieManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello",
            "bar@example.com:world"
    };

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    private static CookieManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView = (EditText) findViewById(R.id.username);
        mEmailView.setText(mEmail);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    try {
                        attemptLogin();
                    } catch (UnsupportedEncodingException e) {
                        Log.e("post_exception", e.getMessage());
                    }
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    attemptLogin();
                } catch (UnsupportedEncodingException e) {
                    Log.e("post_exception", e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cm = new CookieManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() throws UnsupportedEncodingException {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        // Create data variable for sent values to server

        String data = URLEncoder.encode(getString(R.string.__user), "UTF-8")
                + "=" + URLEncoder.encode(mEmail, "UTF-8");

        data += "&" + URLEncoder.encode(getString(R.string.__pass), "UTF-8") + "="
                + URLEncoder.encode(md5(mPassword), "UTF-8");

        data += "&" + URLEncoder.encode("session", "UTF-8")
                + "=" + URLEncoder.encode("1", "UTF-8");

        Log.d("post_send_data", data);

        String text = "http://www.orce.uni.edu.pe/m/api/login.json.php";

        String[] params = {text,data};
        BufferedReader reader=null;

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            Log.d("post_params", params.toString());
            mAuthTask = new UserLoginTask();
            mAuthTask.execute(params);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
            String url_str = params[0];
            String data = params[1];
            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            Log.d("post_url",url_str);
            Log.d("post_data",data);
            try {
                // Defined URL  where to send data
                URL url = new URL(url_str);

                // Send POST data request
                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                //cm.setCookies(conn);
                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                conn.setUseCaches (false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                /*
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                conn.setDoOutput(true);
                */
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                // Get the server response

                DataInputStream reader = new DataInputStream(conn.getInputStream());
                StringBuffer sb = new StringBuffer();
                String line = null;
                cm.storeCookies(conn);
                Log.d("post_cookie", cm.toString());

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }

                String text = sb.toString();
                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                return text;

            } catch (MalformedURLException e) {
                Log.e("post_exception", e.getMessage());
            } catch (IOException e) {
                Log.e("post_exception", e.getMessage());
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                    return "true";
                }
            }

            // TODO: register the new account here.
            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            return "false";
        }

        @Override
        protected void onPostExecute(final String result) {

            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            Log.d("post_result",result);
            mAuthTask = null;
            showProgress(false);

            if (result != "" || result == "true") {
                try{
                    JSONObject myObject = new JSONObject(result);
                    Boolean error = (Boolean) myObject.get("error");
                    if (!error) {
                        Log.d("post_login", "CONGRATULATIONS!");
                        //@TODO Guardar Cookie para pedir datos o buscar otra forma
                        String data = URLEncoder.encode("CURSO", "UTF-8")
                                + "=" + URLEncoder.encode("GP515", "UTF-8");

                        data += "&" + URLEncoder.encode("SECCION", "UTF-8") + "="
                                + URLEncoder.encode("U", "UTF-8");
                        String[] params = {"http://www.orce.uni.edu.pe/m/api/notas.json.php",data};
                        mAuthTask = new UserLoginTask();
                        mAuthTask.execute(params);
                        finish();
                    }
                } catch (Exception e) {
                    Log.e("post_exception", e.getMessage());
                }
            } else {
                Log.d("post_login", "FAIL!");
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
            Log.d("post_cancel","CANCELED!");
        }
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e("post_exception", e.getMessage());
        }
        return "";
    }
}
