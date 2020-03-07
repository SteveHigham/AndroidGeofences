package uk.co.sjlt.androidgeofences;

import android.app.Application;

import lombok.Getter;
import lombok.Setter;

/**
 * Main AndroidGeofences application object
 */
public class GeofencesApplication extends Application
{
/**
 * Identifies whether we have the foreground location permission.
 * This will initialise to false.
 */
@Getter
@Setter
private boolean foregroundLocationPermission;

/**
 * Identifies whether we have the background location permission.
 * This will initialise to false.
 */
@Getter
@Setter
private boolean backgroundLocationPermission;
}

// End of class.

