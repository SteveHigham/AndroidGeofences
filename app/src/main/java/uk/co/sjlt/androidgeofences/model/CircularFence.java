package uk.co.sjlt.androidgeofences.model;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import lombok.Getter;

/**
 * Concrete fence for circular fences.
 */
class CircularFence extends Fence
{

/**
 * Circle centre as a GoogleMaps LatLng
 */
@Getter
LatLng centre;

/**
 * Radius in metres
 */
@Getter
float radius;

/**
 * Package constructor exposed by Fence.buildCircularFence
 * @param name  String  Fence name
 * @param c     LatLng  Centre of fence
 * @param r     float   Radius of fence in metres
 */
CircularFence (String name, LatLng c, float r)
{
  super (name);
  centre = c;
  radius = r;
}

@Override
protected Geofence.Builder createBuilder ()
{
  return super.createBuilder ()
      .setCircularRegion (centre.latitude, centre.longitude, radius);
}

@Override
void showOnMap (FenceMap map)
{ map.showCircularFence (this); }

}

// End of class.
