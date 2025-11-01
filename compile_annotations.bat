@echo off
echo Compilation des annotations et du test...

REM Nettoyage des anciens .class pour éviter les conflits de packages déplacés
if exist "build\classes" rmdir /s /q "build\classes"
mkdir "build\classes"

REM Compilation des annotations et classes utilitaires
echo Compilation des annotations et classes du framework...
javac -d "build\classes" framework\annotation\Controller.java framework\annotation\GetMapping.java

REM Classes utilitaires (déplacées dans framework\utilitaire)
REM IMPORTANT: compiler MappingInfo AVANT UrlMappingRegistry
javac -classpath "build\classes" -d "build\classes" framework\utilitaire\MappingInfo.java
javac -classpath "build\classes" -d "build\classes" framework\utilitaire\ConfigLoader.java
javac -classpath "build\classes" -d "build\classes" framework\utilitaire\ClassScanner.java
javac -classpath "build\classes" -d "build\classes" framework\utilitaire\UrlMappingRegistry.java

REM Service principal qui dépend des utilitaires
javac -classpath "build\classes" -d "build\classes" framework\annotation\AnnotationReader.java

if errorlevel 1 (
    echo Erreur de compilation des annotations!
    pause
    exit /b 1
)

REM Compilation des servlets
echo Compilation des servlets...
REM Ne compiler que FrontServlet dans framework\servlet
javac -classpath "jakarta.servlet-api_5.0.0.jar;build\classes" -d "build\classes" framework\servlet\FrontServlet.java
REM Compiler ResourceFilter et UrlTestServlet dans framework\utilitaire
javac -classpath "jakarta.servlet-api_5.0.0.jar;build\classes" -d "build\classes" framework\utilitaire\ResourceFilter.java framework\utilitaire\UrlTestServlet.java

if errorlevel 1 (
    echo Erreur de compilation des servlets!
    pause
    exit /b 1
)

REM Copie du fichier de configuration
echo Copie du fichier config.properties...
copy "testFramework\resources\config.properties" "build\classes\"

REM Compilation des sous-packages de com.testframework
echo Compilation des sous-packages (controller, util, test, admin)...

if exist "testFramework\com\testframework\controller\*.java" (
    javac -classpath "build\classes" -d "build\classes" testFramework\com\testframework\controller\*.java
    if errorlevel 1 (
        echo Erreur de compilation du package controller!
        pause
        exit /b 1
    )
)

if exist "testFramework\com\testframework\util\*.java" (
    javac -classpath "build\classes" -d "build\classes" testFramework\com\testframework\util\*.java
)

if exist "testFramework\com\testframework\test\*.java" (
    javac -classpath "build\classes" -d "build\classes" testFramework\com\testframework\test\*.java
)

if exist "testFramework\com\testframework\admin\*.java" (
    javac -classpath "build\classes" -d "build\classes" testFramework\com\testframework\admin\*.java
)

REM Compilation de la classe Main
echo Compilation de la classe Main...
javac -classpath "build\classes" -d "build\classes" testFramework\com\testframework\Main.java

if errorlevel 1 (
    echo Erreur de compilation de la classe Main!
    pause
    exit /b 1
)

echo Compilation réussie!
echo.
echo Pour tester les annotations:
echo java -cp "build\classes" testFramework.com.testframework.Main
echo.
pause
