package com.example.alexanderibsen.spookbusters.GL;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by marti on 29/10/2017.
 */

class MyGLRenderer implements GLSurfaceView.Renderer {

    Triangle triangle;
    Square square;
    private float yaw;
    private float pitch;
    private float roll;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f,0.0f,0.0f,0.0f);

        triangle = new Triangle();
        square = new Square();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //GLES20.glDisable(GLES20.GL_CULL_FACE);


        // Set the camera position (View matrix)
        float[] look = new float[]{
                (float) (Math.cos(yaw)*Math.cos(pitch)),
                -(float) Math.sin(pitch),
                (float) (Math.sin(yaw)*Math.cos(pitch))
        };
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, look[0], look[1], look[2], 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        float[] scratch = new float[16];
        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 4000L;
        //float angle = 0.090f * ((int) time);
        float angle = (float) (roll/Math.PI*-180);
        Matrix.setRotateM(mRotationMatrix, 0, angle, look[0], look[1], look[2]);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);


        // Draw shape
        //triangle.draw(mMVPMatrix);
        square.draw(scratch);
    }

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);

    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void setRotation(float yaw, float pitch, float roll) {

        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }
}
