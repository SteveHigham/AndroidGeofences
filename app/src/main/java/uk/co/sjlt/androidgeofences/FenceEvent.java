package uk.co.sjlt.androidgeofences;

import java.util.Date;

import lombok.Getter;

/**
 * Class to store the relavent details aboiut a Geofencing Event
 */
class FenceEvent
{
@Getter
private Date timestamp;

@Getter
private String fence;

@Getter
int event;

FenceEvent (Date t, String f, int i)
{
  timestamp = t;
  fence = f;
  event = i;
}

}

// End of class.