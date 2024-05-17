package com.example.rec_ab;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.PixelFormat;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.File;
import java.io.IOException;


public class videoRecorderActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final int REQUEST_PERMISSION_CODE = 100;
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder = null;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button recordButton;
    private boolean isSurfaceReady = false;
    private boolean pendingStartRecording = false;
    private static final int RECORD_DURATION = 10000; // 10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_recorder);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        surfaceHolder.setKeepScreenOn(true);

        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                } else {
                    if (checkPermission()) startRecording();
                    else requestPermission();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "on resume called", Toast.LENGTH_SHORT).show();

        if (isSurfaceReady && checkPermission()) {
            recordButton.performClick();
        } else {
            pendingStartRecording = true;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isSurfaceReady = true;
        if (checkPermission()) {
            prepareMediaRecorder();
            if (pendingStartRecording) {
                recordButton.performClick();
                pendingStartRecording = false;
            }
        } else {
            requestPermission();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isSurfaceReady = false;
    }

    private boolean checkPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        int audioPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(videoRecorderActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                prepareMediaRecorder();
                if (isSurfaceReady) {
                    recordButton.performClick();
                    pendingStartRecording = false;
                }
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void prepareMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setOutputFile(getOutputMediaFile().toString());
        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setVideoSize(1280, 720);

        try {
            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            recordButton.setText("Record again");
            finish();
        }
    }

    private void startRecording() {
        if (mediaRecorder == null) {
            prepareMediaRecorder();
        }
        if (mediaRecorder != null) {
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show();
            recordButton.setText("Stop");

            // Stop recording after 10 seconds
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecording();
                }
            }, RECORD_DURATION);
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyVideos2");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        return new File(mediaStorageDir.getPath() + File.separator + "BIB2__" + System.currentTimeMillis() + "_.mp4");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "video activity destroyed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(videoRecorderActivity.this, MainActivity.class);
        startActivity(intent);
    }
}





//
//public class videoRecorderActivity extends AppCompatActivity implements SurfaceHolder.Callback {
//
//    private static final int REQUEST_PERMISSION_CODE = 100;
//    private boolean isRecording = false;
//    private MediaRecorder mediaRecorder = null;
//    private SurfaceView surfaceView;
//    private SurfaceHolder surfaceHolder;
//    private Button recordButton;
//    private boolean isSurfaceReady = false;
//    private boolean pendingStartRecording = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_video_recorder);
//
//        surfaceView = findViewById(R.id.surfaceView);
//        surfaceHolder = surfaceView.getHolder();
//        surfaceHolder.addCallback(this);
//
//        surfaceHolder.setKeepScreenOn(true);
//
//        recordButton = findViewById(R.id.recordButton);
//        recordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isRecording) {
//                    stopRecording();
//                } else {
//                    if (checkPermission()) startRecording();
//                    else requestPermission();
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        Toast.makeText(this, "on resume called", Toast.LENGTH_SHORT).show();
//
//        if (isSurfaceReady && checkPermission()) {
//            recordButton.performClick();
//        } else {
//            pendingStartRecording = true;
//        }
//    }
//
//    @Override
//    public void surfaceCreated(@NonNull SurfaceHolder holder) {
//        isSurfaceReady = true;
//        if (checkPermission()) {
//            prepareMediaRecorder();
//            if (pendingStartRecording) {
//                recordButton.performClick();
//                pendingStartRecording = false;
//            }
//        } else {
//            requestPermission();
//        }
//    }
//
//    @Override
//    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}
//
//    @Override
//    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//        isSurfaceReady = false;
//    }
//
//    private boolean checkPermission() {
//        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
//        int audioPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
//        return cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestPermission() {
//        ActivityCompat.requestPermissions(videoRecorderActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                prepareMediaRecorder();
//                if (isSurfaceReady) {
//                    recordButton.performClick();
//                    pendingStartRecording = false;
//                }
//            } else {
//                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void prepareMediaRecorder() {
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mediaRecorder.setOutputFile(getOutputMediaFile().toString());
//        mediaRecorder.setOrientationHint(90);
//        mediaRecorder.setVideoSize(1280, 720);
//        mediaRecorder.setMaxDuration(10000);
//
//        try {
//            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
//            mediaRecorder.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void stopRecording() {
//        if (mediaRecorder != null) {
//            Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null;
//            isRecording = false;
//            recordButton.setText("Record again");
//            finish();
//        }
//    }
//
//    private void startRecording() {
//        if (mediaRecorder == null) {
//            prepareMediaRecorder();
//        }
//        if (mediaRecorder != null) {
//            mediaRecorder.start();
//            isRecording = true;
//            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show();
//            recordButton.setText("Stop");
//        }
//    }
//
//    private File getOutputMediaFile() {
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyVideos2");
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                return null;
//            }
//        }
//        return new File(mediaStorageDir.getPath() + File.separator + "BIB2__" + System.currentTimeMillis() + "_.mp4");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Toast.makeText(this, "video activity destroyed", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(videoRecorderActivity.this, MainActivity.class);
//        startActivity(intent);
//    }
//}

