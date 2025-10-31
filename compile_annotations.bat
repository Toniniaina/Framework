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
