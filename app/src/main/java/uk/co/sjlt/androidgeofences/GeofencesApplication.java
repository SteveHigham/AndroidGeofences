package uk.co.sjlt.androidgeofences;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.Setter;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofencingRequest.*;

/**
 * Main AndroidGeofences application object.
 *
 * The Geofencing methods are held here so they can be accessed by any activity.
 *
 * I use hasXxxPermission boolean variables to force Lombok to create hasXxx
 * getters, otherwise Lombok defaults to isXxx getters.
 * Kind of works. Now we have isHas... getters. I thgink this will be fixed
 * in a future Lombok release.
 */
public class GeofencesApplication extends Application
{

private static final String CLASSTAG =
    " " + GeofencesApplication.class.getSimpleName () + " ";

/**
 * DEFAULT - Initial status. Fences need to be added but this hasn't happened
 * yet. This may have been tried and failed in which case it should be retried
 * if the app gets the focus again.
 */
public enum Status { DEFAULT, FENCES_ADDED, FENCES_REMOVED }

private GeofencingClient geofencingClient;

/**
 * Identifies whether we have the foreground location permission.
 * This will initialise to false. Without this permission the app is useless
 * and should terminate.
 */
@Getter
@Setter
private boolean hasForegroundLocationPermission;

/**
 * Identifies whether we have the background location permission.
 * This will initialise to false. For API's before Android 10 (Android Q) this
 * will always be set true. From 10 / Q onwards this depends on the user. We can operate
 * without background locations with reduced functionality.
 */
@Getter
@Setter
private boolean hasBackgroundLocationPermission;

@Getter
private Status status;

private AlertDialog addingFenceFailedDialog;
private PendingIntent pendingIntent;

/**
 * Overriding the constructor causes the emulator to crash.
 * Therefore we do our initialisation here.
 */
@Override
public void onCreate ()
{
  super.onCreate ();
  Log.v (Constants.LOGTAG, CLASSTAG + "Application onCreate called");
  status = status.DEFAULT;
}

public boolean isGeofencingInitialised () { return geofencingClient != null; }

/**
 * This initialises the Geofencing system.
 * This cannot be done until the relevant permissions have been verified,
 * therefore this is called from the RequestPermissionsActivity.
 */
public void initGeofencing (Activity activity)
{
  if (! isGeofencingInitialised ())
  {
    geofencingClient = LocationServices.getGeofencingClient (this);
    Intent intent = new Intent (this, GeofenceBroadcastReceiver.class);
    pendingIntent =
        PendingIntent.getBroadcast (this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT );
    addFences (activity);
  }
}

public void addFences (final Activity activity)
{
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

  // Add the geofence
  geofencingClient.addGeofences (request, pendingIntent)
      .addOnSuccessListener (activity, new OnSuccessListener<Void> ()
      {
        @Override
        public void onSuccess (Void aVoid)
        {
          handleAddingFenceSucceeded ();
        }
      })
      .addOnFailureListener (activity, new OnFailureListener ()
      {
        @Override
        public void onFailure (@NotNull Exception e)
        {
          handleAddingFenceFailed (activity, e);
        }
      });
}

private void createAddingFenceFailedDialog (final Activity activity)
{
  Log.v (Constants.LOGTAG, CLASSTAG + ">createAddingFenceFailedDialog");
  addingFenceFailedDialog = new AlertDialog.Builder (activity)
      .setTitle (R.string.adding_fence_failed_title)
      .setMessage ("")
      .setPositiveButton (R.string.ok, new DialogInterface.OnClickListener ()
      {
        @Override
        public void onClick (DialogInterface dialog, int which)
        {
          handleCloseAddingFenceFailedDialog (activity);
        }
      })
      .create ();
}

private void handleAddingFenceFailed (Activity activity, Exception e)
{
  String msg = CLASSTAG + "Adding fence failed: " + e.getMessage ();
  Log.w (Constants.LOGTAG, msg, e);
  createAddingFenceFailedDialog (activity);
  addingFenceFailedDialog.show ();
}

private void handleAddingFenceSucceeded ()
{
  Log.i (Constants.LOGTAG, CLASSTAG + "Fence added.");
  status = Status.FENCES_ADDED;
}

/**
 * This method hadles the OK button response to the
 * AddingFenceFailedDialog.
 * We dismiss the dialog.
 */
private void handleCloseAddingFenceFailedDialog (Activity activity)
{
  addingFenceFailedDialog.dismiss ();
  addingFenceFailedDialog = null;
  activity.finish ();
}

}

// End of class.

