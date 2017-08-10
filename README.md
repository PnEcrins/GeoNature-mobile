# GeoNature-mobile

![GeoNature-mobile illustrations](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/illustration-geonature-mobile.jpg)

GeoNature est une application de saisie et de synthèse des observations faune et flore : https://github.com/Pnx-SI/GeoNature
GeoNature-mobile permet de saisir 4 de ces protocoles sur appareil mobile Android + une application de recherche dédiée à la flore.

* Saisie contact Faune vertébré
* Saisie contact Faune invertébré
* Saisie contact Faune mortalité
* Saisie Flore prioritaire
* Recherche flore

Démos
=====

 Vidéos
 ------
* Application Android contact faune - http://dai.ly/k4Heui6J10dzcO5ehXf
* Application Android recherche flore - http://dai.ly/k3bCZzAHgSC9yM5eukA

 Screenshots
 -----------
![GeoNature-mobile screenshot](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-contact-faune-nomade.jpg)

![GeoNature-mobile screenshot 2](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-applis.jpg)

![GeoNature-mobile screenshot 3](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/master/docs/images/screen-applis-02.jpg)


Concepts
========

La production de données d'observation avec GeoNature-mobile repose sur 4 briques indissociables constituant une chaine de travail complète (une de ces 4 briques est facultative)

* GeoNature et sa base de données (stockage + consultation et saisie web)
* GeoNature-mobiles (saisie Android)
* Geonature-mobile-webapi (Lien entre la base de données GeoNature et les applications Android)
* GeoNature-mobile-sync (Lien entre le terminal Android et la webapi : synchronisation des données + mise à jour applicative)


Pour pouvoir importer les données saisies avec Geonature-mobile dans la BDD PostgreSQL de GeoNature, une web-API doit être installée sur un serveur : https://github.com/PnEcrins/GeoNature-mobile-webapi
Cette webapi permet à la fois d'importer les données saisies mais aussi d''exporter vers les applications mobile les données nécessaires au fonctionnement des applications (listes des observateurs, listes des taxons, contenu des listes déroulantes)

La synchronisation de ces données peut être faite par le réseau (wifi ou 3G). Dans ce cas GeoNature-mobile-sync n'est pas indispensable.
Il est également possible de connecter le terminal mobile en USB à un PC connecté à Internet. Dans ce cas, une [application de synchronisation des données](https://github.com/PnEcrins/GeoNature-mobile-sync) doit être installée sur le PC. Cette application enrichi les fonctionnalités de la synchronisation : 
	* unification de la synchronisation des 5 applications Android en un seul clik
	* mise à jour applicative (si une nouvelle version des applications ou une nouvelle configuration doit être déployée sur une flotte de terminaux Android)
	* backup des saisies
	* log des erreurs
l'usage de cette application est fortement recommandée dans le cadre d'une flotte importante de terminaux et/ou d'utilisateurs peu à l'aise avec les outils informatiques.

![GeoNature schema general](https://github.com/PnEcrins/GeoNature/raw/master/docs/images/schema-geonature-environnement.jpg)

Mise en place
=============

Il est possible de déployer les applications Android en utilisant les APK disponibles dans https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/install

Il vous faut ensuite compléter les fichiers JSON de configuration comme indiqué dans la documentation (https://github.com/PnEcrins/GeoNature-mobile/blob/master/docs/configuration_development.md)

GeoNature-mobile est concu pour un usage hors ligne. Il faut donc embarquer les fonds cartographique nécessaire à la localisation des observations ainsi que les limites des unités géographiques. Ces fonds doivent être produits au format MBTiles et et copiés sur la carte SD du terminal (https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/install/v1.2.0/external%20card).

La génération des tuiles MBTiles est détaillée dans la documentation (https://github.com/PnEcrins/GeoNature-mobile/blob/master/docs/tuilage_mbtiles.rst)


License
=======

&copy; Makina Corpus / Parc national des Ecrins 2012 - 2017
