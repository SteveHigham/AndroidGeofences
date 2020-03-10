package uk.co.sjlt.androidgeofences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.Contract;

import java.lang.Class;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.*;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofencingRequest.*;

/**
 * This is the first screen which is run on application start up.
 * It's purpose is to request any desired permissions.
 * We need to worry about ACCESS_FINE_LOCATION. Without this the app is
 * useless and should terminate.
 * Before Android 10 (Android Q) this permission is sufficient for foreground
 * and background location access. From 10 / Q onwards an additional
 * permission (ACCESS_BACKGROUND_LOCATION) is required for background
 * location access. We can manage without this with reduced functionality.
 */
public class RequestPermissionsActivity extends AppCompatActivity
{

private static final String CLASSTAG =
    " " + RequestPermissionsActivity.class.getSimpleName () + " ";
private static final int REQUEST_CODE_PERMISSIONS = 101;

private Button btnPermissions;
AlertDialog addingFenceFailedDialog;
AlertDialog inadequatePermissionsDialog;

// private GeofencingClient geofencingClient;

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  setContentView (R.layout.activity_request_permissions);

  // First priority is to check permissions. If we already have all the
  // permissions we desire then no user interaction is required.
  final List<String> permissions = permissionsDesired ();

  // If we want additional permissions then we need to talk to the user
  if (permissions.size () > 0)
  {

    btnPermissions = findViewById (R.id.btnPermissions);
    btnPermissions.setClickable (true);
    btnPermissions.setVisibility (View.VISIBLE);
    btnPermissions.setOnClickListener (new View.OnClickListener ()
    {
      @Override
      public void onClick (View view)
      {
        String [] strPermissions = new String [permissions.size ()];
        permissions.toArray (strPermissions);
        requestLocationPermissions (strPermissions);
      }
    });

    // Now we are done. We wait for the user response which will be handled by
    // onRequestPermissionsResult (...)
  } else
  {
    // No permissions required.
    initialiseGeofencing ();
    transitionTo (DisplayLocationActivity.class);
  }
}

@Override
public void onRequestPermissionsResult ( int requestCode,
                                         @NonNull String [] permissions,
                                         @NonNull int [] grantResults )
{
  super.onRequestPermissionsResult (requestCode, permissions, grantResults);

  // Ignore any calls which are not related to REQUEST_CODE_PERMISSIONS
  if (requestCode == REQUEST_CODE_PERMISSIONS)
  {
    //Context context = getApplicationContext ();
    GeofencesApplication app = (GeofencesApplication) getApplication ();
    boolean foreground = app.isHasForegroundLocationPermission ();
    boolean background = app.isHasBackgroundLocationPermission ();

    // Check all permissions and update foreground, background appropriately.
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
        }
      } else if (permission.equalsIgnoreCase (ACCESS_BACKGROUND_LOCATION))
      {
        if (grant)
        {
          foreground = background = true;
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
        handleAllLocationUpdates ();
      } else
      {
        handleForegroundLocationUpdatesOnly ();
      }
      // We have some / all permisissions so we can initialise the geofencing
      initialiseGeofencing ();
      transitionTo (DisplayLocationActivity.class);
    } else
    {
      createInadequatePermissionsDialog ();
      inadequatePermissionsDialog.show ();
    }
  }
}

private void createAddingFenceFailedDialog ()
{
  addingFenceFailedDialog = new AlertDialog.Builder (this)
      .setTitle (R.string.adding_fence_failed_title)
      .setPositiveButton (R.string.ok, new DialogInterface.OnClickListener ()
      {
        @Override
        public void onClick (DialogInterface dialog, int which)
        {
          handleCloseAddingFenceFailedDialog ();
        }
      })
      .create ();
}

private void createInadequatePermissionsDialog ()
{
  inadequatePermissionsDialog =  new AlertDialog.Builder (this)
      .setTitle (R.string.inadequate_permissions_title)
      .setMessage (R.string.inadequate_permissions_message)
      .setPositiveButton (R.string.ok, new DialogInterface.OnClickListener ()
      {
        @Override
        public void onClick (DialogInterface dialog, int which)
        {
          handleCloseInadequatePermissionsDialog ();
        }
      })
      .create ();
}

private void handleAddingFenceFailed (Exception e)
{
  String msg = CLASSTAG + "Adding fence failed: " + e.getMessage ();
  Log.e (Constants.LOGTAG, msg, e);
}

