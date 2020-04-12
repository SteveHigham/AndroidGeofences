package uk.co.sjlt.androidgeofences.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.Getter;
import uk.co.sjlt.androidgeofences.Constants;
import uk.co.sjlt.androidgeofences.GeofencesApplication;
import uk.co.sjlt.androidgeofences.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * This screen will display the current location on request.
 * I make this activity a singleton so that GeofenceBroadcastReceiver can
 * access the public Handler. This is horrible but I can't fine a neat
 * solution.
 * This requires the manifest to set the launchMode="singleTask" attribute for
 * this class.
 */
public class DisplayLocationActivity extends AppCompatActivity
{

@Getter
private static Handler handler;

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

// Last Location Found

private Location lastLocationFound;

private ConstraintLayout llfFrame;

private TextView          llfLatitude;
private TextView          llfLongitude;
private TextView          llfTime;

private Button btnShowLlf;

// Current Location

private Location currentLocation;

private ConstraintLayout clFrame;

private TextView clLatitude;
private TextView clLongitude;
private TextView clTime;

private Button btnShowCl;

private SimpleDateFormat  timeFormatter;

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

  lastLocationFound = null;

  llfFrame      = findViewById (R.id.frame_llf_position);
  llfLatitude   = findViewById (R.id.latitude_llf_value);
  llfLongitude  = findViewById (R.id.longitude_llf_value);
  llfTime       = findViewById (R.id.time_llf_value);
  btnShowLlf    = findViewById (R.id.button_show_llf);

  currentLocation = null;

  clFrame       = findViewById (R.id.frame_current_position);
  clLatitude    = findViewById (R.id.latitude_cp_value);
  clLongitude   = findViewById (R.id.longitude_cp_value);
  clTime        = findViewById (R.id.time_cp_value);
  btnShowCl     = findViewById (R.id.button_show_cp);

  if (handler == null)
  {
    handler = new Handler (getMainLooper ())
    {
      @Override
      public void handleMessage (@NotNull Message msg)
      {
        Log.v (Constants.LOGTAG, CLASSTAG + "Handler receives Event");
        refreshScreen ();
      }
    };
  } else
  {
    Log.e ( Constants.LOGTAG,
        CLASSTAG + "Multiple DisplayLocationActivity instances");
    // This should not occur if the manifest has the
    // android:launchMode="singleTask" attribute
  }

  // Now this is set up we can initialise Geofencing
  initialiseGeofencing ();

  // I assume the app is running and permissions have already been granted.
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  btnShowEvents.setEnabled (app.isHasBackgroundLocationPermission ());
  if (btnShowEvents.isEnabled ())
  {
    btnShowEvents.setOnClickListener (new View.OnClickListener ()
    {
      @Override
      public void onClick (View view) { handleShowEvents (); }
    });
  }

  // If we get here we must have some location permissions enabled
  btnDisplayLocation.setOnClickListener (new View.OnClickListener ()
  {
    @Override
    public void onClick (View view) { handleDisplayLocationRequest (); }
  });

  btnShowCl.setOnClickListener (new View.OnClickListener ()
  {
    @Override
    public void onClick (View view) { handleShowCurrentLocation (); }
  });

