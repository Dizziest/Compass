package com.example.compassnetguru.viewmodel;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CompassViewModel extends ViewModel {

    private float[] geomagnetic = new float[3];
    private float[] gravity = new float[3];
    private float azim = 0f;
    private MutableLiveData<Float> azimuth = new MutableLiveData<>();
    private MutableLiveData<Float> bearing = new MutableLiveData<>();
    private Location destination;

    public LiveData<Float> getAzimuthLiveData(){
        return azimuth;
    }


    public LiveData<Float> getBearingLiveData(){
        return bearing;
    }

    public void onSensorChanged(SensorEvent event, ImageView image, ImageView image2){
        final float alpha = 0.97f;
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                gravity[0] = alpha * gravity[0] + (1-alpha)*event.values[0];
                gravity[1] = alpha * gravity[1] + (1-alpha)*event.values[1];
                gravity[2] = alpha * gravity[2] + (1-alpha)*event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                geomagnetic[0] = alpha * geomagnetic[0] + (1-alpha)*event.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1-alpha)*event.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1-alpha)*event.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R,I,gravity,geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azim = (float)Math.toDegrees(orientation[0]);
                azim = (azim+360)%360;
                azimuth.postValue(azim);

            }
        }
    }

    public void onDestinationChanged(Float latitude, Float longitude){
        destination = new Location("location");
        destination.setLatitude(latitude);
        destination.setLongitude(longitude);
    }

    public void onLocationChanged(Location location){
        if (destination != null){
            bearing.postValue(location.bearingTo(destination));
        }

    }

}
