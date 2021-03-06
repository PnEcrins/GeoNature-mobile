========================================
Récupération et tuilage des fonds raster
========================================

EDIT 2017 : Cette documentation était initialement prévue pour pouvoir générer les mbtiles en utilisant des fonds rasters en Lambert 93 (2154). Cela n'est plus obligatoire depuis la version 1.1.0 et on peut désormais générer des fonds avec une procédure bien simple et classique en projection 4326. Voir la nouvelle documentation : https://github.com/PnEcrins/GeoNature-mobile/blob/master/docs/tuilage_raster_mbtiles-2017-01.pdf

Installation
============
::

    sudo apt-get install python-virtualenv python-dev
    virtualenv env
    source env/bin/activate
    
    pip install TileCache
    pip install mbutil
    pip install shapely


Extraire le contenu de l'archive ``tilecache_filter`` :

::

    unzip tilecache_filter.zip
    mv tilecache_filter.py env/bin/
    mv mbtilesplit.py env/bin/
    mv TileCache/Cleaner.py env/lib/python2.?/site-packages/TileCache/

À chaque ouverture de session shell, il faudra refaire 

::

    source env/bin/activate


Configuration de Tilecache
==========================

::

    [scan]
    type=WMSLayer
    layers=scan100,scan25
    url=http://extranet.parcnational.fr/pnx/wms?
    extension=jpg
    tms_type=google
    srs=EPSG:2154
    bbox=700000,6325197,1060000,6617738
    maxResolution=1142.7383

    [cache]
    type=GoogleDisk
    base=/home/user/tiles/

Par défault, TileCache va utiliser des échelles en puissance de 2 à partir de la valeur de ``maxResolution`` (résolution au niveau de zoom minimum, lorsqu'une seule tuile est affichée).

::

    maxResolution = MIN(LARGEUR, HAUTEUR) / TAILLETUILE

    maxResolution = MIN(1060000-700000, 6617738-6325197) / 256 = 1142,73828125

Il est possible de configurer les résolutions de TileCache manuellement 

::

    bbox=700000,6325197,1060000,6617738
    resolutions=496.34,248.17,124.08...

Voir documentation pour plus de détails (http://tilecache.org/docs/README.html#configuration)


Tuilage de la source distante
=============================

Lancer la commande (dans un `screen` si besoin) :

Par exemple, pour la couche *scan* du zoom 1 au zoom 12 :

::

    tilecache_seed.py scan 1 12

Par défault tilecache_seed fonctionne sur l'emprise définie au niveau de la couche, il est possible de le lancer sur une sous-partie. Voir sa documentation.

Pour supprimer les tuiles qui feraient parties de l'emprise rectangulaire mais en dehors du polygone du parc, il faut utiliser le script *tilecache_filter.py* de cette manière :

::

    python tilecache_filter.py <layer> <minzoom> <maxzoom> --delete --zone="POYGON((...WKT..))"

Voir plus bas pour le WKT du parc.


Passage au format MBTiles
=========================

Grace à la commande mb-util, on peut passer d'une arborescence de tuiles à un fichier MBTiles et vice-versa.

En premier lieu, créer un fichier de metadonnées dans le répertoires générés par TileCache. Par exemple dans ``/home/user/tiles/scan/metadata.json`` 

::

    {
        "name": "Scan",
        "description": "Scan",
        "version": "3",
        "format": "jpeg"
    }

Puis lancer la création du fichier MBTtiles.

::

    mb-util scan/ scan.mbtiles


Creation d'un MBTiles découpés selon les X
==========================================

À cause de la limitation des partitions FAT à 4Go, il est nécessaire de découper les fichiers.

Le script ``mbtilesplit.py`` fait ce travail. Il requiert d'avoir créé auparavant le fichier ``metadata.json``
puisqu'il va être dupliqué dans chacun des fichiers mbtiles résultants.

Il ne prend qu'une arborecensce de répertoire en paramètre, et crée les 10 fichiers Mbtiles.

::

    mbtilesplit.py ortho/


Données d'entrée
================

Emprise du parc WKT :

::

    POLYGON((934055 6450741, 933897 6451290, 936280 6451617, 937426 6451509, 938210 6450569, 937813 6447731, 938414 6447176, 939434 6446087, 941877 6442489, 942437 6444029, 944293 6444526, 946539 6443915, 946332 6446389, 947216 6447556, 946062 6449282, 946042 6452565, 946442 6452964, 946158 6454052, 946322 6454891, 946886 6455846, 948072 6455401, 948851 6455909, 949140 6456538, 949770 6457018, 950630 6456897, 952759 6455760, 953337 6454556, 955285 6455857, 957121 6454446, 957070 6452586, 958910 6451300, 961457 6452171, 962065 6453111, 964826 6450745, 965755 6448365, 965411 6447126, 967062 6446402, 968284 6447025, 970077 6446800, 971588 6445727, 974340 6446055, 975546 6445416, 975697 6444614, 975628 6443622, 975414 6442231, 979214 6440772, 980211 6439133, 982259 6439087, 987646 6433491, 988068 6430412, 988937 6427804, 987378 6424988, 983692 6423553, 983319 6420046, 982539 6417817, 984318 6416351, 982036 6414144, 984458 6411724, 984706 6409031, 986331 6407610, 987294 6404884, 989373 6403652, 988727 6401974, 985305 6401413, 986057 6394365, 983487 6394764, 982385 6396127, 981895 6393734, 979866 6389857, 977777 6388153, 976860 6387792, 978013 6384442, 979185 6382457, 979196 6380842, 979875 6379753, 979641 6378438, 978306 6378150, 977010 6378509, 975683 6378351, 974868 6379816, 973855 6379786, 973167 6380496, 972348 6379828, 971234 6380000, 970657 6381072, 970695 6381521, 969789 6382638, 969715 6383039, 968959 6383330, 968230 6384253, 967152 6384744, 966829 6385199, 965362 6383948, 964682 6384617, 964359 6385752, 963422 6387723, 962689 6388629, 952983 6392476, 950798 6393637, 947983 6395208, 946645 6397957, 944132 6397985, 944086 6400649, 941571 6402704, 939811 6405352, 936516 6408358, 934371 6412783, 935440 6413480, 934866 6414664, 933348 6415433, 932931 6416024, 932519 6417750, 933209 6419235, 932666 6420639, 933298 6421174, 933025 6421584, 933227 6422279, 932287 6422850, 931902 6423410, 930193 6423814, 927997 6423960, 926718 6424144, 926232 6424742, 926494 6425368, 926107 6425932, 926858 6426807, 926845 6427110, 927022 6427301, 926699 6427629, 926296 6427774, 926093 6427946, 925602 6427955, 925027 6428436, 924223 6431391, 925352 6432539, 924225 6433400, 924200 6434254, 924740 6435097, 924761 6435754, 924527 6436144, 925067 6437260, 924489 6438007, 924281 6438519, 924535 6441028, 926715 6440862, 927748 6441003, 929133 6442235, 929765 6442309, 929564 6445341, 930792 6447530, 932129 6447714, 932776 6448079, 933466 6448613, 933881 6449378, 934055 6450741))
