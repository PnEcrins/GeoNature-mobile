===============================
Création du fichier unities.wkt
===============================

Avec pg_admin
=============

Exécuter la requête ci-dessous en cliquant sur le bouton ``Exécuter la requête et sauvegarder le résultat`` 

Lorsque la fenêtre d'option s'ouvre définir les options de la manière suivante :

* **Séparateur de ligne** ``LF``
* **Sép.de colonne** ``,``
* **SNoms des colonnes** ``déchoché``
* **Nom de fichier** ``unities.wkt``
* **Codage** ``Unicode UTF-8``
* **Echappement** ``Sans guillemets``

	::
		SELECT id_unite_geo, 
			st_astext(
				st_snaptogrid(
					(
						(
							ST_DUMP(
								st_transform(the_geom,4326)
							)
						).geom::geometry(Polygon,4326)
					)
					,0.00001
				)
			) 
		FROM layers.l_unites_geo

Si vous souhaitez alléger le fichier en simplifiant les géométries, utiliser la requête ci-dessous en adaptant la valeur de st_simplify selon votre besoin.

	::
		SELECT id_unite_geo, 
			st_astext(
				st_snaptogrid(
					(
						(
							ST_DUMP(
								st_transform(
									st_simplify(the_geom,10)
								,4326)
							)
						).geom::geometry(Polygon,4326)
					)
					,0.00001
				)
			) 
		FROM layers.l_unites_geo

En ligne de commande
====================

	::
		sudo -n -u postgres -s psql -d geonaturedb -c "Copy (SELECT id_unite_geo,st_astext(st_snaptogrid(((ST_DUMP(st_transform(st_simplify(the_geom,10),4326))).geom::geometry(Polygon,4326)),0.00001)) FROM layers.l_unites_geo) TO '/tmp/unities.wkt' WITH CSV DELIMITER ',';"

Le fichier se trouve dans le répertoire /tmp.
Les geométries au format text sont ici encadrées par des doubles quotes. Ceci n'a pas été testé.