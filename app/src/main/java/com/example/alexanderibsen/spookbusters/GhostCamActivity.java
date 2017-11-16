package com.example.alexanderibsen.spookbusters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexanderibsen.spookbusters.GL.MyGLSurfaceView;

import java.util.Arrays;

public class GhostCamActivity extends AppCompatActivity implements Orientation.Listener {
    private final static String TAG = "SimpleCamera";
    private static final int MY_PERMISSIONS_CAMERA = 0;
    private CameraPreviewView cameraPreviewView = null;
    private Orientation orientation;
    private TextView textView;



    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //same as set-up android:screenOrientation="portrait" in <activity>, AndroidManifest.xml
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_ghost_cam);

        cameraPreviewView = findViewById(R.id.textureView);
        cameraPreviewView.setSurfaceTextureListener(cameraPreviewView.mySurfaceTextureListener);

        orientation = new Orientation(this);
        orientation.startListening(this);
        textView = findViewById(R.id.textView2);
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.

        mGLView = findViewById(R.id.mGLView);
    }

    public void gotoRenderView(View view){
        Intent intent = new Intent(this, GhostRenderActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mGLView.onPause();
        cameraPreviewView.onPause();
        orientation.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGLView != null)
            mGLView.onResume();
        orientation.startListening(this);
        cameraPreviewView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        orientation.stopListening();
    }

    @Override
    public void onOrientationChanged(float yaw, float pitch, float roll) {
        textView.setText(String.format("%s, %s, %s", yaw, pitch, roll));
        if(mGLView != null)
            ((MyGLSurfaceView)mGLView).setRotation(yaw, pitch, roll);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != MY_PERMISSIONS_CAMERA) {
            return;
        }
        if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate();
        }
    }
}
