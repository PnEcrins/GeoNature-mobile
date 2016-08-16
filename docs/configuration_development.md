# Applications de saisies

## Pré-requis

* JDK 7 (au minimum)
* Android Studio

## Génération des APKs

### Signature des applications

Si aucun certificat n'a été créé, consulter la page suivante :
http://developer.android.com/tools/publishing/app-signing.html#cert

Ensuite à la racine du projet, ajouter le fichier `gradle.properties` et ajouter les lignes suivantes en complétant les valeurs selon le certificat à utiliser :

```
STORE_FILE=/absolute/path/to/your.keystore
STORE_PASSWORD=storepassword
KEY_ALIAS=keyalias
KEY_PASSWORD=keypassword
```

### Build des applications

Une fois fait, on peut lancer le build global pour générer toutes les applications :

```
./gradlew clean assembleRelease
```

Les APKs générés se trouvent dans le répertoire `build/outputs/apk/` de chaque module :

* **fauna** : `fauna-release-<version>.apk`
* **flora** : `flora-release-<version>.apk`
* **invertebrate** : `invertebrate-release-<version>.apk`
* **mortality** : `mortality-release-<version>.apk`
* **search** : `search-release-<version>.apk`

## Fichiers de configuration des applications

Chaque application possède son propre fichier de configuration au format JSON.
Ce fichier est lu une fois à chaque démarrage de l'application. Il doit suivre le nommage suivant :

```
settings_<nom_du_module>.json
```

Ce qui donne :

* **fauna** : `settings_fauna.json`
* **flora** : `settings_flora.json`
* **invertebrate** : `settings_invertebrate.json`
* **mortality** : `settings_mortality.json`
* **search** : `settings_search.json`

Chaque fichier de configuration doit suivre la structure suivante :

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

Paramètre							| Description
----------------------|------------
url										| URL du serveur de synchronisation
token									| Jeton d'identification à utiliser sur le serveur de synchronisation
status_url						| URL à utiliser pour vérifier le statut du serveur de synchronisation
import_url						| URL de l'import à utiliser pour synchroniser les données de saisies des applications mobiles
exports								| Liste des fichiers à récupérer coté serveur de synchronisation et à copier sur le terminal
exports / url					| URL du fichier à récupérer et à copier sur le terminal
exports / file				| Chemin (optionnel) et nom du fichier à utiliser pour copier le fichier récupéré coté serveur de synchronisation
map / layers					| Liste des fichiers des couches de données à afficher sur la carte
map / layers / name		| Nom de la source de données (fichier ou répertoire) de la couche de données
map / layers / label	| Nom de la couche de données
map / layers / source	| Type de sources de données : mbtiles (format MBTiles), mbtiles_split, dir (répertoire de tuiles), http (non implémenté)

Certaines applications ajoutent spécifiquement des attributs supplémentaires.

**fauna** et **invertebrate** :

Paramètre					| Description
------------------|------------
map / unity_layer	| Couche de données supplémentaire (vectorielle, format WKT) à afficher sur la carte

**search** :

Paramètre										| Description
----------------------------|------------
search / max_radius					| Rayon de recherche (en m) maximal autour d'une position donnée
search / default_radius			| Rayon de recherche (en m) par défaut autour d'une position donnée
search / max_features_found	| Le nombre maximal d'éléments à afficher lors d'une recherche autour d'une position donnée


# Architecture générale

Le projet actuel contient deux bibliothèques Android partagées par toutes les applications mobiles :

* `commons`
* `maps`

Les applications mobiles s'appuient donc sur ces modules pour intégrer les fonctionnalités communes. Ces deux bibliothèques restent indépendantes l'une de l'autre.

## Module commons

Ce module est une bibliothèque Android offrant les fonctionnalités et services communs à l'ensemble des applications mobiles :
* Gestion des fichiers de configuration des applications mobiles
* Gestion des données locales avec notamment la détection des différents points de montage présent sur les terminaux pour exploiter les cartes SD externes
* Gestion de la synchronisation des données locales
* Gestion du workflow de saisie

### Services Android déclarés

