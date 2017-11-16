package com.example.alexanderibsen.spookbusters;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.alexanderibsen.spookbusters.GL.MyGLSurfaceView;
import com.example.alexanderibsen.spookbusters.Objects.Ghost3D;

public class GhostCamActivity extends AppCompatActivity implements Orientation.Listener {
    private final static String TAG = "SimpleCamera";
    private static final int MY_PERMISSIONS_CAMERA = 0;
    private CameraPreviewView cameraPreviewView = null;
    private Orientation orientation;
    private TextView textView;
    private MediaPlayer mediaPlayer;

    private MyGLSurfaceView mGLView;
    private SurfaceView flashSurface;

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
        //flashSurface = findViewById(R.id.flashSurface);

        orientation = new Orientation(this);
        orientation.startListening(this);
        textView = findViewById(R.id.textView2);
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.

        mGLView = findViewById(R.id.mGLView);

        mediaPlayer = MediaPlayer.create(this, R.raw.camera_flash_sound);
    }

    public void gotoRenderView(View view){
        Intent intent = new Intent(this, GhostRenderActivity.class);
        startActivity(intent);
    }


    public void captureGhost(View view){
        for(Ghost3D ghost : mGLView.getGhosts()){
            float angleFast = mGLView.getWorld().getCamera().getDirection().calcAngleFast(ghost.getPosition());
            float distance = ghost.getPosition().length();
            if (angleFast <= 2 && distance < 5) {
                if(angleFast < 1 && distance < 3) {
                    ghost.capture();
                } else {
                    ghost.spook();
                }
            }
            Log.e("Spookbusters.G", String.valueOf(angleFast) + ", " + distance);
        }
        cameraPreviewView.takePicture();
        mediaPlayer.start();
        mGLView.flashValue = 1;
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
