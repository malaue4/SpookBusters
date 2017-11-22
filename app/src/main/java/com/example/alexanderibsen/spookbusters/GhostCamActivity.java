package com.example.alexanderibsen.spookbusters;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.example.alexanderibsen.spookbusters.Objects.GhostSimple;
import com.google.gson.Gson;

import java.util.ArrayList;

public class GhostCamActivity extends AppCompatActivity implements Orientation.Listener {
    private final static String TAG = "SimpleCamera";
    private static final int MY_PERMISSIONS_CAMERA = 0;
    private CameraPreviewView cameraPreviewView = null;
    private Orientation orientation;
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
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.

        mGLView = findViewById(R.id.mGLView);

        mediaPlayer = MediaPlayer.create(this, R.raw.camera_flash_sound);

        GhostSimple[] ghostData;
        // using intent extra, as indicator and data transfer means
        if(getIntent().hasExtra("ghostData")){
            String ghostJson = getIntent().getStringExtra("ghostData");
            ghostData = new Gson().fromJson(ghostJson, GhostSimple[].class);
            for (GhostSimple ghost : ghostData) {
                mGLView.addGhost((float)ghost.lat*3, 0, (float)ghost.lng*3, ghost.id);
            }
        }
/*
        //using intent as indicator(possibly not needed) and shared preferences as data transfer means
        if(getIntent().hasExtra("ghostData")){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String ghostJson = sharedPreferences.getString("ghostsJson", "");
            ghostData = new Gson().fromJson(ghostJson, GhostSimple[].class);
        }*/
    }


    public void captureGhost(View view){
        for(Ghost3D ghost : mGLView.getGhosts()){
            float angleFast = mGLView.getWorld().getCamera().getDirection().calcAngleFast(ghost.getPosition());
            float distance = ghost.getPosition().length();
            if (angleFast <= 2 && distance < 8) {
                if(angleFast < 1 && distance < 6) {
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
        if(mGLView != null)
            mGLView.setRotation(yaw, pitch, roll);
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

    public void switchGhostTracker(View view){
        ArrayList<Integer> bustedSpooks = new ArrayList<>(0);
        for (Ghost3D ghost:mGLView.getGhosts()) {
            if(ghost.currentBehaviour == Ghost3D.GhostBehaviour.VANISH){
                bustedSpooks.add(ghost.id);
            }
        }

        Intent intent = new Intent(this, MapsActivity.class).putExtra("bustedSpooks", bustedSpooks);
        intent.putExtra("FromGhostCam", 0);
        startActivity(intent);
    }
}
