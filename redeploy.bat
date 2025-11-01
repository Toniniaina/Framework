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
echo 3. Compilation des sources du framework...

REM Compiler les annotations de base
javac -d "build\classes" framework\annotation\Controller.java framework\annotation\GetMapping.java

REM Compiler les classes utilitaires (nouveau package framework\utilitaire)
REM IMPORTANT: compiler MappingInfo AVANT UrlMappingRegistry
javac -classpath "build\classes" -d "build\classes" framework\utilitaire\MappingInfo.java
javac -classpath "build\classes" -d "build\classes" framework\utilitaire\ConfigLoader.java
javac -classpath "build\classes" -d "build\classes" framework\utilitaire\ClassScanner.java
javac -classpath "build\classes" -d "build\classes" framework\utilitaire\UrlMappingRegistry.java

REM Compiler le service principal qui dépend des utilitaires
javac -classpath "build\classes" -d "build\classes" framework\annotation\AnnotationReader.java

if errorlevel 1 (
    echo ERREUR: Échec de la compilation des annotations!
    pause
    exit /b 1
)

REM Compiler les servlets
echo Compilation des servlets...
REM FrontServlet reste dans framework\servlet
javac -classpath "jakarta.servlet-api_5.0.0.jar;build\classes" -d "build\classes" framework\servlet\FrontServlet.java
REM ResourceFilter et UrlTestServlet ont été déplacés dans framework\utilitaire
javac -classpath "jakarta.servlet-api_5.0.0.jar;build\classes" -d "build\classes" framework\utilitaire\ResourceFilter.java framework\utilitaire\UrlTestServlet.java

if errorlevel 1 (
    echo ERREUR: Échec de la compilation des servlets!
    pause
    exit /b 1
)

REM Étape 4: Compilation des classes de test
echo 4. Compilation des classes de test...
if not exist "testFramework\WEB-INF\classes" mkdir "testFramework\WEB-INF\classes"

REM Copier config.properties
copy "testFramework\resources\config.properties" "testFramework\WEB-INF\classes\"

REM Compiler les controllers de test
javac -classpath "build\classes" -d "testFramework\WEB-INF\classes" testFramework\com\testframework\controller\*.java
javac -classpath "build\classes;testFramework\WEB-INF\classes" -d "testFramework\WEB-INF\classes" testFramework\com\testframework\admin\*.java
javac -classpath "build\classes;testFramework\WEB-INF\classes" -d "testFramework\WEB-INF\classes" testFramework\com\testframework\util\*.java
javac -classpath "build\classes;testFramework\WEB-INF\classes" -d "testFramework\WEB-INF\classes" testFramework\com\testframework\Main.java

if errorlevel 1 (
    echo ERREUR: Échec de la compilation des classes de test!
    pause
    exit /b 1
)

REM Étape 5: Création du JAR
echo 5. Création du JAR...
cd build
jar cvf framework.jar -C classes .
cd ..

REM Étape 6: Copie du JAR
echo 6. Copie du JAR dans le projet web...
copy "build\framework.jar" "testFramework\WEB-INF\lib\"

REM Étape 7: Vérification
echo 7. Vérification du contenu du JAR...
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

REM Étape 8: Déploiement automatique vers Tomcat (copie dans webapps)
set "TOMCAT_WEBAPPS=D:\Pxampp\tomcat\webapps"
echo.
echo 8. Déploiement vers %TOMCAT_WEBAPPS% ...

if not exist "%TOMCAT_WEBAPPS%" (
    echo [AVERTISSEMENT] Le dossier %TOMCAT_WEBAPPS% n'existe pas. Vérifiez le chemin de Tomcat.
    goto :eof
)

REM Supprimer l'ancienne application si elle existe
if exist "%TOMCAT_WEBAPPS%\testFramework" (
    echo - Suppression de l'ancienne application testFramework ...
    rmdir /s /q "%TOMCAT_WEBAPPS%\testFramework"
)

REM Copier la nouvelle version
echo - Copie de l'application testFramework ...
xcopy "testFramework" "%TOMCAT_WEBAPPS%\testFramework" /E /I /Y >nul
if errorlevel 1 (
    echo [ERREUR] Échec de la copie vers %TOMCAT_WEBAPPS%\testFramework
    goto :eof
)

echo ✅ Déploiement copié dans %TOMCAT_WEBAPPS%\testFramework
echo (Redémarrez Tomcat pour prendre en compte les changements.)
