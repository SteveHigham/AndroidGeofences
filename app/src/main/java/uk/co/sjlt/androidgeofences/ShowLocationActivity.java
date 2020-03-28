package uk.co.sjlt.androidgeofences;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

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

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  displayLocation = getIntent ().getParcelableExtra ("Location");
  Log.v ( Constants.LOGTAG, CLASSTAG + "onCreate called with Location: " +
      displayLocation );
}

@Override
public void onMapReady (GoogleMap googleMap)
{
}

}
// End of class.
