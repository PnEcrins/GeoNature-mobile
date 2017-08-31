# GEONATURE-MOBILE

ORGANISATION FONCTIONNELLE ET FLUX DE DONNEES
=============================================

Geonature mobile permet uniquement de saisir de nouvelles observations faune ou flore. Pour éditer des données existantes ou déjà 
saisies et synchronisées, GeoNature-web doit être utilisé. GeoNature-Mobile comporte également une application de consultation des 
observations de la flore. Cette application ne permet pas de saisir de données mais lors des opérations de synchronisation, les données 
consultables sont mises à jour.

Afin de construire une chaîne de travail complète et cohérente (saisie, transfert, stockage et utilisation), Geonature-Mobile utilise 
plusieurs briques applicatives dont l'articulation et le fonctionnement sont présentés dans ce document.

Les données saisies grâce aux quatre applications de saisie GeoNature-mobiles peuvent être transférées vers la base de données selon 
deux méthodes non concurrentes utilisant toutes les deux une webapi installées sur un serveur. Cette webapi reçoit les données saisies 
au format json et les insert dans la base de données GeoNature. Elle permet également de servir les données à jour (listes des 
observateurs et des taxons notamment) et les fichiers de configuration nécessaires au fonctionnement des applications. Elle héberge 
également les fichiers apk afin de permettre une mise à jour éventuelle des cinq applications GeoNature-Mobile.

- La synchronisation réseau. C'est la plus simple à mettre en oeuvre mais elle offre beaucoup moins de souplesse dans la gestion 
d'une flotte importante de terminaux. En effet, les mise à jour applicatives doit être faites manuellement, terminal par terminal. 
De plus, les 4 applications doit être synchronisées une à une.
  - requiert une connexion wifi ou data (3g ou 4g).
  - le mode "accepter les sources inconnues" doit être activé sur le ou les terminaux pour installer les apk manuellement.

- La synchronisation Desktop. Elle passe par une application de synchronisation installées sur un poste de travail windows (ou Mac osx). Cette application permet de synchroniser les données des cinq applications ainsi que d'assurer la mise à jour applicative en une seule opération. 
  - Requiert une connexion usb entre le poste de travail et le terminal, 
  - l'installation des drivers usb du ou des terminaux sur le ou les postes de travail,
  - l'installation du JRE (java 32 ou 64 bits) sur le ou les postes de travail,
  - le mode "debogage USB" doit être activé sur le ou les terminaux.


Une fois la webapi installée et configurée, il est possible de synchroniser un terminal directement via une connexion réseau 
(wifi ou data). Pour cela, le fichier de settings des applications mobiles à synchroniser doit connaître l'url et le token de 
la webapi. Si ce mode de synchronisation est retenu, les applications doivent être synchronisée une à une. Si une nouvelle 
version de l'application est disponible, elle doit être installée manuellement sur chacun des terminaux de la flotte.

Pour simplifier le travail des utilisateurs mais aussi et surtout celui de l'administrateur, il recommandé d'utiliser la 
synchronisation desktop. Pour cela, il faut installer une petite application qui va connecter le terminal puis le serveur et 
servir de pont entre les deux. Une fois que le pont est établi, cette application opère automatiquement à la synchronisation de 
TOUTES les applications GeoNature-Mobile. cette synchronisation de fait en trois grandes étapes.
1. Mise à jour applicative. En comparant la version des applications installées sur le terminal avec celles disponibles sur le 
serveur (dans la webapi), elle va télécharger puis mettre à jour les applications GeoNature-Mobile du terminal connecté (si besoin 
uniquement).
2. Transfert des fichiers de saisie écrit par les applications mobiles, du terminal vers la base de données, via la webapi. Si cette 
écriture des données d'observation en base de données se déroule normalement, les fichiers de saisie sont copiés pour archivage sur 
le poste de travail puis effacés du terminal. Si une erreur survient, les fichiers de saisie sont conservés sur le terminal. Ils sont 
également copiés sur le poste de travail, et leur contenu est écrit dans un schéma dédié sur la base de données GeoNature. 
3. Copie des données vers le terminal : les données à jour, nécessaires au fonctionnement des applications : listes des observateurs, 
des taxons, des critères d'observation, le statuts des taxons dans les unités géographiques ainsi que les données d'observations flore 
sont importées de la base vers le terminal (fichier data.db). C'est la webapi qui se charge de produire ces données dans un fichier 
sqlite nommé data.db. L'application de synchro desktop se charge de son écriture sur le terminal (en remplacement de l'ancien).

Un fichier de log se trouvant sur le poste de travail donne des éléments concernant le déroulement de l'opération de synchronisation : 
date et heur, nombre de saisies synchronsisées, nom des fichiers json synchronisés erreurs éventuelles.

Si une erreur survient lors d'une de ces trois étapes, elle est représentée par un état d'échec (rouge). En cas de succès, l'état de 
l'étape est affiché en vert.

L'organisation et les flux de données entre ces différentes briques fonctionnelles sont représentés dans les schémas ci-dessous.

![GeoNature-mobile workflow synchro desktop](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/develop/docs/images/workflow-synchronisation-desktop.jpg)


![GeoNature-mobile workflow synchro wifi](https://raw.githubusercontent.com/PnEcrins/GeoNature-mobile/develop/docs/images/workflow-synchronisation-wifi.jpg)
