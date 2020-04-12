package uk.co.sjlt.androidgeofences.model;

import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * This is effectively a utility map to make Fence specific functionality
 * available on a GoogleMap. I don't think we can easily inherit from
 * GoogleMap.
 */
public class FenceMap
{
private GoogleMap map;

public FenceMap (GoogleMap gmap) { map = gmap; }

/**
 * Show a circular fence on a GoogleMap
 * @param   fence CircularFence   The fence to show
 * @return        FenceMap        this to allow chaining
 */
FenceMap showCircularFence (CircularFence fence)
{
  showCircularMarker ( fence.getName (), fence.getCentre (),
      fence.getRadius (), Color.GREEN );
  return this;
}

/**
 * Show a list of fences on a GoogleMap
 * @param   fences  List<Fence>   Fences to show
 * @return          FenceMap      this to allow chaining
 */
public FenceMap showFences (List<Fence> fences)
{
  for (Fence fence : fences)
  { fence.showOnMap (this); }
  return this;
}

/**
 * Show a location on a GoogleMap.
 * The location will be ringed with a black circle showing the accuracy
 * @param   name      String    Name of location
 * @param   location  Location  The location to show
 * @return            FenceMap  this to allow chaining
 */
public FenceMap showLocation (String name, Location location)
{
  // Show the location as a marker
  LatLng position =
      new LatLng (location.getLatitude (), location.getLongitude ());
  showCircularMarker (name, position, location.getAccuracy (), Color.BLACK);
  return this;
}

/**
 * Helper method to show a marker with a surrounding circle if the radius is
 * greater than 1 metre.
 * @param name      String  Name of location - to be displayed
 * @param position  LatLng  Position of location
 * @param radius    float   Radius of surrounding circle
 * @param colour    int     Color of surrounding circle
 */
private void
showCircularMarker (String name, LatLng position, float radius, int colour)
{
  // Show the marker
  MarkerOptions mOptions =
      new MarkerOptions ().position (position).title (name);
  map.addMarker (mOptions);

  // Show the surrounding circle
  if (radius > 1f)
  {
    CircleOptions cOptions = new CircleOptions ()
        .center (position)
        .radius (radius)
        .strokeColor (colour);
    map.addCircle (cOptions);
  }
}

}

// End of class.

