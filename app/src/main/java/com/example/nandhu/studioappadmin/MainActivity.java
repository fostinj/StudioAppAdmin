package com.example.nandhu.studioappadmin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Button choose,upload;
    ImageView imageView;
    boolean photochoosed=false;
    EditText caption;
    private final static int RESULT_SELECT_IMAGE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String TAG = "GalleryUtil";
    private final int GALLERY_ACTIVITY_CODE = 200;
    private final int RESULT_CROP = 400;
    String mCurrentPhotoPath;
    File photoFile = null;
    String encoded="";
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        choose=(Button) findViewById(R.id.choose);
        upload=(Button) findViewById(R.id.save);
        imageView=(ImageView) findViewById(R.id.imageView);
        caption=(EditText) findViewById(R.id.caption);

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (encoded==""|| caption.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Caption or image is missing cant upload", Toast.LENGTH_SHORT).show();
                }else{
                    final ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("Uploading");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    JSONObject object=new JSONObject();
                    try {
                        object.put("caption",caption.getText().toString());
                        object.put("photo",encoded);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest objectRequest=new JsonObjectRequest(Request.Method.POST, "https://conserving-gravel.000webhostapp.com/misc/fostin/upload.php", object, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getString("status").equalsIgnoreCase("true")){
                                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    caption.setText("");
                                    imageView.setImageResource(R.drawable.img);
                                }else if (response.getString("status").equalsIgnoreCase("false")){
                                    Toast.makeText(MainActivity.this, "Uploading failed", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                        }
                    });
                    AppController.getInstance().addToRequestQueue(objectRequest);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent GalIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(Intent.createChooser(GalIntent, "Select Image From Gallery"), MEDIA_TYPE_IMAGE);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEDIA_TYPE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();


            Bitmap bitmap  = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ajce);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArrayImage = baos.toByteArray();
            encoded = Base64.encodeToString(byteArrayImage, Base64.NO_WRAP);
            System.out.println("image="+Base64.encodeToString(byteArrayImage, Base64.NO_WRAP));
            imageView.setImageBitmap(bitmap);
            /*} catch (IOException e) {
                e.printStackTrace();
            }*/



            //
        }
    }

}
