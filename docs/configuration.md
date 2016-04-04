# Applications de saisies

## Pr�-requis

* JDK 7
* Android Studio

## G�n�ration des APKs

### Signature des applications

Si aucun certificat n'a �t� cr��, consulter la page suivante�:
http://developer.android.com/tools/publishing/app-signing.html#cert

Ensuite � la racine du projet, ajouter le fichier `gradle.properties` et ajouter les lignes suivantes en compl�tant les valeurs selon le certificat � utiliser�:
```
STORE_FILE=/absolute/path/to/your.keystore
STORE_PASSWORD=storepassword
KEY_ALIAS=keyalias
KEY_PASSWORD=keypassword
```

###Build des applications

Une fois fait, on peut lancer le build global pour g�n�rer toutes les applications�:
```
./gradlew clean assembleRelease
```

Les APKs g�n�r�s se trouvent dans le r�pertoire `build/outputs/apk/` de chaque module�:
* fauna�: fauna-release-<version>.apk
* flora�: flora-release-<version>.apk
* invertebrate�: invertebrate-release-<version>.apk
* mortality�: mortality-release-<version>.apk
* search�: search-release-<version>.apk

## Fichiers de configuration des applications

Chaque application poss�de son propre fichier de configuration au format JSON. Ce fichier est lu une fois � chaque d�marrage de l'application. Il doit suivre le nommage suivant�:
```
settings_<nom_du_module>.json
```

Ce qui donne�:
* fauna�: settings_fauna.json
* flora�: settings_flora.json
* invertebrate�: settings_invertebrate.json
* mortality�: settings_mortality.json
* search�: settings_search.json

Chaque fichier de configuration doit suivre la structure suivante�:
```
{
	"sync":
	{
		"url": "http://domain.my/webapi/",
		"token": 666,
		"status_url": "status/",
		"import_url": "import/",
		"exports":
		[
			{
				"url": "export/sqlite/",
				"file": "databases/data.db"
			},
			...
		]
	},
	"map":
	{
		"bbox": [914987, 6372012, 994987, 6460012],
		"max_bounds": [[43.9873, 5.2489], [45.6652, 7.1111]],
		"center": [44.795154, 6.228655],
		"start_zoom": 2,
		"min_zoom_pointing": 7,
		"layers":
		[
			{
				"name": "scan.mbtiles",
				"label": "Scan",
				"source": "mbtiles"

			},
			...
		]
	}
}
```

Param�tre | Description
--------- | -----------
url | URL du serveur de synchronisation
token | Jeton d'identification � utiliser sur le serveur de synchronisation
status_url | URL � utiliser pour v�rifier le statut du serveur de synchronisation
import_url | URL de l'import � utiliser pour synchroniser les donn�es de saisies des applications mobiles
exports | Liste des fichiers � r�cup�rer cot� serveur de synchronisation et � copier sur le terminal
exports / url | URL du fichier � r�cup�rer et � copier sur le terminal
exports / file | Chemin (optionnel) et nom du fichier � utiliser pour copier le fichier r�cup�r� cot� serveur de synchronisation
map / layers | Liste des fichiers des couches de donn�es � afficher sur la carte
map / layers / name | Nom de la source de donn�es (fichier ou r�pertoire) de la couche de donn�es
map / layers / label | Nom de la couche de donn�es
map / layers / source | Type de sources de donn�es�: mbtiles (format MBTiles), mbtiles_split, dir (r�pertoire de tuiles), http (non impl�ment�)

Certaines applications ajoutent sp�cifiquement des attributs suppl�mentaires.

**fauna** et **invertebrate**�:

Param�tre | Description
--------- | -----------
map / unity_layer | Couche de donn�es suppl�mentaire (vectorielle, format WKT) � afficher sur la carte

**search**�:

Param�tre | Description
--------- | -----------
search / max_radius | Rayon de recherche (en m) maximal autour d'une position donn�e
search / default_radius | Rayon de recherche (en m) par d�faut autour d'une position donn�e
search / max_features_found | Le nombre maximal d'�l�ments � afficher lors d'une recherche autour d'une position donn�e


# Architecture g�n�rale

Le projet actuel contient deux biblioth�ques Android partag�es par toutes les applications mobiles�:
* commons
* maps

Les applications mobiles s'appuient donc sur ces modules pour int�grer les fonctionnalit�s communes. Ces deux biblioth�ques restent ind�pendantes l'une de l'autre.

## Module commons

Ce module est une biblioth�que Android offrant les fonctionnalit�s et services communs � l'ensemble des applications mobiles�:
* Gestion des fichiers de configuration des applications mobiles
* Gestion des donn�es locales avec notamment la d�tection des diff�rents points de montage pr�sent sur les terminaux pour exploiter les cartes SD externes 
* Gestion de la synchronisation des donn�es locales
* Gestion du workflow de saisie

### Services Android d�clar�s

