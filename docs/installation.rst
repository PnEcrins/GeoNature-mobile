Il est possible de déployer les applications Android en utilisant les APK disponibles dans https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/install

Il vous faut ensuite compléter les fichiers JSON de configuration comme indiqué dans la documentation (https://github.com/PnEcrins/GeoNature-mobile/blob/master/docs/configuration_development.md)

GeoNature-mobile est concu pour un usage hors ligne. Il faut donc embarquer les fonds cartographique nécessaire à la localisation des observations ainsi que les limites des unités géographiques. Ces fonds doivent être produits au format MBTiles et et copiés sur la carte SD du terminal (https://github.com/PnEcrins/GeoNature-mobile/tree/master/docs/install/v1.2.0/external%20card).

La génération des tuiles MBTiles est détaillée dans la documentation (https://github.com/PnEcrins/GeoNature-mobile/blob/master/docs/tuilage_mbtiles.rst)