/////////a1st solution by gpt





//package com.example.rec_ab;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Camera;
//import android.graphics.PixelFormat;
//import android.media.CamcorderProfile;
//import android.media.MediaRecorder;
//import android.opengl.Matrix;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import java.io.File;
//import java.io.IOException;
//
//public class videoRecorderActivity extends AppCompatActivity implements SurfaceHolder.Callback {
//
//    private static final int REQUEST_PERMISSION_CODE = 100;
//    private boolean isRecording = false;
//    private MediaRecorder mediaRecorder=null;
//    private SurfaceView surfaceView;
//    private SurfaceHolder surfaceHolder;
//    private Button recordButton;
//    private Bundle bundle;
//    private String key="";
//
////    ------
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_video_recorder);
//
//        //----------------------//
//
//        surfaceView = findViewById(R.id.surfaceView);
//        surfaceHolder = surfaceView.getHolder();
//        surfaceHolder.addCallback(this);
//
////==========================================================================
//        //surfaceHolder.setFormat(PixelFormat.TRANSPARENT); // If transparency is needed
//        surfaceHolder.setKeepScreenOn(true); // Keep the screen on while recording
//
////////////////////////////////bundle
//       // bundle=getIntent().getExtras();
////
////        if(bundle!=null){
////            key=bundle.getString("1_1");
////        }
//
////        if(key.equals("1_11"))
////        {
////            if (isRecording)
////            {
////                stopRecording();
////            }
////            else
////            {
////                if (checkPermission()) startRecording();
////                else requestPermission();
////            }
//////            Intent intent=new Intent(videoRecorderActivity.this,MainActivity.class);
//////            startActivity(intent);
////            Toast.makeText(this, "got it bib", Toast.LENGTH_SHORT).show();
////            //finish();
////        }
//
//        recordButton = findViewById(R.id.recordButton);
//        recordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isRecording) {
//                    stopRecording();
//                }
//                else
//                {
//                    if (checkPermission()) startRecording();
//                    else requestPermission();
//                }
//            }
//        });
//
//
//
//
//        /////////////////
////        recordButton.performClick();
////        =======================/
//
////        if (isRecording) {
////            stopRecording();
////        }
////        else
////        {
////            if (checkPermission()) startRecording();
////            else requestPermission();
////        }
//        ////////////new
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//
//        Toast.makeText(this, "on resume5000 called", Toast.LENGTH_SHORT).show();
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    //-----
//    @Override
//    public void surfaceCreated(@NonNull SurfaceHolder holder) {
//        if (checkPermission()) {
//
//            prepareMediaRecorder();
//        } else {
//            requestPermission();
//        }
//    }
//
//    @Override
//    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//
//    }
//
//    private boolean checkPermission() {
//       // Toast.makeText(this, "Checking permission", Toast.LENGTH_SHORT).show();
//        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
//        int audioPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
//        return cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestPermission() {
//        //Toast.makeText(this, "Requesting permission", Toast.LENGTH_SHORT).show();
//        ActivityCompat.requestPermissions(videoRecorderActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                prepareMediaRecorder();
//            } else {
//                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//
//    private void prepareMediaRecorder() {
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mediaRecorder.setOutputFile(getOutputMediaFile().toString());
//        // Set the orientation hint to landscape
//        mediaRecorder.setOrientationHint(90); // 0 for landscape
//
//        // Set the desired video size here (720x1280)
//
//        mediaRecorder.setVideoSize(1280,720);
//
//
//        try {
//            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
//            mediaRecorder.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
//
//
//    private void stopRecording() {
//        if (mediaRecorder != null) {
//            Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null; // Release the MediaRecorder instance
//            isRecording = false;
//
//            recordButton.setText("Record again");
//
//            ///////////////
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            finish();
//            //new
//        }
//    }
//
//    private void startRecording() {
//        if (mediaRecorder == null) {
//            // Reinitialize the MediaRecorder instance
//            prepareMediaRecorder();
//        }
//        if (mediaRecorder != null) {
//            mediaRecorder.start();
//            isRecording = true;
//            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show();
//            recordButton.setText("Stop");
//        }
//    }
//
//
//
//
////    =================================
//
//    private File getOutputMediaFile() {
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyVideos2");
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                return null;
//            }
//        }
//       // Toast.makeText(this, "Setting path...", Toast.LENGTH_SHORT).show();
//        return new File(mediaStorageDir.getPath() + File.separator + "BIB2__" + System.currentTimeMillis() + "_.mp4");
//    }
//
//    //////
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Toast.makeText(this, "vide activity destroyed", Toast.LENGTH_SHORT).show();
//        Intent intent=new Intent(videoRecorderActivity.this, MainActivity.class);
//        startActivity(intent);
//
//    }
//}
//
//
///*
//package com.example.rec_ab;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Camera;
//import android.graphics.PixelFormat;
//import android.media.CamcorderProfile;
//import android.media.MediaRecorder;
//import android.opengl.Matrix;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import java.io.File;
//import java.io.IOException;
//
//public class videoRecorderActivity extends AppCompatActivity implements SurfaceHolder.Callback {
//
//    private static final int REQUEST_PERMISSION_CODE = 100;
//    private boolean isRecording = false;
//    private MediaRecorder mediaRecorder=null;
//    private SurfaceView surfaceView;
//    private SurfaceHolder surfaceHolder;
//    private Button recordButton;
//    private Bundle bundle;
//    private String key="";
//
////    ------
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_video_recorder);
//
//        //----------------------//
//
//        surfaceView = findViewById(R.id.surfaceView);
//        surfaceHolder = surfaceView.getHolder();
//        surfaceHolder.addCallback(this);
//
//
////==========================================================================
//
//
//
//
//        //surfaceHolder.setFormat(PixelFormat.TRANSPARENT); // If transparency is needed
//        surfaceHolder.setKeepScreenOn(true); // Keep the screen on while recording
//
//
//
////////////////////////////////bundle
//       // bundle=getIntent().getExtras();
////
////        if(bundle!=null){
////            key=bundle.getString("1_1");
////        }
//
//        if(key.equals("1_11"))
//        {
//            if (isRecording)
//            {
//                stopRecording();
//            }
//            else
//            {
//                if (checkPermission()) startRecording();
//                else requestPermission();
//            }
////            Intent intent=new Intent(videoRecorderActivity.this,MainActivity.class);
////            startActivity(intent);
//            Toast.makeText(this, "got it bib", Toast.LENGTH_SHORT).show();
//            //finish();
//        }
//
//
//
//
//        recordButton = findViewById(R.id.recordButton);
//        recordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isRecording) {
//                    stopRecording();
//                }
//                else
//                {
//                    if (checkPermission()) startRecording();
//                    else requestPermission();
//                }
//            }
//        });
//
//    }
//
////-----
//
//
//
//
//    @Override
//    public void surfaceCreated(@NonNull SurfaceHolder holder) {
//        if (checkPermission()) {
//
//            prepareMediaRecorder();
//        } else {
//            requestPermission();
//        }
//    }
//
//    @Override
//    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//
//    }
//
//    private boolean checkPermission() {
//       // Toast.makeText(this, "Checking permission", Toast.LENGTH_SHORT).show();
//        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
//        int audioPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
//        return cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestPermission() {
//        //Toast.makeText(this, "Requesting permission", Toast.LENGTH_SHORT).show();
//        ActivityCompat.requestPermissions(videoRecorderActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                prepareMediaRecorder();
//            } else {
//                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//
//    private void prepareMediaRecorder() {
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mediaRecorder.setOutputFile(getOutputMediaFile().toString());
//        // Set the orientation hint to landscape
//        mediaRecorder.setOrientationHint(90); // 0 for landscape
//
//        // Set the desired video size here (720x1280)
//
//        mediaRecorder.setVideoSize(1280,720);
//
//        try {
//            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
//            mediaRecorder.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
//
//
//    private void stopRecording() {
//        if (mediaRecorder != null) {
//            Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null; // Release the MediaRecorder instance
//            isRecording = false;
//
//            recordButton.setText("Record again");
//        }
//    }
//
//    private void startRecording() {
//        if (mediaRecorder == null) {
//            // Reinitialize the MediaRecorder instance
//            prepareMediaRecorder();
//        }
//        if (mediaRecorder != null) {
//            mediaRecorder.start();
//            isRecording = true;
//            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show();
//            recordButton.setText("Stop");
//        }
//    }
//
//
//
//
////    =================================
//
//    private File getOutputMediaFile() {
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyVideos2");
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                return null;
//            }
//        }
//       // Toast.makeText(this, "Setting path...", Toast.LENGTH_SHORT).show();
//        return new File(mediaStorageDir.getPath() + File.separator + "BIB2__" + System.currentTimeMillis() + "_.mp4");
//    }
//
//
//
//}
//
//*/