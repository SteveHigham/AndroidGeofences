package uk.co.sjlt.androidgeofences;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
public void onMapReady (GoogleMap googleMap)
{
  Log.v ( Constants.LOGTAG, CLASSTAG + "onMapReady called for Location: " +
      displayLocation + " with Name: " + displayName );
  LatLng loc = new LatLng ( displayLocation.getLatitude (),
      displayLocation.getLongitude () );
  MarkerOptions options = new MarkerOptions ().position (loc).title (displayName);
  googleMap.addMarker (options);
  CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng (loc);
  googleMap.moveCamera (cameraUpdate);
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

}
// End of class.
