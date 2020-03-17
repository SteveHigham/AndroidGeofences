package uk.co.sjlt.androidgeofences;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Iterator;
import java.util.List;

public class FenceEventsAdapter extends RecyclerView.Adapter<FenceEventsAdapter.EventViewHolder>
{

public static class EventViewHolder extends RecyclerView.ViewHolder
{
  TextView eventTimeView;
  TextView eventFenceView;
  TextView eventTypeView;

  public EventViewHolder (View view)
  {
    super (view);
    eventTimeView   = view.findViewById (R.id.event_time);
    eventFenceView  = view.findViewById (R.id.event_fence);
    eventTypeView   = view.findViewById (R.id.event_type);
  }
}

private List<FenceEvent> events;

FenceEventsAdapter (List<FenceEvent> list)
{ events = list; }

@NonNull
@Override
public EventViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType)
{
  return null;
}

@Override
public void onBindViewHolder (@NonNull EventViewHolder holder, int position)
{

}

@Override
public int getItemCount ()
{
  return events.size ();
}

}

// End of class


