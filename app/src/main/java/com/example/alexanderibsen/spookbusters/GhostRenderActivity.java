package com.example.alexanderibsen.spookbusters;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alexanderibsen.spookbusters.GL.MyGLSurfaceView;

public class GhostRenderActivity extends Activity implements Orientation.Listener{
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

        mGLView = new MyGLSurfaceView(this, this);
        addContentView(mGLView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    @Override
    protected void onStop() {
        super.onStop();
        orientation.stopListening();
    }

    @Override
    public void onOrientationChanged(float yaw, float pitch, float roll) {
        textView.setText(yaw+", "+pitch+", "+roll);
        ((MyGLSurfaceView)mGLView).setRotation(yaw, pitch, roll);
    }
}
