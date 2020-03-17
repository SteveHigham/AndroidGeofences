package uk.co.sjlt.androidgeofences;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * This screen will display the current location on request.
 */
public class DisplayLocationActivity extends AppCompatActivity
{

private static final String CLASSTAG =
    " " + DisplayLocationActivity.class.getSimpleName () + " ";

private TextView numEventsLabel;

private TextView status;
private TextView coarsePermission;
private TextView finePermission;
private TextView backgroundPermission;
private TextView numEvents;

private Button btnShowEvents;
private Button btnDisplayLocation;

private ConstraintLayout framePosition;

private TextView titlePosition;
private TextView latitude;
private TextView longitude;
private TextView locationTime;
private SimpleDateFormat timeFormatter;

private int getNumEvents ()
{
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  return app.getEvents ().size ();
}

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  Log.v (Constants.LOGTAG, CLASSTAG + "onCreate called");
  setContentView (R.layout.activity_display_location);

  numEventsLabel = findViewById (R.id.num_events_label);

  status                = findViewById (R.id.status_value);
  coarsePermission      = findViewById (R.id.coarse_permission_value);
  finePermission        = findViewById (R.id.fine_permission_value);
  backgroundPermission  = findViewById (R.id.background_permission_value);
  numEvents             = findViewById (R.id.num_events_value);

  btnShowEvents       = findViewById (R.id.button_show_events);
  btnDisplayLocation  = findViewById (R.id.button_display_location);

  framePosition   = findViewById (R.id.frame_position);
  titlePosition   = findViewById (R.id.title_position);
  latitude        = findViewById (R.id.latitude_value);
  longitude       = findViewById (R.id.longitude_value);
  locationTime    = findViewById (R.id.time_value);

  // I assume the app is running and permissions have already been granted.
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  btnShowEvents.setEnabled (app.isHasBackgroundLocationPermission ());
  if (btnShowEvents.isEnabled ())
  {
    btnShowEvents.setOnClickListener (new View.OnClickListener ()
    {
      @Override
      public void onClick (View view)
      { handleShowEvents (); }
    });
  }
}

@Override
public boolean onCreateOptionsMenu (Menu menu)
{
  MenuInflater inflater = getMenuInflater ();
  inflater.inflate (R.menu.display_location_menu, menu);
  return true;
}

@Override
public boolean onOptionsItemSelected (MenuItem item)
{
  boolean result;
  if (item.getItemId () == R.id.menu_show_events)
  {
    handleShowEvents ();
    result = true;
  } else
  {
    result = super.onOptionsItemSelected (item);
  }
  return result;
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
  // Replace the time formatter in case the Locale has changed.
  timeFormatter =
      new SimpleDateFormat ("dd-MMM HH:mm:ss", Locale.getDefault ());
  refreshScreen ();
}

/**
 * Convert a lat / long double to a formatted string
 */
private String locationToString (double d)
{ return Location.convert (d, Location.FORMAT_DEGREES); }

/**
 * Convert a UTC long time to a string using default Locale
 */
private String timeToString (long t)
{ return timeFormatter.format (new Date (t)); }

private void handleLastLocationFailed ()
{ btnDisplayLocation.setEnabled (true); }

private void handleLastLocationFound (Location location)
{
  Log.v (Constants.LOGTAG, CLASSTAG + "Last location found: " + location);

  // Display the location
  titlePosition.setText (R.string.title_last_location);
  latitude.setText (locationToString (location.getLatitude ()));
  longitude.setText (locationToString (location.getLongitude ()));
  locationTime.setText (timeToString (location.getTime ()));
  framePosition.setVisibility (VISIBLE);

  // Enable the get location button
  btnDisplayLocation.setEnabled (true);
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
    handleLocationNotAvailable ();
  }
}

private void handleLocationNotAvailable ()
{
  Log.i (Constants.LOGTAG, CLASSTAG + "Last known location not available");
  btnDisplayLocation.setEnabled (true);
}

private void handleShowEvents ()
{
  Intent intent = new Intent (this, FenceEventsActivity.class);
  startActivity (intent);
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

  // We only show the number of events received if we have the background permission
  if (app.isHasBackgroundLocationPermission ())
  {
    numEventsLabel.setEnabled (true);
    numEventsLabel.setVisibility (VISIBLE);
    numEvents.setEnabled (true);
    Locale locale = Locale.getDefault ();
    numEvents.setText (String.format (locale, "%d", getNumEvents ()));
    numEvents.setVisibility (VISIBLE);
  } else
  {
    numEventsLabel.setEnabled (false);
    numEventsLabel.setVisibility (GONE);
    numEvents.setEnabled (false);
    numEvents.setVisibility (GONE);
  }

  // Check the availability of the current location
  // We disable the Get Location button whilst we are querying the last known
  // location.
  // We also make the position labels invisible. It will be made visible if a
  // location is obtained.
  btnDisplayLocation.setEnabled (false);
  framePosition.setVisibility (INVISIBLE);
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
