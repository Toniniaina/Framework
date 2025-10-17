@echo off
echo Verification du contenu du JAR framework...
echo.

REM Vérifier si le JAR existe
if not exist "testFramework\WEB-INF\lib\framework.jar" (
    echo ERREUR: Le fichier framework.jar n'existe pas dans testFramework\WEB-INF\lib\
    pause
    exit /b 1
)

echo Taille du JAR:
dir "testFramework\WEB-INF\lib\framework.jar"
echo.

echo Contenu du JAR:
jar tf "testFramework\WEB-INF\lib\framework.jar"
echo.

echo Verification terminee.
pause
