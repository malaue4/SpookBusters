package com.example.alexanderibsen.spookbusters;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by marti on 30/10/2017.
 */

public class Orientation implements SensorEventListener {

    public interface Listener{
        void onOrientationChanged(float yaw, float pitch, float roll);
    }

    private Listener listener;

    private static final String TAG = "SpookBuster.Orientation";
    private final SensorManager sensorManager;

    @Nullable
    private final Sensor rotationSensor;
    private int lastAccuracy;

    public Orientation(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void startListening(Listener listener){
        this.listener = listener;
        if(rotationSensor == null){
            Log.d(TAG, "rotation sensor hardware not available :(");
            return;
        }

        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stopListening(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(listener==null) return;
        if(lastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;
        if(sensorEvent.sensor == rotationSensor) updateOrientation(sensorEvent.values);
    }

    private void updateOrientation(float[] rotation) {
        listener.onOrientationChanged(rotation[0],rotation[1],rotation[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(accuracy != lastAccuracy){
            lastAccuracy = accuracy;
        }
    }
}
