Installation des applications Android
=====================================

Installation des apk
--------------------

Le répertoire ``apk`` contient les fichiers apk permettant l'installation des applications sur le terminal Android.

:notes:

	**Prérequis** : l'option ``sources inconnues`` doit être activée. Exemple sur Android 6 : ``Paramètres --> Ecran verrouillage/Sécurité``

* Copier le ou les fichiers des applications que vous souhaitez installer sur le terminal (par exemple dans le répertoire ``download`` de la mémoire interne)
* Lancer l'installation à partir de chacun des fichiers
* Une fois les applications installées vous devriez avoir un répertoire ``Android/data/com.geonature.mobile``
* Placer dans ce répertoire le contenu proposé dans ``internal memory/Android/data/com.geonature.mobile``

A ce stade les applications sont installées mais ne sont pas encore fonctionnelles. Il faut leur fournir fonds de carte et fichiers de configuration.


Mémoire externe du terminal 
---------------------------

Cette mémoire est destinée à recevoir tous les fichiers cartographiques, potentiellement lourds.

:notes:

    Si vous avez un terminal sans possibilité d'extention de la mémoire (carte microsd), vous n'avez pas de mémoire externe. Dans ce cas, vous pouvez placer les fichiers et répertoires prévus pour la mémoire externe, dans la mémoire interne. Les arborescences sont identiques. Assurez vous de disposer de suffisamment de mémoire.


* Placez les fichiers mbtiles des fonds cartographiques dans le répertoire ``Android/data/com.geonature.mobile/databases``.
* Si vous utilisez les applications ``fauna`` et ``invertebrate`` (= contact faune et contact invertébré), vous devez placer
	* le fichier ``unities.wkt`` dans le répertoire ``Android/data/com.geonature.mobile``. Ce fichier vecteur permet aux applications de déterminer dans quelle unité géographique se trouve l'observation lors du pointage sur la carte. Vous devez produire ce fichier ``unities.wkt`` manuellement (par exemple avec qgis ou PostreSQL/Postgis) à partir du contenu de la table ``layers.l_unites_geo`` de la base de données PostgreSQ de GeoNature. Un fichier exemple des unités géographiques du Parc national des Ecrins est proposé.
	* le fichier ``unities.mbtiles`` dans le répertoire ``Android/data/com.geonature.mobile/databases``. Ce fichier permet de visualiser le contour des unités géographiques lors de la navigation cartographique. Pour produire ce fichier, vous pouvez utiliser le service wms de GeoNature : https://github.com/PnX-SI/GeoNature/blob/master/wms/wms.map.sample#L72-L89 . L'url du service wms de GeoNature devrait ressembler à ceci : http://localhost/wmsgeonature


Memoire interne du terminal
---------------------------

La mémoire interne est destinée à recevoir les fichiers de configuration des applications ainsi que la base de données communes à toutes les applications ``data.db``. Cette base de données contient notamment la liste des taxons et des observateurs ainsi qu'un ensemble d'informations nécessaires au fonctionnement des applications. Elle est utilisée par les applications en lecture seule et est remplacée lors de chaque synchronisation (mise à jour à partir du contenu de la base de données PostgreSQ de GeoNature).

La mémoire interne recevra également les fichiers de saisie dans le répertoire ``Android/data/com.geonature.mobile/inputs``. Après une synchronisation réussie, les fichiers de saisie sont supprimés. Si la synchronisation échoue, les fichiers sont renommés ``ko_xxxxxx.json`` et devront être traités manuellement.

**Adapter le contenu des fichiers de configuration settings_xxxxx.json** de chacune des applications installées.

* partie ``sync`` = paramètres pour la synchronisation de l'application via la webapi.
	* ``url`` contient l'adresse de la webapi.
	* ``token`` doit correspondre au token attendu par la webapi. Voir la configuration du fichier setting_local.py de la webapi.
	* ``status_url`` URL à utiliser pour vérifier le statut du serveur de synchronisation (laisser à la valeur par défaut).
	* ``import_url`` URL de l'import à utiliser pour synchroniser les données de saisies des applications mobiles (laisser à la valeur par défaut).
	* ``exports`` contient un tableau des url complémentaires à l'url de base de la webapi. 
		* ``export/sqlite/`` permet de produire et de copier sur le terminal Android la base de données de l'application data.db. Les valeurs proposées ne doivent pas être modifiées.

* Partie ``qualification`` = configuration des métadonnées à attacher aux observations produites par les applications.
	* ``organism`` correspond à l'organisme producteur de la données. La valeur  de type integer doit être présente dans le champ ``id_organisme`` de la table ``utilisateurs.bib_organismes`` de la base de données PostgreSQL de GeoNature.
	* ``protocol`` est le protocole correspondant au données produites par l'application. Par exemple ``contact faune`` pour l'application ``fauna``. La valeur  de type integer doit être présente dans le champ ``id_protocole`` de la table ``meta.t_protocoles`` de la base de données PostgreSQL de GeoNature.
	* ``lot`` est le lot de données correspondant au données produites par l'application. Par exemple ``contact invertébrés`` pour l'application ``invertebrate``. La valeur de type integer doit être présente dans le champ ``id_lot`` de la table ``meta.bib_lots`` de la base de données PostgreSQL de GeoNature.

* Partie ``map`` = configuration du comportement cartographique de l'application.
	* ``max_bounds`` et ``center`` permettent de définir les limites de l'emprise cartographique à afficher ainsi que les coordonnées sur lesquelles la carte doit se centrer. Un exemple est fourni pour le territoire du Parc national des Ecrins. Vous devez adapter ces valeurs à votre territoire. Les valeurs doivent correspondre à l'emprise du tuilage réalisé dans les fichiers mbtiles que vous avez copiez sur la mémoire externe (voir ci-dessus la partie ``Mémoire externe du terminal``).
	* ``start_zoom`` défini le niveau de zoom initial ou minimal que l'application peut afficher
	* ``min_zoom_pointing`` il s'agit du niveau de zoom minimal à partir duquel il est possible de pointer une observation. Lors de la navigation cartographique, les boutons permettant de positionner l'observation sont désactivés tant que ce niveau de zoom n'est pas atteint. Ceci permet d'éviter de positionner des observations de manière trop imprécise. Par défaut la valeur 15 correspond au premier niveau de zoom du scan25.
	* la sous-partie ``layers`` permet de déclarer les fonds disponibles pour la navigation cartographique.
		* ``name`` correspond au nom du fichier placé dans ``Android/data/com.geonature.mobile/databases``.
		* ``label`` est le nom du fond qui sera utilisé par l'application pour nommer la couche en question.
		* ``source`` est un paramètre qui doit être laissé avec la valeur par défault ``mbtiles``. Historiquement il était nécessaire de spliter les tuilages trop lourds en plusieurs fichiers. Ce paramètre permettait de définir le type de source de tuilage.

	* la sous-partie ``unity_layer`` permet de déclarer le fichier mbtiles contenant la couche des unités geographiques sur le même principe que la sous-partie ``layers``.

* Partie ``search`` pour l'application ``recherche flore`` uniquement.
	* max_radius défini le rayon de recherche maximal en mètre autour de la position donnée
	* default_radius défini le rayon de recherche par défaut en mètre autour de la position donnée
	* max_features_found défini le nombre maximal d'éléments à afficher lors d'une recherche autour de la position donnée (gestion des performances)
