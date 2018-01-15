package com.example.android.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.myapplication.models.ServerResponseData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.android.myapplication.Helpers.ACCESS_TOKEN_ID;
import static com.example.android.myapplication.Helpers.BASE_URL;
import static com.example.android.myapplication.Helpers.CenterToastMessage;

public class LoginActivity extends AppCompatActivity{

    private EditText mUserName, mPassword;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserName = (EditText) findViewById(R.id.txtUserName);
        mPassword = (EditText) findViewById(R.id.txtPassword);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Logging in. Please wait...");
        
        Button mLoginButton = (Button) findViewById(R.id.btnLogin);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserName = mUserName.getText().toString();
                String Pwd = mPassword.getText().toString();
                if (UserName.isEmpty()){
                    Toast toast = Toast.makeText(LoginActivity.this, "User Name can not be empty", Toast.LENGTH_SHORT);
                    CenterToastMessage(toast);
                    return;
                } else if (Pwd.isEmpty()){
                    Toast toast = Toast.makeText(LoginActivity.this, "Password can not be empty", Toast.LENGTH_SHORT);
                    CenterToastMessage(toast);
                    return;
                }

                String userAndPass = "{\"username\": \"" + UserName + "\",\"password\":\"" + Pwd + "\"}";
                new LogInTask().execute(BASE_URL + "/auth/login", userAndPass);
            }
        });

        View mLoginFormView = findViewById(R.id.login_form);
        View mProgressView = findViewById(R.id.login_progress);
    }

    public class LogInTask extends AsyncTask<String, String, ServerResponseData>{
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }
        
        @Override
        protected ServerResponseData doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            ServerResponseData serverResponseData = new ServerResponseData();

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                byte[] outputInBytes = params[1].getBytes("UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write( outputInBytes );
                os.close();
                connection.connect();

                int responseCode = connection.getResponseCode();
                serverResponseData.setResponseCode(responseCode);


                if (responseCode == 200) {
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    String finalJson = buffer.toString();
                    JSONObject jsonObject = new JSONObject(finalJson);
                    serverResponseData.setMessage(jsonObject.getString("access_token"));
                } else {
                    serverResponseData.setMessage("User not found");
                }

                return serverResponseData;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null){
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return serverResponseData;
        }

        @Override
        protected void onPostExecute(ServerResponseData result) {
            super.onPostExecute(result);
            dialog.dismiss();
            int responseCode = result.getResponseCode();
            if (responseCode == 200){
                Toast.makeText(LoginActivity.this, "You're Log in successfully", Toast.LENGTH_SHORT).show();
                Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
                MainIntent.putExtra(ACCESS_TOKEN_ID, result.getMessage());
                startActivity(MainIntent);
            } else {
                Toast.makeText(LoginActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

