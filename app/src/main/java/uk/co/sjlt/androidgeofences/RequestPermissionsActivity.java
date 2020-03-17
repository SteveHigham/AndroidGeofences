package uk.co.sjlt.androidgeofences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.*;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

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

/**
 * This holds the dialog object whilst the dialog is active.
 * At other times the value is null.
 */
private AlertDialog inadequatePermissionsDialog;

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  Log.v (Constants.LOGTAG, CLASSTAG + "onCreate called");
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
    startActivity (new Intent (this, DisplayLocationActivity.class));
  }
}

@Override
public void onRequestPermissionsResult ( int requestCode,
                                         @NonNull String [] permissions,
                                         @NonNull int [] grantResults )
{
  super.onRequestPermissionsResult (requestCode, permissions, grantResults);
  Log.v (Constants.LOGTAG, CLASSTAG + "onRequestPermissionsResult called");

  // Ignore any calls which are not related to REQUEST_CODE_PERMISSIONS
  if (requestCode == REQUEST_CODE_PERMISSIONS)
  {
    GeofencesApplication app = (GeofencesApplication) getApplication ();
    boolean coarsePermission = app.isHasCoarseLocationPermission ();
    boolean finePermission = app.isHasFineLocationPermission ();
    boolean backgroundPermission = app.isHasBackgroundLocationPermission ();

    // Check all permissions and update foreground, background appropriately.
    for (int i = 0; i < permissions.length; i++)
    {
      String permission = permissions [i];
      boolean grant = grantResults [i] == PERMISSION_GRANTED;
      if (permission.equalsIgnoreCase (ACCESS_COARSE_LOCATION))
      {
        coarsePermission = grant;
      } else if (permission.equalsIgnoreCase (ACCESS_FINE_LOCATION))
      {
        finePermission = grant;
      } else if (permission.equalsIgnoreCase (ACCESS_BACKGROUND_LOCATION))
      {
        backgroundPermission = grant;
      } else
      {
        Log.w ( Constants.LOGTAG,
            CLASSTAG + "Unexpected permission request: " + permission);
      }
    }

    if (coarsePermission || finePermission)
    {
      // Background permission is true for API's before Android 10 / Q
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
      { backgroundPermission = true; }

      if (backgroundPermission)
      {
        handleAllLocationUpdates (coarsePermission, finePermission);
      } else
      {
        handleForegroundLocationUpdatesOnly (coarsePermission, finePermission);
      }
      // We have some / all permisissions so we can progress to the next screen
      startActivity (new Intent (this, DisplayLocationActivity.class));
    } else
    {
      createInadequatePermissionsDialog ();
      inadequatePermissionsDialog.show ();
    }
  }
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

private void handleAllLocationUpdates (boolean coarsePermission, boolean finePermission)
{
  // foreground and background enabled.
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  app.setHasBackgroundLocationPermission (true);
  app.setHasCoarseLocationPermission (coarsePermission);
  app.setHasFineLocationPermission (finePermission);
}

/**
 * This method hadles the OK button response to the
 * InadequatePermissionsDialog.
 * We dismiss the dialog and shut down the application as it is useless
 * without any location permissions.
 */
private void handleCloseInadequatePermissionsDialog ()
{
  inadequatePermissionsDialog = null;
  finish ();
}

private void handleForegroundLocationUpdatesOnly ( boolean coarsePermission,
                                                   boolean finePermission )
{
  // only foreground enabled.
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  app.setHasCoarseLocationPermission (coarsePermission);
  app.setHasFineLocationPermission (finePermission);
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
  GeofencesApplication app = (GeofencesApplication) getApplication ();

  app.setHasCoarseLocationPermission (
      ActivityCompat.checkSelfPermission (this, ACCESS_COARSE_LOCATION) ==
          PERMISSION_GRANTED );

  app.setHasFineLocationPermission (
      ActivityCompat.checkSelfPermission (this, ACCESS_FINE_LOCATION) ==
          PERMISSION_GRANTED );

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
  {
    // Android 10 / Q or higher - we would like the background permission
    // as well
    app.setHasBackgroundLocationPermission (
        ActivityCompat.checkSelfPermission (this,
            ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED );
  }

  // Now work out what to ask for
  List<String> result = new ArrayList<> ();

  if (! app.isHasCoarseLocationPermission ())
  { result.add (ACCESS_COARSE_LOCATION); }

  if (! app.isHasFineLocationPermission ())
  { result.add (ACCESS_FINE_LOCATION); }


  if (! app.isHasBackgroundLocationPermission ())
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
    { result.add (ACCESS_BACKGROUND_LOCATION); }
  }

  // We need to cater for the case where no permissions are requested and the
  // API is before 10 / Q.
  if (result.isEmpty () && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q))
  { app.setHasBackgroundLocationPermission (true); }

  Log.v ( Constants.LOGTAG,
    CLASSTAG + "permissionsDesired returns: " + result.toString () );

  return result;
}

private void requestLocationPermissions (@NonNull String [] permissions)
{
  ActivityCompat.requestPermissions (this, permissions,
      REQUEST_CODE_PERMISSIONS );
}

}

// End of class

