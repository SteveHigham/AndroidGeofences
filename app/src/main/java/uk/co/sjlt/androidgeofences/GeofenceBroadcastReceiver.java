package uk.co.sjlt.androidgeofences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Date;
import java.util.List;

import uk.co.sjlt.androidgeofences.model.FenceEvent;
import uk.co.sjlt.androidgeofences.ui.DisplayLocationActivity;

public class GeofenceBroadcastReceiver extends BroadcastReceiver
{

private static final String CLASSTAG =
    " " + GeofenceBroadcastReceiver.class.getSimpleName () + " ";

@Override
public void onReceive (Context context, Intent intent)
{
  Log.v ( Constants.LOGTAG,
      CLASSTAG + "Event received from intent with action: " + intent.getAction ());
  GeofencingEvent event = GeofencingEvent.fromIntent (intent);

  if (event.hasError ())
  {
    String msg =
        GeofenceStatusCodes.getStatusCodeString (event.getErrorCode ());
    Log.w (Constants.LOGTAG, CLASSTAG + "Error: " + msg);
  } else
  {
    int transition = event.getGeofenceTransition ();
    if ( transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        transition == Geofence.GEOFENCE_TRANSITION_EXIT )
    {
      List<Geofence> fences = event.getTriggeringGeofences ();
      for (Geofence fence : fences)
      {
        handleTransition (context, fence.getRequestId (), transition);
      }
    }
  }

  // Now send on...
  Log.v ( Constants.LOGTAG, CLASSTAG + "Event context class: " +
      context.getClass ().getCanonicalName () );
  DisplayLocationActivity.getHandler ().handleMessage (new Message ());
 }

private void handleTransition (Context context, String fence, int transition)
{
  // Log the event
  GeofencesApplication app =
      (GeofencesApplication) (context.getApplicationContext ());
  Resources resources = context.getResources ();
  String msg = resources.getString ( R.string.receiver_log_transition,
      app.getTransitionString (resources, transition), fence );
  Log.i (Constants.LOGTAG, CLASSTAG + msg);

  // Add to the event list
  app.getEvents ().add (new FenceEvent (new Date (), fence, transition));
}

}

// End of class

