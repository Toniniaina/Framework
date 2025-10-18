@echo off
echo Compilation des annotations et du test...

REM Créer les répertoires de build s'ils n'existent pas
if not exist "build\classes" mkdir "build\classes"

REM Compilation des annotations
echo Compilation des annotations...
javac -d "build\classes" framework\annotation\*.java

if errorlevel 1 (
    echo Erreur de compilation des annotations!
    pause
    exit /b 1
)

REM Compilation des servlets
echo Compilation des servlets...
javac -classpath "jakarta.servlet-api_5.0.0.jar;build\classes" -d "build\classes" framework\servlet\*.java

if errorlevel 1 (
    echo Erreur de compilation des servlets!
    pause
    exit /b 1
)

REM Compilation du test
echo Compilation du test...
javac -classpath "build\classes" -d "build\classes" testFramework\*.java

if errorlevel 1 (
    echo Erreur de compilation du test!
    pause
    exit /b 1
)

echo Compilation réussie!
echo.
echo Pour tester les annotations:
echo java -cp "build\classes" testFramework.TestFramework
echo.
pause
