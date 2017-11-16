package com.example.alexanderibsen.spookbusters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;

import static android.support.v4.app.ActivityCompat.requestPermissions;
import static android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale;

/**
 * Created by marti on 16/11/2017.
 */

public class CameraPreviewView extends TextureView {
    private final static String TAG = "SimpleCamera";
    private static final int MY_PERMISSIONS_CAMERA = 0;
    private final Context context;
    TextureView.SurfaceTextureListener mySurfaceTextureListener = new MySurfaceTextureListener();

    private Size mPreviewSize = null;
    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mPreviewBuilder = null;
    private CameraCaptureSession mPreviewSession = null;
    private CameraDevice.StateCallback mStateCallback = new CameraDeviceCallback();
    private CameraCaptureSession.StateCallback mPreviewStateCallback = new CameraCaptureCallback();


    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }



    @RequiresApi(23)
    private void requestCameraPermission() {
        if(context.checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            if (shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(context, "Need camera to show ghosts", Toast.LENGTH_LONG).show();
            } else { // No explanation needed, we can request the permission.
                requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_CAMERA);
            }

        }
    }

    private boolean hasCameraPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void onPause() {
        if (mCameraDevice != null)
        {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    public void onResume() {
        if (mCameraDevice == null)
            openCamera();
    }

    class MySurfaceTextureListener implements TextureView.SurfaceTextureListener {

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // TODO Auto-generated method stub
            //Log.i(TAG, "onSurfaceTextureUpdated()");

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onSurfaceTextureSizeChanged()");

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onSurfaceTextureDestroyed()");
            return false;
        }


        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onSurfaceTextureAvailable()");

            openCamera();

        }


    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            if (!hasCameraPermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestCameraPermission();
                } else {
                    Toast.makeText(context, "This app needs camera permission", Toast.LENGTH_SHORT).show();
                }
            } else {
                manager.openCamera(cameraId, mStateCallback, null);
            }
        }
        catch(CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    private class CameraDeviceCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onOpened");
            mCameraDevice = camera;

            SurfaceTexture texture = getSurfaceTexture();
            if (texture == null) {
                Log.e(TAG, "texture is null");
                return;
            }

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);

            try {
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e){
                e.printStackTrace();
            }

            mPreviewBuilder.addTarget(surface);

            try {
                mCameraDevice.createCaptureSession(Arrays.asList(surface), mPreviewStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onError");

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onDisconnected");

        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
            Log.e(TAG, "onClosed");
        }
    }

    private class CameraCaptureCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onConfigured");
            mPreviewSession = session;

            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            HandlerThread backgroundThread = new HandlerThread("CameraPreview");
            backgroundThread.start();
            Handler backgroundHandler = new Handler(backgroundThread.getLooper());

            try {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            // TODO Auto-generated method stub
            Log.e(TAG, "CameraCaptureSession Configure failed");
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            super.onClosed(session);
            Log.i(TAG, "CameraCaptureSession closed");
        }
    }
}