private void handleAllLocationUpdates ()
{
  // foreground and background enabled.
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  app.setHasForegroundLocationPermission (true);
  app.setHasBackgroundLocationPermission (true);
}

/**
 * This method hadles the OK button response to the
 * AddingFenceFailedDialog.
 * We dismiss the dialog.
 */
private void handleCloseAddingFenceFailedDialog ()
{
  addingFenceFailedDialog.dismiss ();
  addingFenceFailedDialog = null;
}

/**
 * This method hadles the OK button response to the
 * InadequatePermissionsDialog.
 * We dismiss the dialog and shut down the application as it is useless
 * without any location permissions.
 */
private void handleCloseInadequatePermissionsDialog ()
{
  inadequatePermissionsDialog.dismiss ();
  inadequatePermissionsDialog = null;
  finish ();
}

private void handleForegroundLocationUpdatesOnly ()
{
  // only foreground enabled.
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  app.setHasForegroundLocationPermission (true);
}

private void initialiseGeofencing ()
{
  GeofencingClient client = LocationServices.getGeofencingClient(this);

  // 50 metre geofence centred on Steve's House
  Geofence fence =  new Geofence.Builder ()
      .setRequestId ("Steve's House")
      .setCircularRegion (53.324859d, -1.990568d, 50f)
      .setExpirationDuration (NEVER_EXPIRE)
      .setTransitionTypes ( Geofence.GEOFENCE_TRANSITION_ENTER |
          Geofence.GEOFENCE_TRANSITION_EXIT )
      .build ();

  // Build the Geofencing request
  GeofencingRequest request = new GeofencingRequest. Builder ()
      .setInitialTrigger (INITIAL_TRIGGER_DWELL | INITIAL_TRIGGER_ENTER |
          INITIAL_TRIGGER_EXIT)
      .addGeofence (fence)
      .build ();

  // Create the PendingIntent
  Intent intent = new Intent (this, GeofenceBroadcastReceiver.class);
  PendingIntent pendingIntent =
      PendingIntent.getBroadcast (this, 0, intent,
          PendingIntent.FLAG_UPDATE_CURRENT );

  // Add the geofence
  client.addGeofences (request, pendingIntent)
      .addOnSuccessListener (this, new OnSuccessListener<Void> ()
      {
        @Override
        public void onSuccess (Void aVoid)
        {
          Log.i (Constants.LOGTAG, CLASSTAG + "Fence added.");
        }
      })
      .addOnFailureListener (this, new OnFailureListener ()
      {
        @Override
        public void onFailure (Exception e)
        {
          handleAddingFenceFailed (e);
        }
      });
}

/**
 * Method to return all permissions which we would like to have but do not
 * currently have.
 * This only includes ACCESS_BACKGROUND_LOCATION if we are running under
 * Android 10 / Q or higher.
 * This also updates the application variables correctly.
 * @return List<String> List of desired permissions we do not currently have.
 */
@Contract (pure = true)
@NonNull
private List<String> permissionsDesired ()
{
  List<String> result = new ArrayList<> ();

  if ( ActivityCompat.checkSelfPermission (this, ACCESS_FINE_LOCATION) ==
      PERMISSION_GRANTED )
  {
    // We have the foreground permission
    GeofencesApplication app = (GeofencesApplication) getApplication ();
    app.setHasForegroundLocationPermission (true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
    {
      // Android 10 / Q or higher - we would like the background permission
      // as well
      if (ActivityCompat.checkSelfPermission (this,
          ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED)
      {
        app.setHasBackgroundLocationPermission (true);
      } else
      {
        result.add (ACCESS_BACKGROUND_LOCATION);
      }
    } else
    {
      // Before Android 10 / Q. Therefore background location checking doesn't
      // need any additional permissions.
      app.setHasBackgroundLocationPermission (true);
    }
  } else
  {
    // We don't have any permissions
    result.add (ACCESS_FINE_LOCATION);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
    {
      // Background permission also desired for Android 10 / Q or later
      result.add (ACCESS_BACKGROUND_LOCATION);
    }
  }

  Log.v ( Constants.LOGTAG,
      CLASSTAG + "permissionsDesired returns: " + result.toString () );

  return result;
}

private void requestLocationPermissions (@NonNull String [] permissions)
{
  ActivityCompat.requestPermissions (this, permissions,
      REQUEST_CODE_PERMISSIONS );
}

private void transitionTo (Class activityClass)
{
  startActivity (new Intent (this, activityClass));
}

}

// End of class

