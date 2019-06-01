package helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {

    private static final int REQUEST_CODE_FINE_GPS = 0;
    private static final String FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private Location mLocation;
    private boolean hasLocation;

    private Activity activity;

    public LocationHelper(Activity activity) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        this.activity = activity;
        this.hasLocation = false;

    }

    /**
     * Check to see we have the necessary permissions for this app.
     */
    private static boolean hasFineLocationPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, FINE_LOCATION_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check to see we have the necessary permissions for this app, and ask for them if we don't.
     */
    private static void requestFineLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[]{FINE_LOCATION_PERMISSION}, REQUEST_CODE_FINE_GPS);
    }

    public Location getLocation() {
        return mLocation;
    }

    private void setLocation(Location location) {
        this.mLocation = location;
    }

    public boolean isHasLocation() {
        return hasLocation;
    }

    private void setHasLocation(boolean hasLocation) {
        this.hasLocation = hasLocation;
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLcation() {

        if (hasFineLocationPermission(this.activity) == false) {

            requestFineLocationPermission(this.activity);

        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this.activity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                setLocation(location);
                                setHasLocation(true);
                            }
                        }
                    });
        }


    }

    public void onCreate() {

        createLocationRequest();
        getLastKnownLcation();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    setLocation(location);
                    setHasLocation(true);
                }
            }

            ;
        };
    }

    public void onResume() {
        startLocationUpdates();
    }

    public void onPause() {
        stopLocationUpdates();
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (hasFineLocationPermission(this.activity) == false) {

            requestFineLocationPermission(this.activity);

        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}
