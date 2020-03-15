package uk.co.sjlt.androidgeofences;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

public class FenceEventsActivity extends AppCompatActivity
{

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  setContentView (R.layout.activity_fence_events);
  Toolbar toolbar = findViewById (R.id.toolbar);
  setSupportActionBar (toolbar);

  getSupportActionBar().setDisplayHomeAsUpEnabled (true);
  getSupportActionBar().setDisplayShowHomeEnabled (true);
}

}

// End of class.
