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

private RecyclerView recycler;

@Override
protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate (savedInstanceState);
  Log.v (Constants.LOGTAG, CLASSTAG + "onCreate called");
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

  recycler = findViewById (R.id.fence_events_recycler);
  LinearLayoutManager layoutMgr = new LinearLayoutManager (this);
  recycler.setLayoutManager (layoutMgr);
}

@Override
public void onPause ()
{
  super.onPause ();
  Log.v (Constants.LOGTAG, CLASSTAG + "onPause called");
}

@Override
public void onResume ()
{
  super.onResume ();
  Log.v (Constants.LOGTAG, CLASSTAG + "onResume called");
  GeofencesApplication app = (GeofencesApplication) getApplication ();
  FenceEventsAdapter adapter = new FenceEventsAdapter (app);
  recycler.setAdapter (adapter);
  recycler.invalidate ();
}

@Override
public void onStart ()
{
  super.onStart ();
  Log.v (Constants.LOGTAG, CLASSTAG + "onStart called");
}

@Override
public void onStop ()
{
  super.onStop ();
  Log.v (Constants.LOGTAG, CLASSTAG + "onPause called");
}



}

// End of class.
