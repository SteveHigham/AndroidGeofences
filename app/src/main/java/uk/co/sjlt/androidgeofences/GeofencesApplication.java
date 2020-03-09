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
 * This will initialise to false. Without this permission the app is useless
 * and should terminate.
 */
@Getter
@Setter
private boolean foregroundLocationPermission;

/**
 * Identifies whether we have the background location permission.
 * This will initialise to false. For API's before Android 10 (Android Q) this
 * will always be set true. From 10 / Q onwards this depends on the user. We can operate
 * without background locations with reduced functionality.
 */
@Getter
@Setter
private boolean backgroundLocationPermission;
}

// End of class.

