# AndroidGeofences
Sample Java source code for experimenting with geofencing and location
API's. This is purely a developer tool for experimentation. It is not
a completed application.

## Getting Started
This project should be built with Android Studio. I suspect you can
also do a straight gradle build but I havent done this myself.

Before building you need to create a file
../secrets/AndroidGeofences.properties

This file must contain the following line:
ANDROID-GEO-API_KEY=<YOUR API KEY>

You need to add in your own API key (unquoted). If this is not a valid
Google API key then the "Show Location" functionality will not
work. This allows you to see a map showing your location and any
nearby geofences which are set up.

For help on this subject see:
https://developers.google.com/maps/documentation/android-sdk/get-api-key

In addition you need to add in (hard coded) any geofences you want to
experiement with. Currently there is one fence in place which is near
my house. Probably not much good to you. The fence should be added in
GeofencesApplication.onCreate ().

### Prerequisites
You will need Android Studio and some Java knowledge.
Fot the "Show Location" functionality you will need a Google Maps SDK
for Android API key.

### Installing
You will need to build the application in Android Studio. You can then
run the appliation in an emulator or build a package for a real
device.
This is not a completed application. In particular it does not explain
the permissions requested or the rationale for requesting these
permissions.

## Contributing
I haven't done this before so please help me out. I guess you request
a pull-request in the normal manner and I will do my best to oblige.

## Authors
Steve Higham - steve@sjlt.co.uk

## License
This project is licensed under the MIT License - see the [LICENSE]
(LICENSE) file for details.

## Acknowledgements
Thanks to Paulo for the idea and encouregment to investigate
geofencing.
