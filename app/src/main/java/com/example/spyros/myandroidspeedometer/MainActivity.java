package com.example.spyros.myandroidspeedometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity implements LocationListener,
        View.OnClickListener, SensorEventListener {

    private LocationManager locationManager;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    TextView speedTextView;
    ToggleButton toggleButton;
    TextView longitudeValue;
    TextView latitudeValue;
    TextView acceleratorValue;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 10;
    private float currentSpeed = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speedTextView = (TextView) findViewById(R.id.speedTextView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        longitudeValue = (TextView) findViewById(R.id.longitudeValue);
        latitudeValue = (TextView) findViewById(R.id.latitudeValue);
        acceleratorValue = (TextView) findViewById(R.id.acceleratorValue);

        toggleButton.setChecked(true);
        toggleButton.setOnClickListener(this);

        //setup GPS location service
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //setup accelerometer sensor
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //turn on speedometer using GPS
        turnOnGps();
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        latitudeValue.setText(String.valueOf(lat));
        longitudeValue.setText(String.valueOf(lng));

        currentSpeed = location.getSpeed() * 3.6f;
        speedTextView.setText(new DecimalFormat("#.##").format(currentSpeed));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //if provider failed to get location try use another provider (Internet, GPS etc...)
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setCostAllowed(false);
            String providerName = locationManager.getBestProvider(criteria, true);
            //and then you can make location update request with selected best provider
            locationManager.requestLocationUpdates(providerName, 1000, 5, this);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        turnOnGps();
    }

    @Override
    public void onProviderDisabled(String provider) {
        turnOffGps();

    }

    private void turnOnGps() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
    }

    private void turnOffGps() {
        speedTextView.setText("0");
        longitudeValue.setText(getResources().getString(R.string.unknownLongLat));
        latitudeValue.setText(getResources().getString(R.string.unknownLongLat));
        longitudeValue.setText("unknown");
        locationManager.removeUpdates(this);
    }

    public void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.toggleButton) {
            vibrate();
            if (toggleButton.isChecked()) {
                turnOnGps();
            } else {
                turnOffGps();
            }
        }
    }


    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed < SHAKE_THRESHOLD) {
                    if (currentSpeed > 0) {
                        currentSpeed = currentSpeed - 1;
                    } else {
                        currentSpeed = 0;
                    }
                    speedTextView.setText(new DecimalFormat("#.##").format(currentSpeed));
                    acceleratorValue.setText("stopped");
                } else {
                    acceleratorValue.setText("moving");
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
