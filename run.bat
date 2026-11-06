@echo off
call build.bat
if errorlevel 1 exit /b 1
echo Starte Anwendung...
java -cp bin com.project.Main