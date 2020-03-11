package uk.co.sjlt.androidgeofences;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This screen will display the current location on request.
 */
public class DisplayLocationActivity extends AppCompatActivity
{

private static final String CLASSTAG =
    " " + DisplayLocationActivity.class.getSimpleName () + " ";

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  setContentView (R.layout.activity_display_location);
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
}

}

// End of class.
