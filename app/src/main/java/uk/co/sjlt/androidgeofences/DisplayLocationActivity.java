package uk.co.sjlt.androidgeofences;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  setContentView (R.layout.activity_display_location);
  status                = findViewById (R.id.status_value);
  coarsePermission      = findViewById (R.id.coarse_permission_value);
  finePermission        = findViewById (R.id.fine_permission_value);
  backgroundPermission  = findViewById (R.id.background_permission_value);
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

private void refreshScreen ()
{
  GeofencesApplication app = (GeofencesApplication) getApplication ();
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
}

}

// End of class.
