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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private UserCourseTask mCourseTask = null;
    private UserScoreTask mScoreTask = null;

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
    private Button pyde_button;
    private Button clear_button;
    private EditText response_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.username);

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
        pyde_button = (Button) findViewById(R.id.pyde_button);
        pyde_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                courses();
            }
        });

        clear_button = (Button) findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                response_text.setText("");
                clear_button.setVisibility(0);
            }
        });

        response_text = (EditText) findViewById(R.id.response_text);
        response_text.setText("");
    }

    //@TODO Hacer que Froyo tambien pueda
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onResume() {
        super.onResume();
        cm = new CookieManager();
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cm);
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

        String text = getString(R.string.url_login);

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
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected String doInBackground(String... params) {
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
                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                conn.setUseCaches (false);

                //Connect
                conn.setDoInput(true);
                conn.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                // Get the server response

                DataInputStream reader = new DataInputStream(conn.getInputStream());
                StringBuffer sb = new StringBuffer();
                String line = null;
                Log.d("post_cookie", cm.toString());

                List<HttpCookie> cookies = cm.getCookieStore().getCookies();
                for (HttpCookie cookie: cookies)
                {
                    Log.d("post","Name = " + cookie.getName());
                    Log.d("post","Name = " + cookie.getName());
                    Log.d("post","Value = " + cookie.getValue());
                    Log.d("post","Lifetime (seconds) = " + cookie.getMaxAge());
                    Log.d("post","Path = " + cookie.getPath());
                }

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

            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            return "false";
        }

        @Override
        protected void onPostExecute(final String result) {

            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            Log.d("post_result",result);
            mAuthTask = null;
            showProgress(false);
            response_text.setText(response_text.getText()+"\n\n\n"+result);
            clear_button.setVisibility(1);

            if (result != "" || result == "true") {
                try{
                    JSONObject myObject = new JSONObject(result);
                    Boolean error = (Boolean) myObject.get("error");
                    if (!error) {
                        Log.d("post_login", "CONGRATULATIONS!");
                        pyde_button.setVisibility(1);
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

    public class UserCourseTask extends AsyncTask<String, Void, String> {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected String doInBackground(String... params) {
            String url_str = params[0];
            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            Log.d("post_url",url_str);
            try {

                // Defined URL  where to send data
                URL url = new URL(url_str);

                // Send GET request
                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                conn.setUseCaches (false);

                //Connect
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Get the server response

                DataInputStream reader = new DataInputStream(conn.getInputStream());
                StringBuffer sb = new StringBuffer();
                String line = null;
                Log.d("post_cookie", cm.toString());

                List<HttpCookie> cookies = cm.getCookieStore().getCookies();
                for (HttpCookie cookie: cookies)
                {
                    Log.d("post","Name = " + cookie.getName());
                    Log.d("post","Name = " + cookie.getName());
                    Log.d("post","Value = " + cookie.getValue());
                    Log.d("post","Lifetime (seconds) = " + cookie.getMaxAge());
                    Log.d("post","Path = " + cookie.getPath());
                }

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
            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            return "false";
        }

        @Override
        protected void onPostExecute(final String result) {

            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            Log.d("post_result", result);
            mCourseTask = null;
            showProgress(false);
            response_text.setText(response_text.getText()+"\nXXXXXXXXXXXXXXXXXXX\n"+result);
            clear_button.setVisibility(1);

            if (result != "" || result == "true") {
                try{
                    JSONObject myObject = new JSONObject(result);
                    Boolean error = (Boolean) myObject.get("error");
                    if (!error) {
                        Log.d("post_login", "CONGRATULATIONS!");

                        JSONObject data = (JSONObject) myObject.get("data");
                        JSONArray cursos = data.getJSONArray("cursos");
                        for (int i = 0; i < cursos.length(); i++) {
                            JSONObject curso = cursos.getJSONObject(i);
                            Log.d("post_condition",curso.getString("condicion"));
                            if (curso.getString("condicion").equals("N")) {
                                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                                String codigo = (String) curso.get("codigo");
                                String seccion = (String) curso.get("seccion");
                                score(codigo, seccion);
                            }
                            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                        }
                        pyde_button.setVisibility(1);
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
            mCourseTask = null;
            showProgress(false);
            Log.d("post_cancel","CANCELED!");
        }
    }

    public class UserScoreTask extends AsyncTask<String, Void, String> {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected String doInBackground(String... params) {
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
                Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                conn.setUseCaches (false);

                //Connect
                conn.setDoInput(true);
                conn.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                // Get the server response

                DataInputStream reader = new DataInputStream(conn.getInputStream());
                StringBuffer sb = new StringBuffer();
                String line = null;
                Log.d("post_cookie", cm.toString());

                List<HttpCookie> cookies = cm.getCookieStore().getCookies();
                for (HttpCookie cookie: cookies)
                {
                    Log.d("post","Name = " + cookie.getName());
                    Log.d("post","Name = " + cookie.getName());
                    Log.d("post","Value = " + cookie.getValue());
                    Log.d("post","Lifetime (seconds) = " + cookie.getMaxAge());
                    Log.d("post","Path = " + cookie.getPath());
                }

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

            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            return "false";
        }

        @Override
        protected void onPostExecute(final String result) {

            Log.d("post_line","" + new Exception().getStackTrace()[0].getLineNumber());
            Log.d("post_result",result);
            mScoreTask = null;
            showProgress(false);
            response_text.setText(response_text.getText()+"\n==================\n"+result);
            clear_button.setVisibility(1);

            if (result != "" || result == "true") {
                try{
                    JSONObject myObject = new JSONObject(result);
                    Boolean error = (Boolean) myObject.get("error");
                    if (!error) {
                        Log.d("post_login", "CONGRATULATIONS!");
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
            mScoreTask = null;
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

    //Obtener notas del curso PyDE
    public void score(String codigo, String seccion) throws UnsupportedEncodingException {
        String data = URLEncoder.encode("CURSO", "UTF-8")
                + "=" + URLEncoder.encode(codigo, "UTF-8");

        data += "&" + URLEncoder.encode("SECCION", "UTF-8") + "="
                + URLEncoder.encode(seccion, "UTF-8");
        String[] params = {getString(R.string.url_notas),data};
        mScoreTask = new UserScoreTask();
        mScoreTask.execute(params);
    }

    public void courses() {
        mCourseTask = new UserCourseTask();
        mCourseTask.execute(getString(R.string.url_cursos));
    }
}
