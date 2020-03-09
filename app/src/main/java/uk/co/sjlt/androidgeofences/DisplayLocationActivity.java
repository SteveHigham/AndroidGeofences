package uk.co.sjlt.androidgeofences;

import android.os.Bundle;

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

}

// End of class.
