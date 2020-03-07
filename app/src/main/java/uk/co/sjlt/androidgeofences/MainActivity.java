package uk.co.sjlt.androidgeofences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import static android.Manifest.permission.*;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofencingRequest.*;

public class MainActivity extends AppCompatActivity
{

private static final String CLASSTAG =
    " " + MainActivity.class.getSimpleName () + " ";
private static final int REQUEST_CODE_PERMISSIONS = 101;
private GeofencingClient geofencingClient;
private Button btnPermissions;

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  setContentView (R.layout.activity_main);

  btnPermissions = findViewById (R.id.btnPermissions);
  btnPermissions.setOnClickListener ( new View.OnClickListener ()
  {
    @Override
    public void onClick (View view)
    { requestLocationPermission (); }
  } );

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

@Override
public void onRequestPermissionsResult ( int requestCode,
                                         @NonNull String [] permissions,
                                         @NonNull int [] grantResults )
{
  super.onRequestPermissionsResult (requestCode, permissions, grantResults);
  if (requestCode == REQUEST_CODE_PERMISSIONS)
  {
    boolean foreground = false, background = false;
    Context context = getApplicationContext ();

    // Check all permissions
    for (int i = 0; i < permissions.length; i++)
    {
      String permission = permissions [i];
      boolean grant = grantResults [i] == PERMISSION_GRANTED;
      if (permission.equalsIgnoreCase (ACCESS_FINE_LOCATION))
      {
        // Foreground permission allowed
        if (grant)
        {
          foreground = true;
          Toast.makeText (context, "Foreground location permission allowed",
              Toast.LENGTH_SHORT).show ();
        } else
        {
          Toast.makeText (context, "Location Permission denied",
              Toast.LENGTH_SHORT).show ();
          break;
        }
      } else if (permission.equalsIgnoreCase (ACCESS_BACKGROUND_LOCATION))
      {
        if (grant)
        {
          foreground = background = true;
          Toast.makeText (context, "Background location permission allowed",
              Toast.LENGTH_SHORT).show ();
        } else
        {
          Toast.makeText (context,
              "Background location permission denied",
              Toast.LENGTH_SHORT).show ();
        }
      } else
      {
        Log.w ( Constants.LOGTAG,
            CLASSTAG + "Unexpected permission request: " + permission);
      }
    }

    if (foreground)
    {
      if (background)
      {
        handleLocationUpdates ();
      } else
      {
        handleForegroundLocationUpdates ();
      }
    }
  }
}

private void handleLocationUpdates ()
{
  // foreground and background enabled.
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  app.setForegroundLocationPermission (true);
  app.setBackgroundLocationPermission (true);
}

private void handleForegroundLocationUpdates ()
{
  // only foreground enabled.
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  app.setForegroundLocationPermission (true);
}

private void requestLocationPermission ()
{
  Log.v (Constants.LOGTAG, CLASSTAG + ">requestLocationPermission ()");
  boolean foreground =
      ActivityCompat.checkSelfPermission (this, ACCESS_FINE_LOCATION) ==
          PERMISSION_GRANTED;
  if (foreground)
  {
    Log.v ( Constants.LOGTAG,
        CLASSTAG + "ACCESS_FINE_LOCATION already granted for foreground" );
    boolean background = false;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
    {
      background = ActivityCompat.
          checkSelfPermission (this, ACCESS_BACKGROUND_LOCATION) ==
          PERMISSION_GRANTED;
    }
    if (background)
    {
      Log.v ( Constants.LOGTAG,
          CLASSTAG + "ACCESS_BACKGROUND_LOCATION already granted" );
      handleLocationUpdates ();
    } else
    {
      Log.v ( Constants.LOGTAG,
          CLASSTAG + "Requesting permission: ACCESS_BACKGROUND_LOCATION" );
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
      {
        ActivityCompat.requestPermissions(this,
            new String [] {ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE_PERMISSIONS);
      }
    }
  } else
  {
    Log.v ( Constants.LOGTAG,
        CLASSTAG + "Requesting permissions: " +
            "ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION" );
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
    {
      ActivityCompat.requestPermissions(this,
          new String [] {ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION},
          REQUEST_CODE_PERMISSIONS );
    }
  }
  Log.v (Constants.LOGTAG, CLASSTAG + "<requestLocationPermission");
}

}

// End of class

