package uk.co.sjlt.androidgeofences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofencingRequest.*;

public class MainActivity extends AppCompatActivity
{

public static final int REQUEST_CODE_PERMISSIONS = 101;
private GeofencingClient geofencingClient;
private Button btnPermissions;

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  setContentView (R.layout.activity_main);
  btnPermissions = findViewById (R.id.btnPermissions);

  // Geofencing stuff

  geofencingClient = LocationServices.getGeofencingClient(this);

  // 50 metre geofence centred on Steve's House
  Geofence fence =  new Geofence.Builder ()
      .setRequestId ("Steve's House")
      .setCircularRegion (53.324859d, -1.990568d, 50f)
      .setExpirationDuration (NEVER_EXPIRE)
      .setTransitionTypes ( Geofence.GEOFENCE_TRANSITION_ENTER |
          Geofence.GEOFENCE_TRANSITION_EXIT )
      .build ();

  // Build the Geofencing request
  GeofencingRequest geoRequest = new GeofencingRequest. Builder ()
      .setInitialTrigger (INITIAL_TRIGGER_DWELL | INITIAL_TRIGGER_ENTER |
          INITIAL_TRIGGER_EXIT)
      .addGeofence (fence)
      .build ();
}

private void handleLocationUpdates ()
{
  //foreground and background
  Toast.makeText ( getApplicationContext(),
      "Start Foreground and Background Location Updates", Toast.LENGTH_SHORT )
      .show();
}

private void requestLocationPermission ()
{
  boolean foreground =
      ActivityCompat.checkSelfPermission (this, ACCESS_FINE_LOCATION) ==
          PERMISSION_GRANTED;
  if (foreground)
  {
    boolean background =
        ActivityCompat.
            checkSelfPermission (this, ACCESS_BACKGROUND_LOCATION) ==
            PERMISSION_GRANTED;
    if (background)
    {
      handleLocationUpdates ()
    } else
    {
      ActivityCompat.requestPermissions(this,
          new String[]{ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE_PERMISSIONS);
    }
  } else
  {
    ActivityCompat.requestPermissions(this,
        new String[]{ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION},
        REQUEST_CODE_PERMISSIONS );
  }
}

}

// End of class

