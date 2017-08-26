package com.example.hussain.autismtracker;



import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Keval on 11-Nov-16.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */

public class DemoCamService extends HiddenCameraService {

    public long NOTIFY_INTERVAL;
    SharedPreferences sharedpreferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                CameraConfig cameraConfig = new CameraConfig()
                        .getBuilder(this)
                        .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                        .setImageRotation(270)
                        .setCameraResolution(CameraResolution.LOW_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        .build();

                startCamera(cameraConfig);
               /* Log.e("ffs","started");
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Log.e("ffs","middle1");
                        takePicture();
                        Log.e("ffs","middle2");
                    }
                },0,10000);
                Log.e("ffs","ended");*/
               //takePicture();
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        takePicture();
                    }
                }, 5000);


            }
            else {

                //Opens settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {
            //TODO Ask your parent activity for providing runtime permission
            Log.e("Camera","Cannot permission not available");

        }
        return START_NOT_STICKY;
    }

    /**
     * Converts clicked imagee to Base64 String
     * and deletes the image from the device
     * @param imageFile-Clicked image
     */

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        sharedpreferences.edit().putString("encodedImage",encodedImage).apply();

        Log.i("Image capture", encodedImage);
        Log.e("Image Path",String.valueOf(encodedImage.length()));
        File fdelete = new File(imageFile.getPath());
        if (fdelete.exists()) {
            senddata();
            if (fdelete.delete()) {
                Log.e("File Deleted",imageFile.getPath());
            } else {
                Log.e("File not Deleted",imageFile.getPath());
            }
        }

        bitmap.recycle();
        bitmap=null;
        imageBytes = null;
        stopSelf();
    }

    /**
     * Handles various exceptions and errors
     * that might occur
     * @param errorCode
     */
    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                /*Camera open failed. Probably because another application
                is using the camera*/
                Log.e("Camera","Cannot open camera");

                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                /*camera permission is not available
                Ask for the camra permission before initializing it.*/
                Log.e("Camera","Permission not available");

                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                /*Display information dialog to the user with steps to grant "Draw over other app"
                  permission for the app.*/
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Log.e("Camera","No front camera");

                break;
        }
        stopSelf();
    }


    public void senddata()
    {
        String url = "http://192.168.43.66:3000/user/detect";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            try {
                                JSONObject person = new JSONObject(response);
                                boolean ct = person.getBoolean("status");
                                Log.e("ct value", Boolean.toString(ct));
                                if (ct) {
                                    String msg = person.getString("msg");
                                    Log.e("msg",msg);
                                    Log.e("img", "image sent");

                                } else {
                                    Log.e("img2", "image not sent");
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("Error", e.getMessage());
                            }
                            System.out.println(response);
                            //stopService(new Intent(DemoCamService.this, DemoCamService.class));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null && error.getMessage() !=null){
                    Toast.makeText(getApplicationContext(),"error VOLLEY "+error.getMessage(),Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_LONG).show();

                }
                //mTextView.setText("That didn't work!");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                String authkey = sharedpreferences.getString("authKey", "");
                String img1 = sharedpreferences.getString("encodedImage", "");
                //String img2 = sharedPreferences.getString("encodedImage1", null);
                //String img3 = sharedPreferences.getString("encodedImage2", null);
                params.put("authKey", authkey);
                params.put("image",img1);
                //params.put("img2",img2);
                //params.put("img3",img3);
                Log.i("params of my service", params.toString());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(DemoCamService.this);
        requestQueue.add(stringRequest);
    }
}