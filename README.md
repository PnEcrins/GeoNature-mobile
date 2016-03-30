# GeoNature-mobile
[Mobile applications](https://github.com/PnEcrins/GeoNature-mobile) about:

* Fauna (Saisie contact Faune vertébré)
* Invertebrate (Saisie contact Faune invertébré)
* Mortality (Saisie contact Faune mortalité)
* Flora (Saisie Flore prioritaire)
* Search (Recherche flore)

GeoNature est une application de saisie et de synthèse des observations faune et flore : https://github.com/PnEcrins/GeoNature

GeoNature-mobile permet de saisir 4 de ces protocoles sur appareil mobile Android.

![GeoNature-mobile screenshot](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-contact-faune-nomade.jpg)

![GeoNature-mobile screenshot 2](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-applis.jpg)

![GeoNature-mobile screenshot 3](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-applis-02.jpg)

Pour pouvoir importer les données saisies avec Geonature-mobile dans la BDD PostgreSQL de GeoNature, une web-API doit être installée sur le serveur : https://github.com/PnEcrins/GeoNature-mobile-webapi

La synchronisation de ces données peut être faite par le réseau (wifi ou 3G) ou en connectant le mobile en USB à un PC connecté à internet. Dans ce cas, une application de synchronisation des données soit être installée sur le PC : https://github.com/PnEcrins/GeoNature-mobile-sync 

![GeoNature schema general](https://github.com/PnEcrins/GeoNature/raw/master/docs/images/schema-geonature-environnement.jpg)

Il est possible de déployer les applications Android en utilisant les APK disponibles dans https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/install

Il vous faut ensuite compléter les fichiers JSON de configuration comme indiqué dans la documentation (https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/configuration_development.odt)

Il vous faut aussi intégrer les fichiers SIG (fonds carto au format MBTILES et limites des unités géographiques) sur la carte SD du mobile (https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/install/V1.0.0/external%20card).

La génération des tuiles MBTILES est détaillée dans la documentation (https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/tuilage-mbtiles.txt)

![GeoNature-mobile illustrations](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/illustration-geonature-mobile.jpg)

# Development

## Prerequisites
* [JDK 7](https://jdk7.java.net/download.html)
* [Android SDK](http://developer.android.com/sdk/index.html)

## Main features
* Read and manage local tileset as *MBTiles* format (SQLite)
* Use [Leaflet](http://leafletjs.com/) for displaying offline maps
* Sync all local data

## Components
* [Leaflet](http://leafletjs.com/)
* [Android Support Library](http://developer.android.com/tools/support-library/index.html)
* [ViewPagerIndicator](http://viewpagerindicator.com/)

## Full Build
A full build can be executed with the following command:

```
./gradlew clean assembleDebug
```

## Release Build
A release type build like it would be necessary for publication of the application to the Android
market and the necessary steps for it is configured.
The following preparation for the execution is necessary:

* Create your key following the instructions at [http://developer.android.com/tools/publishing/app-signing.html#cert](http://developer.android.com/tools/publishing/app-signing.html#cert)
* Create a ``gradle.properties`` file in the same folder of this *README.md* file like this:

```
STORE_FILE=/absolute/path/to/your.keystore
STORE_PASSWORD=storepassword
KEY_ALIAS=keyalias
KEY_PASSWORD=keypassword
```

After this preparation, the release build can be invoked with the following command:

```
./gradlew clean assembleRelease
```

which will in turn sign and zipalign all apks.

## Deploying the application
Ensure that you have a connected device with Android 2.3.x or higher running and execute the
following command after build:

```
./gradlew installDebug
```

You can combine a full build and deploy the application in a same command:

```
./gradlew clean installDebug
```

## Project information
* Source: [https://github.com/PnEcrins/GeoNature-mobile](https://github.com/PnEcrins/GeoNature-mobile)
* Documentation : [https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs](https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs)

## Offline Maps
Map layers can be provided in [MBTiles](http://mapbox.com/developers/mbtiles/) format.
Here is a step-by-step tutorial to create a MBTiles file, from a WMS service.

First, install required tools:

```
sudo apt-get install python-setuptools
sudo easy_install TileCache
sudo easy_install mbutil
```

Configure *TileCache* for your WMS service, in a file named ``tilecache.cfg``:

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
First, create a ``metadata.json`` file in the resulting ``/tmp/tiles/scan/`` folder (especially useful for specifying tile files extensions).

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

## License
&copy; Makina Corpus 2012 - 2015
