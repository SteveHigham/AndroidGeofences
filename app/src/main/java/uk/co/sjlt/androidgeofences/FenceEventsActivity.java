package uk.co.sjlt.androidgeofences;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Iterator;

public class FenceEventsActivity extends AppCompatActivity
{

  Iterator<FenceEvent> eventIterator;

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
  LinearLayoutManager layoutMgr = new LinearLayoutManager (this);
  recycler.setLayoutManager (layoutMgr);
}

@Override
public void onPause ()
{
  super.onPause ();
  eventIterator = null;
}

@Override
public void onResume ()
{
  super.onResume ();
  updateIterator ();
}

private void updateIterator ()
{
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  eventIterator = app.getEvents ().iterator ();
}

}

// End of class.
