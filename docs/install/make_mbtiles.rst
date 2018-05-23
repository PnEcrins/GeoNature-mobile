==================================
Création des fonds cartographiques
==================================

Avec MOBAC
==========


Téléchargement et usage
-----------------------

MOBile Atlas Creator (MOBAC) est un logiciel open source écrit en java et permettant de créer des atlas pour GPS ou de nombreuses applications pour smarthphones. Les formats supportés sont nombreux.

[Télécharger MOBAC](http://sourceforge.net/projects/mobac/files/Mobile%20Atlas%20Creator/MOBAC%202.0/Mobile%20Atlas%20Creator%202.0.0.zip/download)

Une fois téléchargé, MOBAC ne s'installe pas, il suffit d'ouvrir le répertoire et de lancer l'application.

[Guide d'utilisation](http://mobac.sourceforge.net/quickstart)

Les fichiers produits sont disponibles dans le répertoire ``atlases``.


Sources personnalisées
----------------------

Il est possible d'ajouter des sources personnalisées dans le répertoire ``mapsources`` de mobac. Vous pouvez notamment utiliser un service wms comme celui de GeoNature pour tuiler les couches disponible via le service wms.


Création du unities.mbtiles
---------------------------

:notes:

	Ce fichier n'est utilisé que par les applications ``fauna`` et ``invertebrate``


* Créer un fichier nommé unities.xml, y copier le contenu ci-dessous en adpatant l'url du service wms et le placer dans le répertoire ``mapsources`` de MOBAC.

  ::  
  
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<customWmsMapSource>
		   <name>unites geographiques</name>
		   <minZoom>0</minZoom>
		   <maxZoom>18</maxZoom>
		   <tileType>PNG</tileType>
		   <version>1.1.1</version>
		   <layers>unitesgeo</layers>
		   <url>http://localhost/wmsgeonature?</url>
		   <coordinatesystem>EPSG:4326</coordinatesystem>
		   <aditionalparameters><![CDATA[&TRANSPARENT=true]]></aditionalparameters>
		</customWmsMapSource>

* Fermer et ouvrir de nouveau MOBAC
* Une nouvelle source de carte doit être disponible.
* Atlas ‐> Nouvel atlas 
* Donner un nom à l’atlas par exemple ``unities``
* Choisir le format de l’atlas (MBTiles SQLite)
* choisir la source de la carte ``unites geographiques``
* Cocher les niveaux de zoom de 6 à 18
* Définir la zone de l’atlas 
	* Sélection->Mode de sélection->Polygonale
	* dessiner le contour de la zone à tuiler
* Donner un nom à la sélection 
* Cliquer sur ``Ajouter la sélection``
* Cocher `` Recréer/ajuster les dalles`` et choisir le format des dalles ``PNG``
* Cliquer sur le bouton ``Créer l’atlas`` 

Une fois que le fichier est créer il se trouve dans le répertoire ``atlases``.

Il est possible d'ouvrir le fichier avec un éditeur sqlite et supprimer toutes les tuiles vides dont la taille est de 334 octets avec la commande SQL suivante.

  ::  
  
        DELETE FROM tiles WHERE length(tile_data) = 334;

Vous devrez ensuite utiliser le menu "système-> optimiser" (logiciel sqliteman) pour réellement réduire la taille de la base de données du fichier mbtiles.


Création des scan.mbtiles et ortho.mbtiles
------------------------------------------

:notes:

	Seul le fichier ``scan.mbtiles`` est obligatoire pour localiser les observations. Si vous n'utilisez pas de fichier ortho.mbtiles, pensez à retirer sa déclaration des fichiers de settings de l'application. Vous pouvez également utiliser d'autres sources à votre convenance.

Si vous disposez de fichiers raster que vous souhaitez tuiler (scan IGN, ortho, mnt, etc...), vous devez les rendre disponibles dans le service wms de GeoNature ou sur n'importe quel autre service wms accessible par MOBAC. Sur le modèle présenté ci-dessus, vous devez ensuite créer un fichier xml par source et le placer dans le répertoire ``mapsources`` de MOBAC. 

Pour tuiller des fonds raster, sans transparence, voici la procédure légèrement modifiée.

* Atlas ‐> Nouvel atlas 
* Donner un nom à l’atlas par exemple ``scan``
* Choisir le format de l’atlas (MBTiles SQLite)
* choisir la source de la carte ``ma source scan``
* Cocher les niveaux de zoom de 6 à 16 (à titre indicatif le scan25 de l'IGN correspond au niveau 15 et 16)
* Définir la zone de l’atlas 
	* Sélection->Mode de sélection->Polygonale (ou rectangulaire selon le besoin)
	* dessiner le contour de la zone à tuiler
* Donner un nom à la sélection 
* Cliquer sur ``Ajouter la sélection``
* Cocher `` Recréer/ajuster les dalles`` et choisir le format des dalles ``JPEG - qualité 60`` (à vous de choisir un compromis acceptable entre taille finale du fichier mbtiles et qualité des images des tuiles de la carte)
* Cliquer sur le bouton ``Créer l’atlas`` 


:notes:

	Il est possible de tuiler le service WMTS de l'IGN. Pour cela, vous devez disposer des droits pour le faire ainsi que d'une clé IGN référençant l'adresse du poste sur lequel MOBAC travaille.
