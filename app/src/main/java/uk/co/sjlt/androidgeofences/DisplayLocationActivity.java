package uk.co.sjlt.androidgeofences;

import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * This screen will display the current location on request.
 */
public class DisplayLocationActivity extends AppCompatActivity
{

private static final String CLASSTAG =
    " " + DisplayLocationActivity.class.getSimpleName () + " ";

private TextView status;
private TextView coarsePermission;
private TextView finePermission;
private TextView backgroundPermission;

private Button btnCurrentLocation;

private ConstraintLayout positionLabels;

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  setContentView (R.layout.activity_display_location);

  status                = findViewById (R.id.status_value);
  coarsePermission      = findViewById (R.id.coarse_permission_value);
  finePermission        = findViewById (R.id.fine_permission_value);
  backgroundPermission  = findViewById (R.id.background_permission_value);

  btnCurrentLocation = findViewById (R.id.btnCurrentLocation);

  positionLabels = findViewById (R.id.frame_position_labels);
}

@Override
public boolean onCreateOptionsMenu (Menu menu)
{
  MenuInflater inflater = getMenuInflater ();
  inflater.inflate (R.menu.display_location_menu, menu);
  return true;
}

/**
 * We need to cater for the use case where the add fences initially fails. The user
 * then fixes the problem (device not allowing fine location) and returns to
 * the app.
 */
@Override
public void onPause ()
{
  super.onPause ();
  Log.v (Constants.LOGTAG, CLASSTAG + "onPause called");
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  if (app.getStatus () == GeofencesApplication.Status.FENCES_FAILED)
  {
    app.setStatus (GeofencesApplication.Status.DEFAULT);
  }
}

/**
 * We need to cater for the use case where the add fences initially fails. The user
 * then fixes the problem (device not allowing fine location) and returns to
 * the app.
 */
@Override
public void onResume ()
{
  super.onResume ();
  Log.v (Constants.LOGTAG, CLASSTAG + "onResume called");
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  if ( app.isGeofencingInitialised () &&
      (app.getStatus () == GeofencesApplication.Status.DEFAULT) )
  {
    app.addFences (this);
  }
  refreshScreen ();
}

private void handleLastLocationFailed ()
{ btnCurrentLocation.setEnabled (true); }

private void handleLastLocationFound (Location location)
{
  Log.v (Constants.LOGTAG, CLASSTAG + "Last location found: " + location);
  positionLabels.setVisibility (VISIBLE);
  btnCurrentLocation.setEnabled (true);
}

private void handleLocationAvailable (LocationAvailability availability)
{
  Log.v (Constants.LOGTAG, CLASSTAG + "LocationAvailability returned");
  if (availability.isLocationAvailable ())
  {
    GeofencesApplication app = (GeofencesApplication) getApplication ();
    app.getLastLocation ()
        .addOnSuccessListener ( new OnSuccessListener<Location> ()
        {
          @Override
          public void onSuccess (Location location)
          {
            handleLastLocationFound (location);
          }
        } )
        .addOnFailureListener ( new OnFailureListener ()
        {
          @Override
          public void onFailure (@NonNull Exception e)
          {
            Log.w ( Constants.LOGTAG,
                CLASSTAG + "Last location failure: " +
                    e.getLocalizedMessage () );
            handleLastLocationFailed ();
          }
        } );
  } else
  {
    Log.i ( Constants.LOGTAG,
        CLASSTAG + "LocationAvailabilty.isLocationAvailable returns false" );
    btnCurrentLocation.setEnabled (true);
  }
}

private void handleLocationNotAvailable ()
{
  Log.i (Constants.LOGTAG, CLASSTAG + "Last known location not available");
  btnCurrentLocation.setEnabled (true);
}

private void refreshScreen ()
{
  GeofencesApplication app = (GeofencesApplication) getApplication ();

  // Display known statuses
  status.setText (app.getStatusText ());
  Resources res = getResources ();
  String text = res.getString ( app.isHasCoarseLocationPermission () ?
      R.string.permission_granted : R.string.permission_withheld );
  coarsePermission.setText (text);
  text = res.getString ( app.isHasFineLocationPermission () ?
      R.string.permission_granted : R.string.permission_withheld );
  finePermission.setText (text);
  text = res.getString ( app.isHasBackgroundLocationPermission () ?
      R.string.permission_granted : R.string.permission_withheld );
  backgroundPermission.setText (text);

  // Check the availability of the current location
  // We disable the Get Location button whilst we are querying the last known
  // location.
  // We also make the position labels invisible. It will be made visible if a
  // location is obtained.
  btnCurrentLocation.setEnabled (false);
  positionLabels.setVisibility (INVISIBLE);
  app.getLocationAvailability ()
      .addOnSuccessListener ( new OnSuccessListener<LocationAvailability> ()
      {
        @Override
        public void onSuccess (LocationAvailability availability)
        {
          handleLocationAvailable (availability);
        }
      } )
      .addOnFailureListener ( new OnFailureListener ()
      {
        @Override
        public void onFailure (@NonNull Exception e)
        {
          Log.w ( Constants.LOGTAG,
              CLASSTAG + "Location availability failure: " +
                  e.getLocalizedMessage () );
          handleLocationNotAvailable ();
        }
      } );
}

}

// End of class.
