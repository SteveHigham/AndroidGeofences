package uk.co.sjlt.androidgeofences;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;
import lombok.Setter;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
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
public enum Status { DEFAULT, FENCES_ADDED, FENCES_FAILED, FENCES_REMOVED }

private FusedLocationProviderClient fusedLocationClient;
private GeofencingClient geofencingClient;

/**
 * I store events in this array as additions will come in from the
 * BroadCastReceiver but requests will come in from the Activities. Therefore
 * I want to use a threadsave solution.
 * Updates are rare (not many per second) so I'm not worried about the arrays
 * performance.
 */
@Getter
CopyOnWriteArrayList<FenceEvent> events;

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
@Setter
private boolean hasCoarseLocationPermission;

@Getter
@Setter
private boolean hasFineLocationPermission;

/**
 * Identifies whether we have the foreground location permission.
 * Without this permission the app is useless and should terminate.
 */
public boolean hasForegroundLocationPermission ()
{ return hasCoarseLocationPermission || hasFineLocationPermission; }

@Getter
@Setter
private Status status;

/**
 * This holds the dialog object whilst the dialog is active.
 * At other times the value is null.
 */
private AlertDialog addingFenceFailedDialog;
/**
 * This holds the dialog's parent activity from the point the fence addition
 * is requested to the point the result has been handled. This avoids problems
 * passing the activity into an inner class for the handlers.
 * At other times the value is null.
 */
private Activity dialogActivity;


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
  status = Status.DEFAULT;
  events = new CopyOnWriteArrayList<> ();
}

public Task<Location> getLastLocation ()
{ return fusedLocationClient.getLastLocation (); }

public Task<LocationAvailability> getLocationAvailability ()
{ return fusedLocationClient.getLocationAvailability (); }

public @NotNull String getStatusText ()
{
  Log.v (Constants.LOGTAG, CLASSTAG + "Requesting status text for status: " + status);
  int resId;
  switch (status)
  {
    case DEFAULT:
      resId = R.string.status_default;
      break;
    case FENCES_ADDED:
      resId = R.string.status_fences_added;
      break;
    case FENCES_FAILED:
      resId = R.string.status_fences_failed;
      break;
    case FENCES_REMOVED:
      resId = R.string.status_fences_removed;
      break;
    default:
      resId = R.string.status_unknown;
  }
  String result = getResources ().getString (resId);
  Log.v (Constants.LOGTAG, CLASSTAG + "Returning status text: " + result);
  return result;
}

public String getTransitionString (Resources resources, int transition)
{
  String result;
  switch (transition)
  {
    case Geofence.GEOFENCE_TRANSITION_ENTER:
      result = resources.getString (R.string.geofence_event_enter);
      break;
    case Geofence.GEOFENCE_TRANSITION_EXIT:
      result = resources.getString (R.string.geofence_event_exit);
      break;
    default:
      result = resources.getString (R.string.geofence_event_unknown);

  }
  return result;
}

public boolean isDialogActive () { return dialogActivity != null; }

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
    // Initialise the Fused Location Client
    fusedLocationClient = LocationServices.getFusedLocationProviderClient (this);

    // Initialise the geofencing
    geofencingClient = LocationServices.getGeofencingClient (this);
    Intent intent = new Intent (this, GeofenceBroadcastReceiver.class);
    pendingIntent =
        PendingIntent.getBroadcast (this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT );
  }

  // We don't mind adding the same fences multiple times and we need to keep the flow going.
  addFences (activity);
}

public void addFences (Activity activity)
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
  dialogActivity = activity;
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
          handleAddingFenceFailed (e);
        }
      });
}

private void createAddingFenceFailedDialog (String msg)
{
  Log.v (Constants.LOGTAG, CLASSTAG + ">createAddingFenceFailedDialog");
  addingFenceFailedDialog = new AlertDialog.Builder (dialogActivity)
      .setTitle (R.string.adding_fence_failed_title)
      .setMessage (msg)
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

private void handleAddingFenceFailed (Exception e)
{
  String logMsg = CLASSTAG + "Adding fence failed: " + e.getLocalizedMessage ();
  Log.w (Constants.LOGTAG, logMsg, e);
  String msg;
  Resources res = getResources ();
  if (e instanceof ApiException)
  {
    int code = ((ApiException) e).getStatusCode ();
    switch (code)
    {
      case 1004:  // GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION
        msg = res.getString ( R.string.failed_with_error_code, code,
            res.getString (R.string.geofence_insufficient_location_permission) );
        break;
      case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
        msg = res.getString ( R.string.failed_with_error_code, code,
            res.getString (R.string.geofence_not_available) );
        break;
      case 1005: //GeofenceStatusCodes.GEOFENCE_REQUEST_TOO_FREQUENT
        msg = res.getString ( R.string.failed_with_error_code, code,
            res.getString (R.string.geofence_request_too_frequent) );
        break;
      case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
        msg = res.getString ( R.string.failed_with_error_code, code,
            res.getString (R.string.geofence_too_many_geofences) );
        break;
      case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
        msg = res.getString ( R.string.failed_with_error_code, code,
            res.getString (R.string.geofence_too_many_pending_intents) );
        break;
      default:
        msg = res.getString ( R.string.failed_with_error_code, code,
            e.getLocalizedMessage () );
    }
  } else
  {
    msg = res.getString ( R.string.failed_with_unexpected_error,
        e.getLocalizedMessage () );
  }
  createAddingFenceFailedDialog (msg);
  addingFenceFailedDialog.show ();
}

private void handleAddingFenceSucceeded ()
{
  Log.i (Constants.LOGTAG, CLASSTAG + "Fence added.");
  status = Status.FENCES_ADDED;
  Activity activity = dialogActivity;
  dialogActivity = null;

  // Transition to the Display Location screen
  // We see a crash under Android 10 if we don't set the FLAG_ACTIVITY_NEW_TASK
  Intent intent = new Intent (activity, DisplayLocationActivity.class);
  intent.addFlags (FLAG_ACTIVITY_NEW_TASK);
  activity.startActivity (intent);

  // Now we can kill the Request Permissions Activity
  activity.finish ();
}

/**
 * This method hadles the OK button response to the
 * AddingFenceFailedDialog.
 * We dismiss the dialog.
 */
private void handleCloseAddingFenceFailedDialog ()
{
  Log.v (Constants.LOGTAG, CLASSTAG + "handleCloseAddingFenceFailedDialog called");
  addingFenceFailedDialog = null;
  Activity activity = dialogActivity;
  dialogActivity = null;
  status = Status.FENCES_FAILED;

  // Transition to the Display Location screen
  startActivity (new Intent (activity, DisplayLocationActivity.class));
}

}

// End of class.

