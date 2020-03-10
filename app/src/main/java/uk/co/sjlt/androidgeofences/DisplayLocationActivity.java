package uk.co.sjlt.androidgeofences;

import android.os.Bundle;
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

}

// End of class.
