@echo off
echo Kompiliere Java-Projekt...
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
if %errorlevel% equ 0 (
    echo Build erfolgreich!
    del sources.txt
) else (
    echo Build fehlgeschlagen!
    del sources.txt
    exit /b 1
)