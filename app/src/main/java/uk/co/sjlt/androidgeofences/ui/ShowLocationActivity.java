package uk.co.sjlt.androidgeofences.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import uk.co.sjlt.androidgeofences.Constants;
import uk.co.sjlt.androidgeofences.GeofencesApplication;
import uk.co.sjlt.androidgeofences.R;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a
 * particular location.
 */
public class ShowLocationActivity extends AppCompatActivity
    implements OnMapReadyCallback
{

private static final String CLASSTAG =
    " " + ShowLocationActivity.class.getSimpleName () + " ";

private Location displayLocation;
private String   displayName;

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);

  // Get the location to display and log it
  Intent intent = getIntent ();
  displayName     = intent.getStringExtra ("Name");
  displayLocation = intent.getParcelableExtra ("Location");
  Log.v ( Constants.LOGTAG, CLASSTAG + "onCreate called with Location: " +
      displayLocation + " Name: " + displayName );

  setContentView (R.layout.activity_show_location);
  SupportMapFragment fragment =
      (SupportMapFragment) getSupportFragmentManager ()
          .findFragmentById (R.id.map);
  fragment.getMapAsync (this);
}

@Override
public void onMapReady (GoogleMap map)
{
  Log.v ( Constants.LOGTAG, CLASSTAG + "onMapReady called for Location: " +
      displayLocation + " with Name: " + displayName );

  // Show the indicated location
  LatLng location =
      new LatLng ( displayLocation.getLatitude (),
          displayLocation.getLongitude () );
  showLocation (map, location, displayLocation.getAccuracy ());

  // Show the fences
  // Todo - This doesn't work as Geofence doesn't make the location available.
  // I need a specialised Fence class which can be used to build the fence
  // AND be queried for metadata such as the fence location, size and ID.
  for (Geofence fence: ((GeofencesApplication) getApplication ()).getFences ())
  {

  }

  // Move the camera to an appropriate position and zoom
  map.moveCamera (CameraUpdateFactory.newLatLngZoom (location, 15f));
}

@Override
protected void onResume ()
{
  super.onResume ();
  Log.v (Constants.LOGTAG, CLASSTAG + "onResume called");

  // Check Google Play Services is installed, up to date and enabled.

  int availability = GoogleApiAvailability.getInstance ()
      .isGooglePlayServicesAvailable (this);
  Log.v ( Constants.LOGTAG,
      CLASSTAG + "GooglePlayServices - Availability: " + availability );
  if (availability == ConnectionResult.SUCCESS)
  {
    Log.v (Constants.LOGTAG, CLASSTAG + "Google play services available");

  } else
  {
    Log.v (Constants.LOGTAG, CLASSTAG + "Google play services not available");
    // TODO - Fill in the "update google play services" code.
  }
}

private void showLocation (GoogleMap map, LatLng location, Float accuracy)
{
  // Show the location as a marker
  MarkerOptions mOptions = new MarkerOptions ().position (location).title (displayName);
  map.addMarker (mOptions);

  // If we have an accuracy add in a circle to display
  if (accuracy > 1f)
  {
    CircleOptions cOptions = new CircleOptions ()
        .center (location)
        .radius (accuracy);
    map.addCircle (cOptions);
  }

}

}
// End of class.
