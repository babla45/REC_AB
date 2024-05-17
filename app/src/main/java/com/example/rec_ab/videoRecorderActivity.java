package com.example.rec_ab;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            openCamera();
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
        releaseCamera();
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
                openCamera();
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

    private void openCamera() {
        camera = Camera.open();
        camera.setDisplayOrientation(90); // Set the orientation to portrait
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void prepareMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Use high quality settings from CamcorderProfile
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mediaRecorder.setProfile(profile);

        mediaRecorder.setOutputFile(getOutputMediaFile().toString());
        mediaRecorder.setOrientationHint(90); // Adjust the orientation hint

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
        releaseCamera();
        Toast.makeText(this, "video activity destroyed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(videoRecorderActivity.this, MainActivity.class);
        startActivity(intent);
    }
}