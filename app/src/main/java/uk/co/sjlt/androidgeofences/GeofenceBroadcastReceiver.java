package uk.co.sjlt.androidgeofences;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Message;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Date;
import java.util.List;

import uk.co.sjlt.androidgeofences.model.FenceEvent;
import uk.co.sjlt.androidgeofences.ui.DisplayLocationActivity;
import uk.co.sjlt.androidgeofences.ui.FenceEventsActivity;
import uk.co.sjlt.androidgeofences.ui.RequestPermissionsActivity;

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

  // Add to the event list and raise a notification
  app.getEvents ().add (new FenceEvent (new Date (), fence, transition));
  raiseNotification (context, fence, transition, resources);
}

private void raiseNotification ( Context context, String fence,
                                 int transition, Resources resources )
{
  Log.v ( Constants.LOGTAG, CLASSTAG +
      "Raise a notification for crossing fence: " + fence );

  // Create the Intent to be launched from the notification
  Intent intent = new Intent (context, FenceEventsActivity.class);
  intent.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK |
      Intent.FLAG_ACTIVITY_CLEAR_TASK );
  PendingIntent pendingIntent =
      PendingIntent.getActivity (context, 0, intent, 0);

  // Create the builder and trigger the notification
  String title =
      resources.getString (R.string.receiver_transition_notification_title);
  String text;
  switch (transition)
  {
    case Geofence.GEOFENCE_TRANSITION_ENTER:
      text = resources.getString (
          R.string.receiver_transition_notification_entry, fence );
      break;
    case Geofence.GEOFENCE_TRANSITION_EXIT:
      text = resources.getString (
          R.string.receiver_transition_notification_exit, fence );
      break;
    default:
      Log.w ( Constants.LOGTAG, CLASSTAG + "Unknown transition code " +
          transition + " on fence " + fence );
      text = resources.getString (
          R.string.receiver_transition_notification_unknown,
          transition, fence );
  }
  NotificationCompat.Builder builder =
      new NotificationCompat.Builder ( context,
          RequestPermissionsActivity.CHANNEL_ID )
      .setSmallIcon (R.drawable.ic_launcher_foreground)
      .setContentTitle (title)
      .setContentText (text)
      /*
      .setStyle(new NotificationCompat.BigTextStyle()
          .bigText("Much longer text that cannot fit one line..."))
       */
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent (pendingIntent)
      .setAutoCancel (true);
  NotificationManagerCompat notificationManager =
      NotificationManagerCompat.from (context);
  // notificationId (1) is a unique int for each notification that you must define
  notificationManager.notify (1, builder.build ());
}

}

// End of class

