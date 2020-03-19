package uk.co.sjlt.androidgeofences;

import java.util.Date;

import androidx.annotation.NonNull;
import lombok.Getter;

/**
 * Class to store the relavent details aboiut a Geofencing Event
 */
class FenceEvent
{
@Getter
private final @NonNull Date timestamp;

@Getter
private final @NonNull String fence;

@Getter
private final int event;

FenceEvent (@NonNull Date t, @NonNull String f, int i)
{
  timestamp = t;
  fence = f;
  event = i;
}

public @NonNull String toString ()
{
  return "{Time: " + timestamp + ", Fence: " + fence + ", Event: " + event +
      "}";
}

}

// End of class.