Le module commons offre un ensemble de Service Android pour les applications�:
* SettingsService�: Pour charger au d�marrage de chaque application son fichier de configuration ainsi qu'�ventuellement d'autres donn�es (comme le fichier unities.wkt)
* CheckServerService�: Pour v�rifier si le serveur de synchronisation est disponible ou non
* SyncService�: Service de synchronisation des donn�es locales
* SyncCommandIntentService�: Permet d'ex�cuter des commandes en dehors des applications. Ce service est utilis� par l'application de synchronisation desktop pour�:
	* R�cup�rer les informations relatives � chaque application install�e sur le terminal (ID de l'application, num�ro de version, etc.)
	* Supprimer les saisies stock�es localement sur le terminal une fois que la synchronisation s'est bien d�roul�e via l'application de synchronisation desktop
	* D�placer les fichiers � synchroniser au bon emplacement sur le terminal si celui-ci poss�de une carte SD externe. Cette commande est importante car l'application de synchronisation desktop embarque un client adb pour communiquer avec le terminal en USB et de pouvoir ex�cuter des commandes. Mais il n'a pas les permissions suffisantes pour pouvoir acc�der en �criture � la carte SD externe du terminal si celui-ci en poss�de une. Donc la synchronisation des fichiers se fait en deux temps dans ce cas l�: Les fichiers � synchroniser son copi�s localement sur le terminal dans l'espace de stockage interne puis l'application de synchronisation desktop lance la commande permettant de d�placer ces fichiers au bon endroit sur le terminal. Le service SyncCommandIntentService est invoqu� via cette commande pour d�placer des fichiers.
	Cette partie pourrait �tre simplifi�e en compl�tant l'application de synchronisation desktop avec un client MTP pour pouvoir acc�der directement � l'ensemble de l'espace de stockage du terminal et donc de pouvoir acc�der en lecture / �criture � la carte SD externe du terminal. Le client adb fera toujours le m�me travail mais d�l�guera les op�rations de copie au client MTP.

Le module commons poss�de un nouveau package appel� service avec un nouveau Service Android RequestHandlerService. Ce service se veut plus simple � l'utilisation car il poss�de son propre client pour pouvoir l'utiliser. L'avantage est de n'avoir plus qu'un seul service de d�clar� pour toutes les applications et l'id�e � terme est de remplacer tous les services list�s ci-dessus par des requ�tes pouvant �tre jou� via ce nouveau service � travers son client. Cette nouvelle approche o� les appels se font � travers le client permet de supprimer tout le code qui s'occupe de la gestion des services Android.

Actuellement, il n'est pas vraiment termin� et n'est utilis� qu'� titre d'exemple dans l'application search en remplacant le service SettingsService par son �quivalent LoadSettingsRequestHandler.

## Module maps

Ce module est une biblioth�que Android g�rant la partie cartographique des applications mobiles. Elle embarque notamment un composant Android WebView permettant de g�rer l'int�gration de la biblioth�que Javascript [Leaflet](http://leafletjs.com/). Ce module est compl�tement ind�pendant du reste et ne d�pend pas du module commons.

### Organisation du module

* assets�: Ensemble des sources Javascript, notamment la biblioth�que cartographique Leaflet.
* content�: Gestion des sources de donn�es � afficher (MBTiles, r�pertoire, etc.)
* control�: Contr�leurs (UI) de la carte (zoom, position centr�e autour de la position courante, etc.)
* geojson�: Objets GeoJSON

A la racine , on trouve le fragment g�n�rique AbstractWebViewFragment permettant d'afficher la carte via un composant Android WebView.

### Gestion des sources de donn�es

Le module maps offre trois sources possibles d'acc�s aux sources de donn�es�:
* dir�: Permet de lire les tuiles directement sur l'espace de stockage du terminal selon un r�pertoire donn� (pyramide de tuiles)
* mbtiles�: Permet de lire les tuiles selon un fichier MBTiles.
* mbtiles_split�: Permet de lire les tuiles selon un ensemble de fichiers MBTiles �clat�s selon le param�tre x (en colonne).
* http�: Cette quatri�me source est simplement d�clar�e mais non impl�ment�e.

### Contr�leurs

Le module maps offre en standard plusieurs contr�leurs.

**MainControl**

Contr�leur par d�faut et obligatoire. Il permet de charger les autres contr�leurs d�clar�s et offre�:
* La gestion de la position courante de l'utilisateur
* Acc�der aux attributs de la carte (Limites, centre de la carte, le zoom courant, le zoom minimal et maximal)
* Acc�der � la couche de donn�e courante
* R�cup�rer les tuiles selon les coordonn�es

**CenterPositionControl**

Contr�leur g�n�rique permettant d'ajouter sur la carte un bouton permettant de centrer automatiquement la carte selon la position courante de l'utilisateur.

**DrawControl**

Contr�leur g�n�rique permettant d'ajouter sur la carte une barre de bouton permettant la saisie de g�om�tries (points, lignes et polygones), et de permettre leurs �ditions (d�placement, ajout, suppression et modification de la g�om�trie en ajoutant, d�pla�ant et en supprimant des points).

**FeaturesControl**

Contr�leur g�n�rique permettant d'ajouter une couche de donn�es suppl�mentaire sur la carte � partir de donn�es vectorielles.

**MenuUnitiesControl**

Contr�leur un peu particulier utilis� uniquement dans les applications fauna, invertebrate et mortality pour�:
* Afficher ou non la couche des unit�s g�ographiques
* Ajouter et d�placer un marqueur sur la carte et mettre en �vidence l'unit� g�ographique correspondant

Ce contr�leur n'a pas vraiment sa place ici car trop sp�cifique.

**SwitchLayersControl**

Contr�leur g�n�rique permettant de changer de couche de donn�es sur la carte.

**ZoomControl**

Contr�leur g�n�rique permettant d'ajouter une barre de bouton permettant de zoomer ou d�zoomer sur la carte.

**SearchControl**

Ce contr�leur n'est pas pr�sent dans le module maps mais dans l'application search. Il permet d'ajouter une fonction de recherche autour d'une position donn�e. Il n'y a pas grand-chose � faire pour le rendre g�n�rique et de l'int�grer au sein du module maps.