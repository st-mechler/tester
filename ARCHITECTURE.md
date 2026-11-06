# Architektur

## Ziel
Dieses Projekt ist ein reines Java-Projekt ohne Build-Tools wie Maven oder Gradle.
Der Build erfolgt ueber Harness-Skripte im Projekt-Root.

## Verzeichnisstruktur

```text
src/
  com/
    project/
      Main.java
bin/                (Build-Ausgabe, nicht versionieren)
build.bat
run.bat
build.sh
run.sh
```

## Paketregeln
- Jede Java-Datei liegt unter `src/`.
- Das `package`-Statement muss immer der Ordnerstruktur entsprechen.
- Aktuelles Einstiegspaket: `com.project`.

## Startpunkt
- Hauptklasse: `com.project.Main`
- Build: `build.bat` (Windows) oder `./build.sh` (Linux/macOS)
- Run: `run.bat` (Windows) oder `./run.sh` (Linux/macOS)