Le module `commons` offre un ensemble de Service Android pour les applications :
* `SettingsService` : Pour charger au démarrage de chaque application son fichier de configuration ainsi qu'éventuellement d'autres données (comme le fichier unities.wkt)
* `CheckServerService` : Pour vérifier si le serveur de synchronisation est disponible ou non
* `SyncService` : Service de synchronisation des données locales
* `SyncCommandIntentService` : Permet d'exécuter des commandes en dehors des applications. Ce service est utilisé par l'application de synchronisation desktop pour :
	* Récupérer les informations relatives à chaque application installée sur le terminal (ID de l'application, numéro de version, etc.)
	* Supprimer les saisies stockées localement sur le terminal une fois que la synchronisation s'est bien déroulée via l'application de synchronisation desktop
	* Déplacer les fichiers à synchroniser au bon emplacement sur le terminal si celui-ci possède une carte SD externe. Cette commande est importante car l'application de synchronisation desktop embarque un client `adb` pour communiquer avec le terminal en USB et de pouvoir exécuter des commandes. Mais il n'a pas les permissions suffisantes pour pouvoir accéder en écriture à la carte SD externe du terminal si celui-ci en possède une. Donc la synchronisation des fichiers se fait en deux temps dans ce cas là : Les fichiers à synchroniser son copiés localement sur le terminal dans l'espace de stockage interne puis l'application de synchronisation desktop lance la commande permettant de déplacer ces fichiers au bon endroit sur le terminal. Le service `SyncCommandIntentService` est invoqué via cette commande pour déplacer des fichiers.

		Cette partie pourrait être simplifiée en complétant l'application de synchronisation desktop avec un client MTP pour pouvoir accéder directement à l'ensemble de l'espace de stockage du terminal et donc de pouvoir accéder en lecture / écriture à la carte SD externe du terminal. Le client `adb` fera toujours le même travail mais déléguera les opérations de copie au client MTP.

Le module `commons` possède un nouveau package appelé `service` avec un nouveau Service Android `RequestHandlerService`. Ce service se veut plus simple à l'utilisation car il possède son propre client pour pouvoir l'utiliser. L'avantage est de n'avoir plus qu'un seul service de déclaré pour toutes les applications et l'idée à terme est de remplacer tous les services listés ci-dessus par des requêtes pouvant être joué via ce nouveau service à travers son client. Cette nouvelle approche où les appels se font à travers le client permet de supprimer tout le code qui s'occupe de la gestion des services Android.

Actuellement, il n'est pas vraiment terminé et n'est utilisé qu'à titre d'exemple dans l'application `search` en remplaçant le service `SettingsService` par son équivalent `LoadSettingsRequestHandler`.

## Module maps

Ce module est une bibliothèque Android gérant la partie cartographique des applications mobiles. Elle embarque notamment un composant Android `WebView` permettant de gérer l'intégration de la bibliothèque Javascript [Leaflet](http://leafletjs.com). Ce module est complètement indépendant du reste et ne dépend pas du module `commons`.

### Organisation du module

* `assets` : Ensemble des sources Javascript, notamment la bibliothèque cartographique Leaflet.
* `content` : Gestion des sources de données à afficher (MBTiles, répertoire, etc.)
* `control` : Contrôleurs (UI) de la carte (zoom, position centrée autour de la position courante, etc.)
* `geojson` : Objets GeoJSON

A la racine , on trouve le `fragment` générique `AbstractWebViewFragment` permettant d'afficher la carte via un composant Android `WebView`.

### Gestion des sources de données

Le module `maps` offre trois sources possibles d'accès aux sources de données :
* `dir` : Permet de lire les tuiles directement sur l'espace de stockage du terminal selon un répertoire donné (pyramide de tuiles)
* `mbtiles` : Permet de lire les tuiles selon un fichier MBTiles.
* `mbtiles_split` : Permet de lire les tuiles selon un ensemble de fichiers MBTiles éclatés selon le paramètre x (en colonne).
* `http` : Cette quatrième source est simplement déclarée mais non implémentée.

### Contrôleurs

Le module `maps` offre en standard plusieurs contrôleurs.

**MainControl**

Contrôleur par défaut et obligatoire. Il permet de charger les autres contrôleurs déclarés et offre :
* La gestion de la position courante de l'utilisateur
* Accéder aux attributs de la carte (Limites, centre de la carte, le zoom courant, le zoom minimal et maximal)
* Accéder à la couche de donnée courante
* Récupérer les tuiles selon les coordonnées

**CenterPositionControl**

Contrôleur générique permettant d'ajouter sur la carte un bouton permettant de centrer automatiquement la carte selon la position courante de l'utilisateur.

**DrawControl**

Contrôleur générique permettant d'ajouter sur la carte une barre de bouton permettant la saisie de géométries (points, lignes et polygones), et de permettre leurs éditions (déplacement, ajout, suppression et modification de la géométrie en ajoutant, déplaçant et en supprimant des points).

**FeaturesControl**

Contrôleur générique permettant d'ajouter une couche de données supplémentaire sur la carte à partir de données vectorielles.

**MenuUnitiesControl**

Contrôleur un peu particulier utilisé uniquement dans les applications `fauna`, `invertebrate` et `mortality` pour :
* Afficher ou non la couche des unités géographiques
* Ajouter et déplacer un marqueur sur la carte et mettre en évidence l'unité géographique correspondant

Ce contrôleur n'a pas vraiment sa place ici car trop spécifique.

**SwitchLayersControl**

Contrôleur générique permettant de changer de couche de données sur la carte.

**ZoomControl**

Contrôleur générique permettant d'ajouter une barre de bouton permettant de zoomer ou dézoomer sur la carte.

**SearchControl**

Ce contrôleur n'est pas présent dans le module `maps` mais dans l'application `search`. Il permet d'ajouter une fonction de recherche autour d'une position donnée. Il n'y a pas grand-chose à faire pour le rendre générique et de l'intégrer au sein du module `maps`.
