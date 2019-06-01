package helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;

public class SensorHelper implements SensorEventListener {

    // device sensor manager
    private SensorManager sensorManager;
    private float heading;

    public SensorHelper(Context context) {
        // initialize your android device sensor capabilities
        //sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager = context.getSystemService(SensorManager.class);
    }

    public void onResume() {
        // code for system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    public void onUpdate() {

    }

    public void onPause() {
        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get angle around the z-axis rotated
        heading = Math.round(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float getHeading() {
        return heading;
    }
}
