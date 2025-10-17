@echo off
echo Nettoyage et redéploiement complet...
echo.

REM Étape 1: Recompiler complètement
echo 1. Nettoyage des anciens fichiers...
if exist "build\classes" rmdir /s /q "build\classes"
if exist "build\framework.jar" del "build\framework.jar"
if exist "testFramework\WEB-INF\lib\framework.jar" del "testFramework\WEB-INF\lib\framework.jar"

REM Étape 2: Créer les répertoires
echo 2. Création des répertoires...
if not exist "build\classes" mkdir "build\classes"
if not exist "testFramework\WEB-INF\lib" mkdir "testFramework\WEB-INF\lib"

REM Étape 3: Compilation
echo 3. Compilation des sources...
javac -classpath "jakarta.servlet-api_5.0.0.jar" -d "build\classes" framework\servlet\FrontServlet.java framework\servlet\ResourceFilter.java

if errorlevel 1 (
    echo ERREUR: Échec de la compilation!
    pause
    exit /b 1
)

REM Étape 4: Création du JAR
echo 4. Création du JAR...
cd build
jar cvf framework.jar -C classes .
cd ..

REM Étape 5: Copie du JAR
echo 5. Copie du JAR dans le projet web...
copy "build\framework.jar" "testFramework\WEB-INF\lib\"

REM Étape 6: Vérification
echo 6. Vérification du contenu du JAR...
jar tf "testFramework\WEB-INF\lib\framework.jar" | findstr "ResourceFilter"

if errorlevel 1 (
    echo ERREUR: ResourceFilter.class non trouvé dans le JAR!
    pause
    exit /b 1
)

echo.
echo ✅ Redéploiement terminé avec succès!
echo.
echo INSTRUCTIONS POUR TOMCAT:
echo 1. Arrêtez Tomcat complètement
echo 2. Supprimez le dossier testFramework de webapps (si il existe)
echo 3. Supprimez le cache Tomcat: work\Catalina\localhost\testFramework
echo 4. Copiez le dossier testFramework dans webapps
echo 5. Redémarrez Tomcat
echo.
pause
