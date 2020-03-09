package uk.co.sjlt.androidgeofences;

import android.app.Application;

import lombok.Getter;
import lombok.Setter;

/**
 * Main AndroidGeofences application object.
 * I use hasXxxPermission boolean variables to force Lombok to create hasXxx
 * getters, otherwise Lombok defaults to isXxx getters.
 * Kind of works. Now we have isHas... getters. I thgink this will be fixed
 * in a future Lombok release.
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
private boolean hasForegroundLocationPermission;

/**
 * Identifies whether we have the background location permission.
 * This will initialise to false. For API's before Android 10 (Android Q) this
 * will always be set true. From 10 / Q onwards this depends on the user. We can operate
 * without background locations with reduced functionality.
 */
@Getter
@Setter
private boolean hasBackgroundLocationPermission;
}

// End of class.