  btnShowLlf.setOnClickListener (new View.OnClickListener ()
  {
    @Override
    public void onClick (View view) { handleShowLastLocationFound (); }
  });
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

@Override
public boolean onPrepareOptionsMenu (Menu menu)
{
  Log.v (Constants.LOGTAG, CLASSTAG + "onPrepareOptionsMenu called");
  menu.findItem (R.id.menu_show_events).setEnabled (getNumEvents () > 0);
  return true;
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

  // Add fences if required
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

public void refreshScreen ()
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

  btnShowEvents.setEnabled (getNumEvents () > 0);

  // Check the availability of the current location
  // We disable the Get Location button whilst we are querying the last known
  // location.
  // We also make the position labels invisible. It will be made visible if a
  // location is obtained.
  btnDisplayLocation.setEnabled (false);
  lastLocationFound = null;
  llfFrame.setVisibility (GONE);
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

  // Force the menu to be updated
  invalidateOptionsMenu ();
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

private void handleDisplayLocationRequest ()
{
  Log.v (Constants.LOGTAG, CLASSTAG + "Location requested");
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  app.requestSingleLocation (new LocationCallback ()
  {
    @Override
    public void onLocationAvailability (LocationAvailability la)
    {
      String msg = CLASSTAG +
          "onLocationAvailability called within handleDisplayLocationRequest";
      Log.v (Constants.LOGTAG, msg);
    }

    @Override
    public void onLocationResult (LocationResult result)
    { handleLocationResultReceived (result); }
  })
      .addOnSuccessListener ( new OnSuccessListener <Void> ()
      {
        @Override public void onSuccess (Void aVoid)
        { handleLocationRequestSucceeded (); }
      } )
      .addOnFailureListener (new OnFailureListener ()
      {
        @Override public void onFailure (@NonNull Exception e)
        { handleLocationRequestFailed (e); }
      } );
}

private void handleLastLocationFailed (Exception e)
{
  Log.w ( Constants.LOGTAG,
      CLASSTAG + "Last location failure: " + e.getLocalizedMessage () );
  btnDisplayLocation.setEnabled (true);
}

private void handleLastLocationFound (Location location)
{
  Log.v (Constants.LOGTAG, CLASSTAG + "Last location found: " + location);

  // Display the location
  lastLocationFound = location;
  String text = locationToString (location.getLatitude ());
  float accuracy = location.getAccuracy ();
  if (accuracy != 0.0f)
  {
    text += " (\u00B1" + // ± - \u00B1
        String.format (Locale.getDefault (), "%.2f m", accuracy) + ")";
  }
  llfLatitude.setText (text);
  llfLongitude.setText (locationToString (location.getLongitude ()));
  llfTime.setText (timeToString (location.getTime ()));
  llfFrame.setVisibility (VISIBLE);

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
            handleLastLocationFailed (e);
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

private void handleLocationRequestFailed (Exception e)
{
  Log.w (Constants.LOGTAG, CLASSTAG + "Location request failure: " +
      e.getLocalizedMessage () );
}

private void handleLocationRequestSucceeded ()
{
  Log.v (Constants.LOGTAG, CLASSTAG + "Location request succeeded");
}

private void handleLocationResultReceived (LocationResult result)
{
  String msg = CLASSTAG +
      "onLocationAvailability called within handleDisplayLocationRequest";
  Log.v (Constants.LOGTAG, CLASSTAG + "Location result received");
  //Location loc = result.getLastLocation ();
  currentLocation = result.getLastLocation ();
  String text = locationToString (currentLocation.getLatitude ());
  float accuracy = currentLocation.getAccuracy ();
  if (accuracy != 0.0f)
  {
    text += " (\u00B1" + // ± - \u00B1
      String.format (Locale.getDefault (), "%.2f m", accuracy) + ")";
  }
  clLatitude.setText (text);
  clLongitude.setText (locationToString (currentLocation.getLongitude ()));
  clTime.setText (timeToString (currentLocation.getTime ()));
  clFrame.setVisibility (VISIBLE);
}

private void handleShowCurrentLocation ()
{
  Log.v ( Constants.LOGTAG,
      CLASSTAG + "handleShowCurrentLocation called for location: " +
          currentLocation );
  if (currentLocation == null)
  {
    Log.w ( Constants.LOGTAG, CLASSTAG +
        "Ignoring show current location request as no location defined" );
  } else
  {
    showLocationOnMap ( getResources ().getString (R.string.current_position),
        currentLocation );
  }
}

private void handleShowLastLocationFound ()
{
  Log.v ( Constants.LOGTAG,
      CLASSTAG + "handleShowLastLocationFound called for location: " +
          lastLocationFound );
  if (lastLocationFound == null)
  {
    Log.w ( Constants.LOGTAG, CLASSTAG +
        "Ignoring show last location found request as no location defined" );
  } else
  {
    showLocationOnMap ( getResources ().getString (R.string.last_location),
        lastLocationFound );
  }
}

private void handleShowEvents ()
{
  Intent intent = new Intent (this, FenceEventsActivity.class);
  startActivity (intent);
}

private void initialiseGeofencing ()
{
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  app.initGeofencing (this);
}

private void showLocationOnMap (String name, Location loc)
{
  Intent intent = new Intent (this, ShowLocationActivity.class)
      .putExtra ("Location", loc)
      .putExtra ("Name", name);
  startActivity (intent);
}

}

// End of class.
