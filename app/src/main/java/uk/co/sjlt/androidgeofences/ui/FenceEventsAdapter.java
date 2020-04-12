package uk.co.sjlt.androidgeofences.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import uk.co.sjlt.androidgeofences.Constants;
import uk.co.sjlt.androidgeofences.model.FenceEvent;
import uk.co.sjlt.androidgeofences.GeofencesApplication;
import uk.co.sjlt.androidgeofences.R;

public class FenceEventsAdapter extends RecyclerView.Adapter<FenceEventsAdapter.EventViewHolder>
{

private static final String CLASSTAG =
    " " + FenceEventsAdapter.class.getSimpleName () + " ";

static class EventViewHolder extends RecyclerView.ViewHolder
{
  private static final String CLASSTAG =
      " " + FenceEventsAdapter.class.getSimpleName () + "." +
      EventViewHolder.class.getSimpleName () + " ";

  TextView eventTimeView;
  TextView eventFenceView;
  TextView eventTypeView;

  EventViewHolder (View view)
  {
    super (view);
    Log.v (Constants.LOGTAG, CLASSTAG + "Constructor called");
    eventTimeView   = view.findViewById (R.id.event_time);
    eventFenceView  = view.findViewById (R.id.event_fence);
    eventTypeView   = view.findViewById (R.id.event_type);
  }
}

/**
 * List of events to be displayed
 */
private @NonNull List<FenceEvent> events;

/**
 * Format the timestamp in a Locale dependant manner
 * See #updateTimeFormatter to replace the formatter
 */
private SimpleDateFormat timeFormatter;


private @NonNull
GeofencesApplication app;

@Override
public int getItemCount ()
{
  int result = events.size ();
  Log.v ( Constants.LOGTAG,
      CLASSTAG + "getItemCount () returns " + result );
  return result;
}

FenceEventsAdapter (@NotNull GeofencesApplication theApp)
{
  app = theApp;
  events = app.getEvents ();
  timeFormatter =
      new SimpleDateFormat ("dd-MMM HH:mm", Locale.getDefault ());
}

@Override
public void onAttachedToRecyclerView (@NotNull RecyclerView view)
{
  super.onAttachedToRecyclerView (view);
  Log.v (Constants.LOGTAG, CLASSTAG + "onAttachedToRecyclerView called");
}

@Override
public void onBindViewHolder (@NonNull EventViewHolder holder, int i)
{
  Log.v (Constants.LOGTAG, CLASSTAG + "onBindViewHolder called");
  FenceEvent event = events.get (i);
  Log.v (Constants.LOGTAG, CLASSTAG + "Binding event: " + i + " " + event);
  String text = timeFormatter.format (event.getTimestamp ());
  holder.eventTimeView.setText (text);
  holder.eventFenceView.setText (event.getFence ());
  text = app.getTransitionString (app.getResources (), event.getEvent ());
  holder.eventTypeView.setText (text);
}

@NonNull
@Override
public EventViewHolder onCreateViewHolder ( @NonNull ViewGroup parent,
                                            int viewType )
{
  Log.v (Constants.LOGTAG, CLASSTAG + "onCreateViewHolder called");
  View view = LayoutInflater.from (parent.getContext ())
          .inflate (R.layout.view_fence_event, parent, false);
  return new EventViewHolder (view);
}

/*
public void update ()
{
  timeFormatter =
      new SimpleDateFormat ("dd-MMM HH:mm", Locale.getDefault ());

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
 */

}

// End of class


