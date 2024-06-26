package com.example.abdessamadalhaouil_pm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

     GoogleMap mMap;

     TextView activityNameTextView;
     TextView speedTextView;
     TextView averageSpeedTextView;
     TextView caloriesTextView;
     TextView distanceTextView;


    private LocationManager locationManager;

    private Location lastLocation;

    private ArrayList<Float> speeds = new ArrayList<>();

    private double totalDistance = 0.0;
    private long startTime = 0;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        distanceTextView = findViewById(R.id.distance);
        speedTextView = findViewById(R.id.speed);
        averageSpeedTextView = findViewById(R.id.average_speed);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        activityNameTextView = findViewById(R.id.activity_name);

        caloriesTextView = findViewById(R.id.calories);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Check for permission to access location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Request location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        startTime = System.currentTimeMillis();



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] ss = new String[1];
            ss[0] = Manifest.permission.ACCESS_COARSE_LOCATION;
            requestPermissions(ss, 997);
        }
        // Enable current location on the map
        mMap.setMyLocationEnabled(true);

        if(isLocationEnabled()){
            Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            assert loc != null;
            double alt = loc.getAltitude() ;
            double lon = loc.getLongitude() ;
            double lat = loc.getLatitude() ;
            System.out.println("lon : " + lon);
            System.out.println("lat : " + lat);
            // Add a marker in Sydney and move the camera
            LatLng myLocation = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        }else{
            Toast.makeText(MapsActivity.this, "Activé localisation.", Toast.LENGTH_LONG).show();
        }
        // Zoom to current location if available
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            LatLng currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
        } else {
            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (lastLocation != null) {
            // Calculate distance between lastLocation and current location
            float[] results = new float[1];
            Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                    location.getLatitude(), location.getLongitude(), results);
            float distanceInMeters = results[0];
            totalDistance += distanceInMeters;

            // Calculate speed in meters per second
            long elapsedTime = System.currentTimeMillis() - startTime;
            float speed = distanceInMeters / (elapsedTime / 1000.0f); // meters per second
            speeds.add(speed);

            // Display distance and speed
            distanceTextView.setText(String.format("Distance: %.2f meters", totalDistance));
            speedTextView.setText(String.format("Speed: %.2f m/s", speed));

            // Calculate average speed
            float sum = 0;
            for (float s : speeds) {
                sum += s;
            }

            float averageSpeed = sum / speeds.size();
            averageSpeedTextView.setText(String.format("Avg Speed: %.2f m/s", averageSpeed));

            // Determine activity name
            String activityName = getActivityName(speed);
            activityNameTextView.setText(activityName);

            // Calculate calories
            float calories = calculateCalories(speed, elapsedTime);
            caloriesTextView.setText(String.format("Calories: %.2f kcal", calories));
        }

        lastLocation = location;
    }
    private String getActivityName(float speed) {
        if (speed < 0.5) {
            return "Stopped";
        } else if (speed < 2.5) {
            return "Walking";
        } else {
            return "Running";
        }
    }

    private float calculateCalories(float speed, long elapsedTime) {
        // Example: MET values
        float met = 0;
        if (speed < 0.5) {
            met = 1.0f; // Stopped
        } else if (speed < 2.5) {
            met = 3.8f; // Walking
        } else {
            met = 7.0f; // Running
        }
        // Calories burned per minute per kilogram
        float caloriesPerMinutePerKg = met * 3.5f / 200;
        // Assuming an average weight of 70 kg
        float caloriesBurned = caloriesPerMinutePerKg * 70 * (elapsedTime / 60000.0f); // elapsedTime in minutes
        return caloriesBurned;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop receiving location updates when the activity is destroyed
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}