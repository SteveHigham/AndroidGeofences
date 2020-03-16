package uk.co.sjlt.androidgeofences;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

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
  ActionBar actionBar = getSupportActionBar ();
  actionBar.setDisplayHomeAsUpEnabled (true);
  actionBar.setDisplayShowHomeEnabled (true);

  RecyclerView recycler = findViewById (R.id.fence_events_recycler);
}

}

// End of class.
