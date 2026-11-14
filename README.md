# Costo

Application de calcul du coût de revient des boissons : prix d'achat des ingrédients,
recettes par portion, pertes, coût fixe par tasse, marge visée et prix de vente conseillé.

Écrite en **Kotlin Multiplatform** avec **Compose Multiplatform** : le même code donne
l'app Android et une version PC.

## Fonctions (version 1.0.0)

- Écrans de bienvenue (3 pages)
- Boissons : coût par tasse, prix au menu, marge réelle (bleue si l'objectif est atteint, orange sinon)
- Fiche boisson : ingrédients et doses, pertes, coût fixe, marge souhaitée, prix conseillé et arrondi
- Ingrédients : prix d'achat par paquet, historique des prix avec tendance
- Réglages : coût fixe, pertes, marge par défaut, récupération des prix de vente du menu, sauvegarde et restauration
- Plein écran ; les mises à jour s'installent par-dessus sans perdre les données

Les données de départ (prix d'achat, produits du menu, doses standard) sont dans
`composeApp/src/commonMain/kotlin/tn/costo/model/Seed.kt`.

## Organisation

```
composeApp/src/commonMain   code commun (modèles, calculs, écrans)
composeApp/src/androidMain  activité Android, icône, plein écran, sauvegarde
composeApp/src/desktopMain  version PC
signing/                    clé de signature (garder le dépôt privé)
```

## Compiler

- **Android** : automatique à chaque `push` sur `main` (GitHub Actions). L'APK signé et les
  captures d'écran sont publiés sur la branche `builds`.
  En local : `./gradlew :composeApp:assembleRelease`
- **PC (Windows)** : `gradlew.bat :composeApp:run` pour lancer, ou
  `gradlew.bat :composeApp:packageMsi` pour un installateur.

## Mises à jour

Avant chaque nouvelle version, augmenter `costoVersionCode` (et `costoVersionName`) dans
`composeApp/build.gradle.kts`. L'APK est toujours signé avec `signing/costo-release.p12` :
c'est ce qui permet de l'installer par-dessus l'ancienne version. Ne pas perdre cette clé.

Police Nunito : licence SIL Open Font License (`FONT-LICENSE-Nunito.txt`).
