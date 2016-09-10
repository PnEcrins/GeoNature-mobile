# GeoNature-mobile

GeoNature est une application de saisie et de synthèse des observations faune et flore : https://github.com/PnEcrins/GeoNature
GeoNature-mobile permet de saisir 4 de ces protocoles sur appareil mobile Android.

* Saisie contact Faune vertébré
* Saisie contact Faune invertébré
* Saisie contact Faune mortalité
* Saisie Flore prioritaire
* Recherche flore

Démos vidéo des applications :
* Application Android contact faune - http://dai.ly/k4Heui6J10dzcO5ehXf
* Application Android recherche flore - http://dai.ly/k3bCZzAHgSC9yM5eukA

![GeoNature-mobile screenshot](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-contact-faune-nomade.jpg)

![GeoNature-mobile screenshot 2](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-applis.jpg)

![GeoNature-mobile screenshot 3](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-applis-02.jpg)

Pour pouvoir importer les données saisies avec Geonature-mobile dans la BDD PostgreSQL de GeoNature, une web-API doit être installée sur le serveur : https://github.com/PnEcrins/GeoNature-mobile-webapi

La synchronisation de ces données peut être faite par le réseau (wifi ou 3G) ou en connectant le mobile en USB à un PC connecté à Internet. Dans ce cas, une [application de synchronisation des données](https://github.com/PnEcrins/GeoNature-mobile-sync) doit être installée sur le PC.

![GeoNature schema general](https://github.com/PnEcrins/GeoNature/raw/master/docs/images/schema-geonature-environnement.jpg)

Il est possible de déployer les applications Android en utilisant les APK disponibles dans https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/install

Il vous faut ensuite compléter les fichiers JSON de configuration comme indiqué dans la documentation (https://github.com/PnEcrins/GeoNature-mobile/blob/master/docs/configuration_development.md)

Il vous faut aussi intégrer les fichiers SIG (fonds carto au format MBTiles et limites des unités géographiques) sur la carte SD du mobile (https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/install/v1.0.0/external%20card).

La génération des tuiles MBTiles est détaillée dans la documentation (https://github.com/PnEcrins/GeoNature-mobile/blob/master/docs/tuilage_mbtiles.rst)

![GeoNature-mobile illustrations](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/illustration-geonature-mobile.jpg)

## License

&copy; Makina Corpus / Parc national des Ecrins 2012 - 2016
