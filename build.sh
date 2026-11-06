#!/bin/bash
echo "Kompiliere Java-Projekt..."
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
if [ $? -eq 0 ]; then
    echo "Build erfolgreich! Die class-Dateien liegen in /bin"
    rm sources.txt
else
    echo "Build fehlgeschlagen!"
    rm sources.txt
    exit 1
fi