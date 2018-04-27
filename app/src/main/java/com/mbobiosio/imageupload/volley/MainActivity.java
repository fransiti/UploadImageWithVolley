package com.mbobiosio.imageupload.volley;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton UploadImage, ChooseImage ;
    TextView ImageName ;
    ImageView UploadImageView ;
    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap bitmap ;
    String image_name;
    String uploaded_image;
    private Uri mImageCaptureUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initilization();

        ChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageFile(v);
            }
        });

        UploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    image_name = ImageName.getText().toString();
                    uploaded_image = getStringImage(bitmap);

                    Log.d("UPLOAD IMAGE ::::::::", String.valueOf(uploaded_image.length()));
                    ApiTaskUploadImage ApiCall = new ApiTaskUploadImage(MainActivity.this);
                    ApiCall.execute(uploaded_image, image_name);
                } else {
                    //Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_LONG).show();
                    Snackbar.make(findViewById(android.R.id.content), "Please Connect Internet", Snackbar.LENGTH_LONG).show();
                }
//                if (UploadImageView.equals(""))  {
//                    Snackbar.make(findViewById(android.R.id.content), "Please Pick a New Image", Snackbar.LENGTH_LONG).show();
//                }
            }
        });
    }


    private void initilization(){
        UploadImage = (FloatingActionButton) findViewById(R.id.up_img);
        ChooseImage = (FloatingActionButton) findViewById(R.id.pick_Img);
        ImageName = (TextView) findViewById(R.id.imagename) ;
        UploadImageView = (ImageView) findViewById(R.id.uploadimage) ;
    }

    private void chooseImageFile(View view){
        CropImage.startPickImageActivity(this);
//        Intent intent = new Intent() ;
//        intent.setType("image/*") ;
//        intent.setAction(Intent.ACTION_GET_CONTENT) ;
//        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);



            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {

                mImageCaptureUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {

                startCropImageActivity(imageUri);
            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ((ImageView) findViewById(R.id.uploadimage)).setImageURI(result.getUri());
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "crop failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private String getStringImage(Bitmap bitmap){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
        byte[] imagebyte = byteArrayOutputStream.toByteArray();
        String encodeString = Base64.encodeToString(imagebyte, Base64.DEFAULT);
        return encodeString ;
    }
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private class ApiTaskUploadImage extends AsyncTask<String, Void, String>{

        Context context ;
        ProgressDialog progressDialog ;
        String upload_image_url = "http://yoururl.com/upload.php" ;

        ApiTaskUploadImage(Context context){
            this.context = context ;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context) ;
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Image Uploading...");
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(2);
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
          //  return isNetwork(params[0]);
            StringBuilder stringBuilder = null;

            try {
                URL url = new URL(upload_image_url) ;
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream() ;
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")) ;

                String image = params[0];
                String name = params[1];
                String data = URLEncoder.encode("image", "UTF-8")+"="+URLEncoder.encode(image, "UTF-8")+"&"+
                        URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(name, "UTF-8") ;

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();


                int responsecode = httpURLConnection.getResponseCode() ;
                if(responsecode == HttpURLConnection.HTTP_OK){
                    InputStream inputStream = httpURLConnection.getInputStream() ;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8")) ;
                    stringBuilder = new StringBuilder() ;
                    String line = "" ;
                    while((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line + "\n") ;
                    }
                }
                httpURLConnection.disconnect();

            }
            catch (MalformedURLException e) {
                Log.d("++++Malformed+++++", String.valueOf(e)) ;
            }
            catch (IOException e) {
                Log.d("++++IOException+++++", String.valueOf(e)) ;
            }

            return stringBuilder.toString().trim();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String json) {

            try {
                progressDialog.dismiss();
                JSONObject mainJSONobj = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1)) ;
                Log.d("mainJSONobj=====", String.valueOf(mainJSONobj)) ;

                String response = mainJSONobj.getString("response") ;
                Log.d("RESPONSE=====", response) ;

                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT) ;
            }
            catch (JSONException e) {
                Log.d("++++JSONException+++++", String.valueOf(e)) ;
            }
       }
    }
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }
}
