package com.example.android.myapplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.myapplication.models.DataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.android.myapplication.Helpers.ACCESS_TOKEN_ID;
import static com.example.android.myapplication.Helpers.BASE_URL;
import static com.example.android.myapplication.Helpers.DATA_ID;
import static com.example.android.myapplication.Helpers.DATA_SUMMARY;

public class DetailActivity extends AppCompatActivity {

    private ImageView originalImageView;
    private TextView detailTextView;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading. Please wait...");

        originalImageView = (ImageView) findViewById(R.id.imageViewOriginal);
        detailTextView = (TextView)findViewById(R.id.txtDetailView);

        Bundle extras = getIntent().getExtras();
        String strToken = extras.getString(ACCESS_TOKEN_ID);
        Integer strDataId = extras.getInt(DATA_ID);
        String strDataSummary = extras.getString(DATA_SUMMARY);
        getSupportActionBar().setTitle(strDataSummary);

        new GetDataDetail().execute(BASE_URL + "/data/" + strDataId.toString(), strToken);
    }

    public class GetDataDetail extends AsyncTask<String, String, DataModel> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected DataModel doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Access-Token", params[1]);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                String finalJson = buffer.toString();
                JSONObject finalObject = new JSONObject(finalJson);

                DataModel dataModel = new DataModel();
                dataModel.setId(Integer.parseInt(finalObject.getString("id")));
                dataModel.setSummary(finalObject.getString("detail"));
                dataModel.setThumbnail_url(finalObject.getString("original_url"));

                return dataModel;

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
            return null;
        }

        @Override
        protected void onPostExecute(DataModel result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Helpers.DownloadImageWithURLTask downloadTask = new Helpers.DownloadImageWithURLTask(originalImageView);
            downloadTask.execute(result.getThumbnail_url());
            detailTextView.setText(result.getSummary());
        }
    }
}
