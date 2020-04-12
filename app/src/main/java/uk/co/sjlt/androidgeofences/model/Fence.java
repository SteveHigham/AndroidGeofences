package uk.co.sjlt.androidgeofences.model;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import lombok.Getter;

import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

/**
 * Class to represent a geofence which can trigger events.
 * This may be an abstract representation of a fence or an actual registered
 * fence.
 * All concrete instances (via subclasses) must be able to dram themselves
 * onto a GoogleMap.
 */
public abstract class Fence
{

public static Fence
buildCircularFence (String name, LatLng centre, float radius)
{ return new CircularFence (name, centre, radius); }

@Getter
@NonNull
String name;

/**
 * Protected constructor for use by subclasses
 * @param fname The fence name
 */
protected Fence (@NonNull String fname) { name = fname; }

/**
 * Creates a Geofence from an existing Fence
 * @return  The new Geofence
 */
public Geofence createGeofence ()
{
  return createBuilder ().build ();
}

/**
 * Creates a Geofence.Builder to build a Geofence.
 * Note that subclasses will need to override so they can complete the builder
 * by adding the fence perimeter details.
 * @return  The new builder
 */
protected Geofence.Builder createBuilder ()
{
  return new Geofence.Builder ()
      .setRequestId (name)
      .setExpirationDuration (NEVER_EXPIRE)
      .setTransitionTypes ( Geofence.GEOFENCE_TRANSITION_ENTER |
          Geofence.GEOFENCE_TRANSITION_EXIT | GEOFENCE_TRANSITION_DWELL )
      .setLoiteringDelay (5 * 60 * 1000);  // DWELL fires after 5 minutes
}

/**
 * Display this fence on a FenceMap
 * @param map The FenceMap
 */
abstract void showOnMap (FenceMap map);

}

// End of class.
