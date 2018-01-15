package com.example.android.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.myapplication.models.FileInfo;
import com.example.android.myapplication.models.ServerResponseData;
import com.example.android.myapplication.remote.APIUtils;
import com.example.android.myapplication.remote.FileService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.android.myapplication.Helpers.ACCESS_TOKEN_ID;
import static com.example.android.myapplication.Helpers.BASE_URL;
import static com.example.android.myapplication.Helpers.CenterToastMessage;

public class UploadActivity extends AppCompatActivity {

    private FileService fileService;
    private String imagePath;
    private String strToken;

    private ImageView ivPreview;
    private EditText etSummary, etDetail;
    private final Integer SELECT_FILE = 0;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        getSupportActionBar().setTitle("Create");

        Button btnChooseFile = (Button) findViewById(R.id.btnChooseFile);
        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        fileService = APIUtils.getFileService();

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading. Please wait...");

        strToken = getIntent().getStringExtra("ACCESS_TOKEN_ID");

        ivPreview = (ImageView)findViewById(R.id.imageViewUpload);
        etSummary = (EditText) findViewById(R.id.etSummary);
        etDetail = (EditText) findViewById(R.id.etDetail);

        btnChooseFile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String Summary = etSummary.getText().toString();
                final String Detail = etDetail.getText().toString();
                if (imagePath == null){
                    Toast toast = Toast.makeText(UploadActivity.this, "Image can not be empty", Toast.LENGTH_SHORT);
                    CenterToastMessage(toast);
                    return;
                } else if (Summary.isEmpty()){
                    Toast toast = Toast.makeText(UploadActivity.this, "Summary can not be empty", Toast.LENGTH_SHORT);
                    CenterToastMessage(toast);
                    return;
                } else if (Detail.isEmpty()){
                    Toast toast = Toast.makeText(UploadActivity.this, "Detail can not be empty", Toast.LENGTH_SHORT);
                    CenterToastMessage(toast);
                    return;
                }
                dialog.show();

                File file = new File(imagePath);
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

                Call<FileInfo> call = fileService.upload(strToken, body);
                call.enqueue(new Callback<FileInfo>() {
                    @Override
                    public void onResponse(Call<FileInfo> call, Response<FileInfo> response) {
                        if (response.isSuccessful()){
                            Integer DataId = response.body().getId();
                            StringBuilder dataDetail = new StringBuilder("{\"summary\": \"" + Summary + "\",\"detail\":\"" + Detail + "\"}");
                            new UpdateDataDetail().execute(BASE_URL + "/data/" + DataId.toString() + "/update", dataDetail.toString());
                        } else {
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                dialog.dismiss();
                                Toast toast = Toast.makeText(UploadActivity.this, jObjError.getString("message"), Toast.LENGTH_SHORT);
                                CenterToastMessage(toast);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<FileInfo> call, Throwable t) {
                        dialog.dismiss();
                        Toast.makeText(UploadActivity.this, "ERROR" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public class UpdateDataDetail extends AsyncTask<String, String, ServerResponseData> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ServerResponseData doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Access-Token", strToken);
                connection.setRequestProperty("Content-Type", "application/json");
                byte[] outputInBytes = params[1].getBytes("UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write( outputInBytes );
                os.close();
                connection.connect();

                ServerResponseData serverResponseData = new ServerResponseData();
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
                    serverResponseData.setMessage("Upload Success");
                } else {
                    serverResponseData.setMessage("Upload not successfully");
                }

                return serverResponseData;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
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
        protected void onPostExecute(ServerResponseData result) {
            super.onPostExecute(result);
            dialog.dismiss();
            int responseCode = result.getResponseCode();
            if (responseCode == 200){
                Toast.makeText(UploadActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                Intent MainIntent = new Intent(UploadActivity.this, MainActivity.class);
                MainIntent.putExtra(ACCESS_TOKEN_ID, strToken);
                startActivity(MainIntent);
            } else {
                Toast.makeText(UploadActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SelectImage(){

        final CharSequence[] items = {"Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Gallery")) {

                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (data == null) {
                Toast.makeText(this, "Unable to choose image!", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
            imagePath = getRealPathFromUrl(imageUri);
        }
    }

    private String getRealPathFromUrl(Uri uri){
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_idx);
        cursor.close();
        return result;
    }
}
