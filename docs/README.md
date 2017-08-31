# DEPLOIEMENT

L'usage de GeoNature-mobile nécessite la mise en place d'une chaine de travail complète. Il serait illusoire de produire des données avec un terminal Android sans pouvoir les exporter ou les consulter en dehors de ce terminal. 

Il est également important de savoir que GeoNature-mobile est concu pour un usage hors ligne. Il faut donc embarquer les fonds cartographique nécessaire à la localisation des observations ainsi que les limites des unités géographiques. Ces fonds doivent être produits au format MBTiles puis copiés sur la carte SD du terminal. Ces fonds peuvent être lourds et nécessiter un espace de stockage importants (plusieurs giga-octets).

La chaine de travail est complexe et nécessite une bonne compréhension du rôle et de la configuration de chacune des briques qui la composent. Vous trouverez ci-dessous 2 schémas de présentation du workflow de cette chaine de travail.

Le premier présente la synchronisation avec GeoNature-mobile-sync installé sur le poste de travail de l'utilisateur (synchro USB) : 

![GeoNature-mobile workflow synchro desktop](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/develop/docs/images/workflow-synchronisation-desktop.jpg)

Le second présente la synchronisation avec par le réseau (synchro WIFI ou 3G/4G) : 

![GeoNature-mobile workflow synchro wifi](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/develop/docs/images/workflow-synchronisation-wifi.jpg)

Etapes de déploiement de la chaine de travail
---------------------------------------------

**1. Installation et configuration de GeoNature et de sa base de données**

Voir la doc : https://github.com/PnX-SI/GeoNature/tree/master/docs

**2. Installation et configuration des application Android**

* Production des fonds cartographiques. [Voir la documentation dédiée](/docs/install/make_mbtiles.rst)
* Production d'un fichier ``unities.wkt`` pour les applications ``fauna`` et ``invertebrate`` = contact faune et contact invertébrés. 
[Voir la documentation dédiée](/docs/install/make_wkt.rst)
* Installation des apk, des fonds et des settings sur les terminaux Android + configuration des fichiers de settings des applications 
(URL de synchronisation, paramètres carto, déclaration des fonds cartographiques). 
[Voir la documentation dédiée](/docs/install/installation.rst)

**3. Installation et configuration de la webapi** sur un serveur ayant une connexion à la base de données GeoNature

* Configuration de l'accès à la base de données
* Configuration générale (token, chemin d'accès aux fichiers apk des applications, chemin d'accès aux fichiers de settings des applications)
* Copie des APK des applications Android et d'un fichier version.json dans le répertoire ``apk`` de l'api (uniquement si usage de GeoNature-mobile-sync)
* Copie des fichiers json de settings des applications Android dans le répertoire ``datas`` de l'api (uniquement si usage de GeoNature-mobile-sync)
* Installation de l'application
* Configuration Apache

Voir la doc détaillée : 
https://github.com/PnEcrins/GeoNature-mobile-webapi/blob/master/docs/installation.md

**4. Synchronisation**

Pour un premier usage des applications mobiles, une synchronisation avec la base de données est nécessaire pour produire le fichier data.db. Ce fichier contient les données nécessaires au fonctionnement des applications. Il est produit par la webapi à partir des informations contenues dans la base de données de Geonature.

Il est possible de synchroniser directement depuis la page d'accueil des applications (une connexion Internet est nécessaire) ou depuis l'application GeoNature-mobile-sync.

**5. Installation et configuration de GeoNature-mobile-sync** (facultatif mais recommandé)

* Installation (.exe pour windows ou .deb pour linux Debian ou Ubuntu)
* Configuration du fichier ``server.json`` (url de la webapi, token et organisme dans le cas d'un usage multi-organisme)


# Développement

[Voir la documentation de développement](/docs/developpement/)
