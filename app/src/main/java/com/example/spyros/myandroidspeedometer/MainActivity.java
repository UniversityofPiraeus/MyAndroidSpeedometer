package com.example.spyros.myandroidspeedometer;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends ActionBarActivity implements LocationListener,
        View.OnClickListener {

    LocationManager locationManager;
    TextView speedTextView;
    ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speedTextView = (TextView) findViewById(R.id.speedTextView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        turnOnSpeedometer();
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
        float currentSpeed = location.getSpeed();
        speedTextView.setText(Float.toString(currentSpeed));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        turnOnSpeedometer();
    }

    @Override
    public void onProviderDisabled(String provider) {
        turnOffSpeedometer();
    }

    private void turnOnSpeedometer(){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
    }

    private void turnOffSpeedometer(){
        speedTextView.setText("0");
        locationManager.removeUpdates(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.toggleButton){
            if(toggleButton.isChecked()){
                turnOnSpeedometer();
            }
            else{
                turnOffSpeedometer();
            }
        }
    }
}
