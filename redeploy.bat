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

REM Compiler les annotations
javac -d "build\classes" framework\annotation\Controller.java framework\annotation\GetMapping.java
javac -classpath "build\classes" -d "build\classes" framework\annotation\MappingInfo.java
javac -classpath "build\classes" -d "build\classes" framework\annotation\ConfigLoader.java
javac -classpath "build\classes" -d "build\classes" framework\annotation\ClassScanner.java
javac -classpath "build\classes" -d "build\classes" framework\annotation\UrlMappingRegistry.java
javac -classpath "build\classes" -d "build\classes" framework\annotation\AnnotationReader.java

if errorlevel 1 (
    echo ERREUR: Échec de la compilation des annotations!
    pause
    exit /b 1
)

REM Compiler les servlets
echo Compilation des servlets...
javac -classpath "jakarta.servlet-api_5.0.0.jar;build\classes" -d "build\classes" framework\servlet\*.java

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
