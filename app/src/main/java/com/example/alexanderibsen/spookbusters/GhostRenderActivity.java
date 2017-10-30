package com.example.alexanderibsen.spookbusters;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class GhostRenderActivity extends AppCompatActivity implements Orientation.Listener{
//https://developer.android.com/training/graphics/opengl/environment.html


    private GLSurfaceView mGLView;
    private Orientation orientation;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost_render);

        orientation = new Orientation(this);
        orientation.startListening(this);
        textView = (TextView) findViewById(R.id.textView);
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.

        mGLView = new MyGLSurfaceView(this);
        addContentView(mGLView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    @Override
    public void onOrientationChanged(float yaw, float pitch, float roll) {
        textView.setText(yaw+", "+pitch+", "+roll);
        ((MyGLSurfaceView)mGLView).setRotation(yaw, pitch, roll);
    }
}
