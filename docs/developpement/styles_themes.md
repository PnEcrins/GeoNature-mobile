# Personnalisation des applications

L'ensemble des applications utilisent la notion de *flavor* au niveau du build gradle qui permet de
créer facilement des variantes de ces applications.
Actuellement, la variante utilisée par défaut est `pne` qui permet d'appliquer notamment les
couleurs de base du thème foncé (thème par défaut) et les icônes des applications mobiles aux
couleurs du Parc National des Écrins.
Lors du build des applications, il est donc nécessaire de préciser la variante à utiliser par
gradle :

En mode `debug` :

```
./gradlew clean assemblePneDebug
```

En mode `release` :

```
./gradlew clean assemblePneRelease
```

## Déclaration d'une nouvelle variante

Il faut modifier chaque fichier `build.gradle` situé dans chaque répertoire des applications et
ajouter une nouvelle variante dans `productFlavors`. Par exemple, si on souhaite créer la variante
nommée `pnm` on obtiendra :

```
android {
    ...
    productFlavors {
        pne {
        }
        pnm {
        }
    }
    ...
}
```

Lors du build des applications, il faudra donc préciser la variante à utiliser par gradle :

En mode `debug` :

```
./gradlew clean assemblePnmDebug
```

En mode `release` :

```
./gradlew clean assemblePnmRelease
```

## Ajout d'un thème couleur

Une fois la variante créée, il faut maintenant ajouter un nouveau répertoire portant le même nom que
cette variante dans le répertoire `src/` de chaque application. Par défaut, on trouve :

* `main` : le répertoire des ressources et des sources de chaque application
* `pne` : le répertoire de la variante `pne`

Avec l'exemple ci-dessus, on aura en plus le répertoire `pnm`.


Copier le répertoire `res/values` ainsi que son contenu dans le répertoire de la nouvelle variante
depuis le répertoire de la variante `pne`.
On trouvera le fichier `colors.xml` dans lequel sont déclarés les couleurs principales du thème :

* `baseColor` : la couleur de base sur laquelle seront dérivées les autres couleurs
* `colorPrimary` : la couleur principale du thème
* `colorPrimaryDark` : la variante foncée de la couleur principale du thème
* `colorAccent` : couleur d'accentuation

Pour faciliter la construction d'une palette de couleur pour le thème foncé des applications, on
peut utiliser le site [http://mcg.mbitson.com](http://mcg.mbitson.com) et de prendre la valeur de la
couleur définie par `baseColor` comme couleur principale. La palette sera générée automatiquement :

* `baseColor` : couleur générée à 500
* `colorPrimary` : couleur générée à 800
* `colorPrimaryDark` : couleur générée à 900
* `colorAccent` : couleur générée à 300

## Icône de l'application

Chaque répertoire des applications contient un répertoire `art/` dans lequel on trouvera une image
SVG commencant par `ìc_launcher_`. On peut s'en inspirer pour créer une nouvelle icône qui servira
de base à l'icône de chaque application. Idéalement, il faut rester sur les principes suivants :

* Format SVG
* Aucune marge
* Icône en noir seulement avec aucun fond (transparent)

Ensuite, aller sur le site [https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html](https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html) pour générer les ressources à importer dans la variante de chaque application. Indiquer les paramètres suivants :

* **Foreground** : Image (choisir l'image SVG)
* **Trim whitespace**: Trim
* **Padding**: 20%
* **Color**: white
* **Background color**: use `baseColor` from styles
* **Scaling**: Center
* **Shape**: Square
* **Effect**: Cast shadow
* **Name**: `ic_launcher`

Ensuite, cliquer sur le bouton *Download ZIP* (bouton bleu flottant, situé en haut à droite).
Dézipper l'archive et copier le contenu dans le répertoire de la variante de façon à trouver
l'arborescence suivante :

* `res`
    * `mipmap-hdpi`
    * `mipmap-mdpi`
    * `mipmap-xhdpi`
    * `mipmap-xxhdpi`
    * `mipmap-xxxhdpi`

## Nom de l'application

Pour changer le nom de l'application, il faut copier les fichiers `res/values/strings.xml` et
`res/values-fr/strings.xml` depuis le répertoire `src/main` de chaque application dans le répertoire
de la variante, en respectant l'arborescence. On trouvera alors l'arborescence suivante dans le
répertoire de la variante :

* `res`
    * `values`
    * `values-fr`

Ensuite, on peut éditer chaque fichier `strings.xml` et ne garder que le noeud contenant la clé
`app_name` :

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <string name="app_name">Mon application</string>

</resources>
```

Le système de build gradle va tout simplement fusionner les ressources situées dans le répertoire
par défaut (`src/main/res`) avec les ressources de la variante sélectionnée lors du build.
Donc il n'est pas nécessaire de garder tout copier dans la variante mais juste prendre les
ressources que l'on souhaite remplacer.
