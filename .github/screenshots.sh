#!/usr/bin/env bash
# Installe l'APK sur l'émulateur et capture chaque écran.
APK=composeApp/build/outputs/apk/release/composeApp-release.apk
mkdir -p shots
adb install -r "$APK"
for s in bienvenue boissons fiche ingredients reglages; do
  adb shell am force-stop tn.costo
  adb shell am start -n tn.costo/.MainActivity --es costo.screen "$s"
  sleep 10
  adb exec-out screencap -p > "shots/$s.png"
done
adb logcat -d -t 500 '*:E' > shots/erreurs.txt || true
