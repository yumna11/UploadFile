package com.example.urazayousuf.uploadfile;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity {

    private  String FILE_URL;
    private Button selectButton, uploadButton;
    private static final String TAG = "Proposal";
    private  EditText pdfNameEditText,username ;
    ProgressDialog progressDialog;
    Uri uri;
    private static String UPLOAD_HTTP_URL;
    public int DOCX_REQ_CODE = 1;

   private String docxNameHolder, docxPathHolder, docxID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AllowRunTimePermission();
        selectButton = (Button) findViewById(R.id.button);
        uploadButton = (Button) findViewById(R.id.button2);
        pdfNameEditText = (EditText) findViewById(R.id.editText);
        username = (EditText) findViewById(R.id.textview);
        Bundle extras = getIntent().getExtras();
        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        selectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                chooseFile();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // pdfUpload(FILE_URL);
                startActivity(new Intent(MainActivity.this,DownloadFile.class));
            }
        });

    }
     private void chooseFile(){
         new MaterialFilePicker()
                 .withActivity(this)
                 .withRequestCode(1)
                 // Filtering files and directories by file name using regexp
                 .withFilterDirectories(true) // Set directories filterable (false by default)
                 .withHiddenFiles(true) // Show hidden files and folders
                 .start();
         //startActivityForResult(Intent.createChooser(intent, "Select docx"), DOCX_REQ_CODE);
         Toast.makeText(MainActivity.this, "select.", Toast.LENGTH_LONG).show();
     }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOCX_REQ_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String selectedFileUri = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                Log.v("MainActivity","File uri is : "+selectedFileUri);

                if (selectedFileUri != null && ! selectedFileUri.equals("")) {
                    String encodedFile = encodeFileToBase64Binary(new File(selectedFileUri));
                    pdfUpload(new File(encodedFile));
                    pdfNameEditText.setText(FILE_URL);
                } else {
                    Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                }
            }
        }}

    private String encodeFileToBase64Binary(File file){
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = Base64.encodeToString(bytes,Base64.DEFAULT);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch <span id="IL_AD5" class="IL_AD">block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return encodedfile;
    }

    private void pdfUpload(File file)  {
        String userName = username.getText().toString();
        UPLOAD_HTTP_URL = "http://fypms.com/api/Fyp/PostProjectProposal?UserName="+userName+"&Url="+file+"&User_id=1";
        String cancel_req_tag = "cancel";
        Log.v("Username",userName);
        progressDialog.setMessage("uploading file please wait...");
        showDialog();

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    UPLOAD_HTTP_URL, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    hideDialog();
                    Log.v(MainActivity.TAG,"Response is "+response.toString());
                    Toast.makeText(getApplicationContext(), " Upload successfully", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(MainActivity.TAG,"The error is "+error.getMessage());
                    hideDialog();

                }
            }) {
            };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, cancel_req_tag);
    }


    public void AllowRunTimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
        {

            Toast.makeText(MainActivity.this,"READ_EXTERNAL_STORAGE permission Access Dialog", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] Result) {

        switch (RC) {

            case 1:

                if (Result.length > 0 && Result[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }


}
