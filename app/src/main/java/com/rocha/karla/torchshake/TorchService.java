package com.rocha.karla.torchshake;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by karla on 11/11/17.
 */

public class TorchService extends Service {
    private SensorManager sensorManager;
    private float acelValue;
    private float acelLastValue;
    private float shake;
    Context context = this;
    boolean status = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        acelValue = SensorManager.GRAVITY_EARTH;
        acelLastValue = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    // Action taken when accelerometer is activated
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Position on a 3D plain
            float x = event.values[0]; // X axis
            float y = event.values[1]; // Y axis
            float z = event.values[2]; // Z axis

            acelLastValue = acelValue;
            // Calculate position
            acelValue = (float) Math.sqrt((double) x*x + y*y + z*z);
            // Difference between last known position and current position
            float delta = acelValue - acelLastValue;
            shake = shake + 0.9f * delta;

            Log.d("Shake", String.valueOf(shake));

            if(hasFlash() && shake > 12){
                if(status){
                    status = false;
                    torchOn(getApplicationContext());
                }else{
                    status = true;
                    torchOff(getApplicationContext());
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    // Verifies camera light exists
    public boolean hasFlash(){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private static void torchOn(Context context){
        try{
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] list = manager.getCameraIdList();
            manager.setTorchMode(list[0], true);
        }catch (CameraAccessException cae){
            cae.printStackTrace();
        }
    }

    private static void torchOff(Context context){
        try{
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            manager.setTorchMode(manager.getCameraIdList()[0], false);
        }
        catch (CameraAccessException cae){
            cae.printStackTrace();
        }
    }
}
