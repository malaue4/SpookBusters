package com.example.alexanderibsen.spookbusters.GL;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.alexanderibsen.spookbusters.Objects.Ghost3D;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static java.lang.Math.sin;

/**
 * Created by marti on 29/10/2017.
 */

public class MyGLSurfaceView extends GLSurfaceView {

    public static MyGLSurfaceView master = null;


    private final MyRenderer mRenderer;
    private FrameBuffer fb = null;
    private World world = null;

    private int fps = 0;

    private Light sun = null;

    private GL10 lastGl = null;

    private RGBColor back = new RGBColor(0, 0, 0, 0);
    public float flashValue = 0;


    private Random rand = new Random();

    public List<Ghost3D> ghosts = new ArrayList<>(1);

    public Texture texture;

    float startX, startY;
    private Object3D android;

    public MyGLSurfaceView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        if (master != null) {
            copy(master);
        }
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        //Configure the EGL to allow a transparent background
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        setPreserveEGLContextOnPause(true);


        setZOrderOnTop(true);

        mRenderer = new MyRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

    }

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }


    private void copy(Object src) {
        try {
            Logger.log("Copying data from master Activity!");
            Field[] fs = src.getClass().getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true);
                f.set(this, f.get(src));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            for(Object3D ghost : ghosts)
                ghost.rotateZ(rotY);

            return true;
        }

        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }

        return super.onTouchEvent(me);
    }

    public List<Ghost3D> getGhosts() {
        return ghosts;
    }

    public World getWorld() {
        return world;
    }



    public void addGhost(float x, float y, float z, int ghostId) {
        Ghost3D ghost = new Ghost3D(Primitives.getPlane(1, 4f), ghostId);

        world.addObject(ghost);
        ghost.moveTo(x, y, z);
        ghost.setTexture("texture");
        ghost.build();
        ghosts.add(ghost);
        ghost.lookAt(SimpleVector.ORIGIN);
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
                /*
                addGhost(0, 0, 15, ghosts.size());
                addGhost(15, 0, 0, ghosts.size());
                addGhost(-15, 0, 0, ghosts.size());
                addGhost(0, 0, -12, ghosts.size());
                */

                Camera cam = world.getCamera();
                //cam.moveCamera(Camera.CAMERA_MOVEOUT, 30);
                //cam.lookAt(SimpleVector.ORIGIN);

                SimpleVector sv = new SimpleVector();
                sv.set(SimpleVector.ORIGIN);
                sun.setPosition(sv);
                MemoryHelper.compact();

                if (master == null) {
                    Logger.log("Saving master Activity!");
                    master = MyGLSurfaceView.this;
                }
            }
        }

        public void addAndroid(int x, int y, int z) {
            try {
                if(android == null) {
                    InputStream is = getResources().getAssets().open("android.3ds");
                    Object3D[] model = Loader.load3DS(is, 3);
                    android = Object3D.mergeAll(model);
                    //android.rotateX((float) (-Math.PI / 2));
                    //android.rotateMesh();
                    android.build();
                }
            } catch (IOException e) {
                e.printStackTrace();
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



            float angDif = (float) Math.PI*2/ ghosts.size();
            float anger = (System.currentTimeMillis()%100000) / 2600f;
            /*for (int i = 0; i < ghosts.size(); i++) {
                Ghost3D ghost3D = ghosts.get(i);
                float ang = angDif * i + anger;
                ghost3D.moveTo((float)cos(ang)*10, ghost3D.getPosition().y, (float)sin(ang)*10);
                ghost3D.lookAt(SimpleVector.ORIGIN);
            }*/

            if (System.currentTimeMillis() - time >= 1000/60) {
                for (Ghost3D ghost :
                        ghosts) {
                    ghost.update(min(System.currentTimeMillis() - time, 100));
                    ghost.setTransparency((int) ((1-ghost.getPosition().length()/16)*20));
                }
                if(flashValue > 0.05) {
                    Logger.log(flashValue + "flashValue");
                    back.setTo((int) (255*flashValue), (int) (255*flashValue), (int) (255*flashValue), (int) (255*flashValue));
                    flashValue = flashValue * 0.5f;
                } else {
                    back.setTo(0,0,0,0);
                }
                //Logger.log(fps + "fps");
                fps = 0;
                time = System.currentTimeMillis();
            }
            fps++;
        }



        public void setCameraRotation(float yaw, float pitch, float roll) {

            if(world != null) {
                SimpleVector lookTarget = new SimpleVector(
                        -(float) (cos(yaw) * cos(pitch)),
                        (float) sin(pitch),
                        (float) (sin(yaw) * cos(pitch))
                );

                Camera cam = world.getCamera();
                cam.lookAt(lookTarget);
                cam.rotateCameraZ(-roll);

            }
        }
    }
}
