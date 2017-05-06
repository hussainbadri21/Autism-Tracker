package com.example.hussain.autismtracker;



        import android.Manifest;
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

        import com.androidhiddencamera.CameraConfig;
        import com.androidhiddencamera.CameraError;
        import com.androidhiddencamera.HiddenCameraService;
        import com.androidhiddencamera.HiddenCameraUtils;
        import com.androidhiddencamera.config.CameraFacing;
        import com.androidhiddencamera.config.CameraImageFormat;
        import com.androidhiddencamera.config.CameraResolution;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.util.Timer;
        import java.util.TimerTask;

/**
 * Created by Keval on 11-Nov-16.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */

public class DemoCamService extends HiddenCameraService {
    int i;
    private Timer mTimer = null;
    private Handler mHandler = new Handler();
    SharedPreferences sharedPreferences;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences=DemoCamService.this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        //Toast.makeText(getApplicationContext(),"Smile!",Toast.LENGTH_SHORT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                CameraConfig cameraConfig = new CameraConfig()
                        .getBuilder(this)
                        .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.LOW_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        .build();

                startCamera(cameraConfig);

                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ffs","Runnable");

                        takePicture();

                    }
                }, 1000);
            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {
            //TODO Ask your parent activity for providing runtime permission
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        //Log.e("i",String.valueOf(i));

            Log.i("Image capture1", encodedImage);
            Log.e("Image Path",imageFile.getPath());
        i = Integer.parseInt(sharedPreferences.getString("i","-1"));
        if(i==0) {
            Log.d("bc", "0");

            sharedPreferences.edit().putString("encodedImage", encodedImage).apply();
        }
        else
            if(i==1){
                Log.d("bc", "1");

                sharedPreferences.edit().putString("encodedImage2",encodedImage).apply();}
        else
            if(i==2) {
                Log.d("bc", "2");

                sharedPreferences.edit().putString("encodedImage3", encodedImage).apply();
            }



        //Log.i("Image capture", encodedImage);
      //  Log.e("Image Path",imageFile.getPath());



            File fdelete = new File(imageFile.getPath());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    Log.e("File Deleted", imageFile.getPath());


                    if (mTimer != null ) {
                        mTimer.cancel();
                        //   sharedPreferences.edit().putString("i","4").apply();
                    } else {
                        // recreate new
                        mTimer = new Timer();
                        i += 1;
                        sharedPreferences.edit().putString("i", String.valueOf(i)).apply();
                    }
                    // schedule task
                    mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, 30 * 1000);
                } else {
                    Log.e("File not Deleted", imageFile.getPath());
                }
            }



        bitmap.recycle();
        bitmap=null;
        imageBytes = null;
       stopSelf();
    }

    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, "Cannot open camera.", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, "Cannot write image captured by camera.", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, "Camera permission not available.", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, "Your device does not have front camera.", Toast.LENGTH_LONG).show();
                break;
        }

        stopSelf();
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // display toast
                    //Toast.makeText(getApplicationContext(), "Timer Run",Toast.LENGTH_SHORT).show();
                    // if(sharedPreferences.getBoolean("cameraValue",false))


                    i = Integer.parseInt(sharedPreferences.getString("i","-1"));
                    Log.e("i ki value",String.valueOf(i));
                    if(i>=3)
                        stopService(new Intent(DemoCamService.this, DemoCamService.class));
                    else


                        startService(new Intent(DemoCamService.this, DemoCamService.class));
                    //sendData();
                }

            });
        }
    }
}
