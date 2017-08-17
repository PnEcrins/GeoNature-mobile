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
* Geonature-mobile-webapi (lien entre la base de données GeoNature et les applications Android)
* GeoNature-mobile-sync (lien entre le terminal Android et la webapi : synchronisation des données + mise à jour applicative)


Pour pouvoir importer les données saisies avec Geonature-mobile dans la BDD PostgreSQL de GeoNature, une web-API doit être installée sur un serveur : https://github.com/PnEcrins/GeoNature-mobile-webapi
Cette webapi permet à la fois d'importer les données saisies mais aussi d''exporter vers les applications mobiles les données nécessaires au fonctionnement des applications (listes des observateurs, listes des taxons, contenu des listes déroulantes)

La synchronisation de ces données peut être faite par le réseau (wifi ou 3G). Dans ce cas GeoNature-mobile-sync n'est pas indispensable.

Il est également possible de connecter le terminal mobile en USB à un PC connecté à Internet. Dans ce cas, [GeoNature-mobile-sync](https://github.com/PnEcrins/GeoNature-mobile-sync) doit être installée sur le PC. Cette application enrichi les fonctionnalités de la synchronisation : 
* unification de la synchronisation des 5 applications Android en un seul clik
* mise à jour applicative (si une nouvelle version des applications ou une nouvelle configuration doit être déployée sur une flotte de terminaux Android)
* backup des saisies
* log des erreurs

L'usage de cette application est fortement recommandée dans le cadre d'une flotte importante de terminaux et/ou d'utilisateurs peu à l'aise avec les outils informatiques.

![GeoNature schema general](https://github.com/PnEcrins/GeoNature/raw/master/docs/images/schema-geonature-environnement.jpg)


Mise en place
=============

L'usage de GeoNature-mobile nécessite la mise en place d'une chaine de travail complète. Il serait illusoire de produire des données avec un terminal Android sans pouvoir les exporter ou les consulter en dehors de ce terminal. 
Il est également important de savoir que GeoNature-mobile est concu pour un usage hors ligne. Il faut donc embarquer les fonds cartographique nécessaire à la localisation des observations ainsi que les limites des unités géographiques. Ces fonds doivent être produits au format MBTiles puis copiés sur la carte SD du terminal. Ces fonds peuvent être lourds et nécessiter un espace de stockage importants (plusieurs giga-octets).
La chaine de travail est complexe et nécessite une bonne compréhension du rôle et de la configuration de chacune des briques qui la composent. Vous trouverez [ici](https://github.com/PnEcrins/GeoNature-mobile/blob/develop/docs/workflow-geonature-mobile.doc?raw=true) une présentation du workflow de cette chaine de travail.

La mise en place de la chaine de travail passe par les étapes suivantes
-----------------------------------------------------------------------

**Installation et configuration de GeoNature et de sa base de données**

https://github.com/PnX-SI/GeoNature/tree/master/docs

**Installation et configuration des application Android**

https://github.com/PnEcrins/GeoNature-mobile/blob/master/docs/install/v1.2.0/installation.rst

* production des fonds cartographiques
* production d'un fichier unities.wkt pour les applications ``fauna`` et ``invertebrate`` = contact faune et contact invertébrés
* configuration des fichiers de settings des applications (url de synchronisation, paramètres carto, déclaration des fonds cartographiques)
* installation des apk, des fonds et des settings sur les terminaux Android

**Installation et configuration de la webapi**  sur un serveur ayant une connexion à la base de données GeoNature

https://github.com/PnEcrins/GeoNature-mobile-webapi/blob/master/docs/installation.md

* configuration de l'accès à la base de données
* configuration générale (token, chemin d'accès aux fichiers apk des applications, chemin d'accès aux fichiers de settings des applications)
* copie des apk des applications Android et d'un fichier version.json dans le répertoire ``apk`` de l'api (uniquement si usage de GeoNature-mobile-sync)
* copie des fichiers json de settings des applications Android dans le répertoire ``datas`` de l'api (uniquement si usage de GeoNature-mobile-sync)
* installation de l'application
* configuration apache

**Synchronisation**

Pour un premier usage des applications mobiles, une synchronisation avec la base de données est nécessaire pour produire le fichier data.db. Ce fichier contient les données nécessaires au fonctionnement des applications. Il est produit par la webapi à partir des informations contenues dans la base de données de Geonature.
Il est possible de synchroniser directement depuis la page d'accueil des applications (une connexion Internet est nécessaire) ou depuis l'application GeoNature-mobile-sync.

**Installation et configuration de GeoNature-mobile-sync** (facultatif mais recommandé)

* installation (.exe pour windows ou .deb pour linux Debian ou Ubuntu)
* configuration du fichier server.json (url de la webapi, token et organisme dans le cas d'un usage multi-organisme)


Développement
=============

https://github.com/PnX-SI/GeoNature/tree/master/docs/developpement/


License
=======

&copy; Makina Corpus / Parc national des Ecrins 2012 - 2017
