package uk.co.sjlt.androidgeofences;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FenceEventsActivity extends AppCompatActivity
{

private static final String CLASSTAG =
    " " + FenceEventsActivity.class.getSimpleName () + " ";

List<FenceEvent> events;

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  setContentView (R.layout.activity_fence_events);
  Toolbar toolbar = findViewById (R.id.toolbar);

  setSupportActionBar (toolbar);
  ActionBar actionBar = getSupportActionBar ();
  if (actionBar == null)
  {
    Log.w (Constants.LOGTAG, CLASSTAG + "Connot access the action bar");
  } else
  {
    actionBar.setDisplayHomeAsUpEnabled (true);
    actionBar.setDisplayShowHomeEnabled (true);
  }

  RecyclerView recycler = findViewById (R.id.fence_events_recycler);
  LinearLayoutManager layoutMgr = new LinearLayoutManager (this);
  recycler.setLayoutManager (layoutMgr);
}

@Override
public void onPause ()
{
  super.onPause ();
  events = null;
}

@Override
public void onResume ()
{
  super.onResume ();
  events = new ArrayList<> ();
  updateEvents ();
}

private void updateEvents ()
{
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  List<FenceEvent> appEvents = app.getEvents ();
  int numEvents = appEvents.size ();
  int eventsSize = events.size ();
  if (numEvents > eventsSize)
  {
    events.addAll (eventsSize, appEvents);
  }
  if ((events.size () != numEvents))
  { throw new AssertionError ("Event adding failed"); }
}

}

// End of class.
