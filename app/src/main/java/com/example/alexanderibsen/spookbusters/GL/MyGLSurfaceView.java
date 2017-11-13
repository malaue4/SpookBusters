package com.example.alexanderibsen.spookbusters.GL;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.example.alexanderibsen.spookbusters.GhostCamActivity;
import com.example.alexanderibsen.spookbusters.R;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by marti on 29/10/2017.
 */

public class MyGLSurfaceView extends GLSurfaceView {

    public static Activity master = null;


    private final MyRenderer mRenderer;
    private FrameBuffer fb = null;
    private World world = null;

    private int fps = 0;

    private Light sun = null;

    private GL10 lastGl = null;

    private RGBColor back = new RGBColor(0, 0, 0, 0);


    private Random rand = new Random();
    public static int countSquares = 0;
    public Object3D ghost;
    public Object3D obj2;
    public Texture texture;

    float startX, startY;
    private Object3D android;

    public MyGLSurfaceView(Context context, Activity master) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        setPreserveEGLContextOnPause(true);

        setZOrderOnTop(true);

        mRenderer = new MyRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        this.master = master;

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    public void setRotation(float yaw, float pitch, float roll){
        mRenderer.setCameraRotation(yaw, pitch, roll);
    }

    public boolean onTouchEvent(MotionEvent me) {

        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            startX = me.getRawX();
            startY = me.getRawY();
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float relativeX=me.getRawX()-startX;
            float relativeY=me.getRawY()-startY;
            startX = me.getRawX();
            startY = me.getRawY();
            float rotY = relativeX/fb.getWidth();
            ghost.rotateY(rotY);

            return true;
        }

        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }

        return super.onTouchEvent(me);
    }

    public class MyRenderer implements GLSurfaceView.Renderer {

        private long time = System.currentTimeMillis();

        public MyRenderer() {
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {

            // Renew the frame buffer
            if (lastGl != gl) {
                Log.i("HelloWorld", "Init buffer");
                if (fb != null) {
                    fb.dispose();
                }
                fb = new FrameBuffer(w, h);
                fb.setVirtualDimensions(fb.getWidth(), fb.getHeight());
                lastGl = gl;
            } else {
                fb.resize(w, h);
                fb.setVirtualDimensions(w, h);
            }

            // Create the world if not yet created
            if (master == null) {
                world = new World();
                world.setAmbientLight(20, 20, 20);

                sun = new Light(world);
                sun.setIntensity(250, 250, 250);

                // Create the texture we will use in the blitting
                texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.ghost)), 256, 256), true);
                TextureManager.getInstance().addTexture("texture", texture);

                // Create the object
                ghost = Primitives.getPlane(1, 4f);
                world.addObject(ghost);
                ghost.translate(0, 0, 10);
                ghost.setTexture("texture");
                ghost.build();

                Camera cam = world.getCamera();
                //cam.moveCamera(Camera.CAMERA_MOVEOUT, 15);
                cam.lookAt(SimpleVector.ORIGIN);

                SimpleVector sv = new SimpleVector();
                sv.set(SimpleVector.ORIGIN);
                sv.y -= 100;
                sv.z -= 100;
                sun.setPosition(sv);
                MemoryHelper.compact();

                if (master == null) {
                    Logger.log("Saving master Activity!");
                    //master = .this;
                }
            }
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }

        public void onDrawFrame(GL10 gl) {

            // Draw the main screen
            fb.clear(back);
            world.renderScene(fb);
            world.draw(fb);
            fb.display();

            //ghost.rotateZ(0.01f);

            if (System.currentTimeMillis() - time >= 1000) {
                Logger.log(fps + "fps");
                fps = 0;
                time = System.currentTimeMillis();
            }
            fps++;
        }

        public void addAndroid() {
            try {
                InputStream is = getResources().getAssets().open("android.3ds");
                Object3D[] model = Loader.load3DS(is, 3);
                android = Object3D.mergeAll(model);
                android.build();
            } catch (IOException e) {
                e.printStackTrace();
            }
            android.rotateX((float) (-Math.PI / 2));
            world.addObject(android);
        }


        public void setCameraRotation(float yaw, float pitch, float roll) {

            if(world != null) {
                SimpleVector lookTarget = new SimpleVector(
                        -(float) (Math.cos(yaw) * Math.cos(pitch)),
                        (float) Math.sin(pitch),
                        (float) (Math.sin(yaw) * Math.cos(pitch))
                );

                Camera cam = world.getCamera();
                cam.lookAt(cam.getPosition().calcAdd(lookTarget));

                cam.rotateAxis(lookTarget, roll);
            }
        }
    }
}
