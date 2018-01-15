package com.example.android.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.myapplication.models.DataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.myapplication.Helpers.ACCESS_TOKEN_ID;
import static com.example.android.myapplication.Helpers.BASE_URL;
import static com.example.android.myapplication.Helpers.DATA_ID;
import static com.example.android.myapplication.Helpers.DATA_SUMMARY;

public class MainActivity extends AppCompatActivity {
    private String strToken;
    private ListView lvData;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("List Data");


        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading. Please wait...");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent UploadIntent = new Intent(MainActivity.this, UploadActivity.class);
                    UploadIntent.putExtra(ACCESS_TOKEN_ID, strToken);
                    startActivity(UploadIntent);
                }
            });

        lvData = (ListView)findViewById(R.id.lvData);
        strToken = getIntent().getStringExtra(ACCESS_TOKEN_ID);
        new GetDataList().execute(BASE_URL + "/data", strToken);
        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataAdapter adapter = (DataAdapter) parent.getAdapter();
                Integer dataId = adapter.dataModelList.get(position).getId();
                String dataSummary = adapter.dataModelList.get(position).getSummary();
                Intent DetailIntent = new Intent(MainActivity.this, DetailActivity.class);
                Bundle extras = new Bundle();
                extras.putString(ACCESS_TOKEN_ID, strToken);
                extras.putString(DATA_SUMMARY, dataSummary);
                extras.putInt(DATA_ID, dataId);
                DetailIntent.putExtras(extras);
                startActivity(DetailIntent);
            }
        });
    }

    public void refresh(View view) {
        new GetDataList().execute(BASE_URL + "/data", strToken);
    }

    public class GetDataList extends AsyncTask<String, String, List<DataModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected List<DataModel> doInBackground(String... params) {
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
                String finalJson = "{\"data\":" + buffer.toString() + "}";
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("data");

                List<DataModel> dataModelList = new ArrayList<>();
                for (int i = 0; i < parentArray.length(); i++) {
                    DataModel dataModel = new DataModel();
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    dataModel.setId(Integer.parseInt(finalObject.getString("id")));
                    dataModel.setSummary(finalObject.getString("summary"));
                    dataModel.setThumbnail_url(finalObject.getString("thumbnail_url"));
                    dataModelList.add(dataModel);
                }

                return dataModelList;

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
            protected void onPostExecute(List<DataModel> result) {
                super.onPostExecute(result);
                dialog.dismiss();
                DataAdapter dataAdapter = new DataAdapter(getApplicationContext(), R.layout.row, result);
                lvData.setAdapter(dataAdapter);
        }
    }

    public class DataAdapter extends ArrayAdapter {
        private List<DataModel> dataModelList;
        private int resource;
        private LayoutInflater inflater;
        public DataAdapter(@NonNull Context context, int resource, @NonNull List<DataModel> objects) {
            super(context, resource, objects);
            dataModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();

            if (convertView == null) {
                convertView = inflater.inflate(resource, null);
                holder.ivDataIcon = (ImageView)convertView.findViewById(R.id.imageView);
                holder.tvDataSummary = (TextView)convertView.findViewById(R.id.txtTitle);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Helpers.DownloadImageWithURLTask downloadTask = new Helpers.DownloadImageWithURLTask(holder.ivDataIcon);
            downloadTask.execute(dataModelList.get(position).getThumbnail_url());

            holder.tvDataSummary.setText(dataModelList.get(position).getSummary());

            return convertView;
        }

        class ViewHolder {
            private ImageView ivDataIcon;
            private TextView tvDataSummary;
        }
    }
}
