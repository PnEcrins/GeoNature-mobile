# Development

## Prerequisites

* [JDK 7](https://jdk7.java.net/download.html)
* [Android SDK](http://developer.android.com/sdk/index.html)

## Main features

* Read and manage local tileset as *MBTiles* format (SQLite)
* Use [Leaflet](http://leafletjs.com) for displaying offline maps
* Sync all local data

## Components

* [Leaflet](http://leafletjs.com)
* [Android Support Library](http://developer.android.com/tools/support-library/index.html)

## Full Build

A full build can be executed with the following command:

```
./gradlew clean assemblePneDebug
```

## Release Build

A release type build like it would be necessary for publication of the application
to the Google Play Store and the necessary steps for it is configured.
The following preparation for the execution is necessary:

* Create your key following the instructions at [http://developer.android.com/tools/publishing/app-signing.html#cert](https://developer.android.com/studio/publish/app-signing.html#cert)
* Create a `gradle.properties` file in the same folder of this *INSTALL.md* file like this:

```
STORE_FILE=/absolute/path/to/your.keystore
STORE_PASSWORD=storepassword
KEY_ALIAS=keyalias
KEY_PASSWORD=keypassword
```

After this preparation, the release build can be invoked with the following command:

```
./gradlew clean assemblePneRelease
```

which will in turn sign and zipalign all apks.

## Deploying the application

Ensure that you have a connected device with Android 2.3.x or higher running and
execute the following command after build:

```
./gradlew installPneDebug
```

You can combine a full build and deploy the application in a same command:

```
./gradlew clean installPneDebug
```

# Project information

* Source: [https://github.com/PnEcrins/GeoNature-mobile](https://github.com/PnEcrins/GeoNature-mobile)
* Documentation : [https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs](https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs)

# Offline Maps

Map layers can be provided in [MBTiles](https://www.mapbox.com/help/an-open-platform/#mbtiles) format.
Here is a step-by-step tutorial to create a MBTiles file, from a WMS service.

First, install required tools:

```
sudo apt-get install python-setuptools
sudo easy_install TileCache
sudo easy_install mbutil
```

Configure *TileCache* for your WMS service, in a file named `tilecache.cfg`:

```
[scan]
type=WMSLayer
layers=scan100,scan25
url=http://extranet.parcnational.fr/pnx/wms?
extension=jpg
tms_type=google
srs=EPSG:2154
bbox=700000,6325197,1060000,6617738
maxResolution=1142.7383

[cache]
type=GoogleDisk
base=/tmp/tiles/
```

Retrieved your WMS images as tiles, on the necessary tiles levels:

```
tilecache_seed.py scan 7 12
```

Package the resulting tiles folder in a *MBTiles* file.
First, create a `metadata.json` file in the resulting `/tmp/tiles/scan/` folder
(especially useful for specifying tile files extensions).

```
{
  "name": "Scan",
  "description": "Scan",
  "version": "3",
  "format": "jpeg"
}
```

And run packaging:

```
mb-util /tmp/tiles/scan/ scan.mbtiles
```

# License

&copy; Makina Corpus / Parc national des Ecrins 2012 - 2016
