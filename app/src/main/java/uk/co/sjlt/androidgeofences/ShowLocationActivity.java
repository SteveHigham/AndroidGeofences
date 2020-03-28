package uk.co.sjlt.androidgeofences;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a
 * particular location.
 */
public class ShowLocationActivity extends AppCompatActivity
    implements OnMapReadyCallback
{

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
}

@Override
public void onMapReady (GoogleMap googleMap)
{
}

}
// End of class.
