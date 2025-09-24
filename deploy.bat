@echo off
REM ------------------------------------------------------------------------
REM Script de déploiement Windows pour compiler le framework et préparer le projet de test
REM ------------------------------------------------------------------------

REM Définition des chemins (à adapter si besoin)
set "FRAMEWORK_DIR=A:\Projet-Framework\framework"
set "BUILD_DIR=%FRAMEWORK_DIR%\build"
set "TEST_DIR=D:\Pxampp\tomcat\webapps\testFramework"
set "SERVLET_JAR=%FRAMEWORK_DIR%\jakarta.servlet-api_5.0.0.jar"

REM Création des dossiers de sortie du framework
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist "%BUILD_DIR%\classes" mkdir "%BUILD_DIR%\classes"

REM Compilation récursive des sources Java du framework
echo Compilation du framework...
for /R "%FRAMEWORK_DIR%" %%f in (*.java) do (
    echo   Compilation de %%~nxf
    javac -classpath "%SERVLET_JAR%" -d "%BUILD_DIR%\classes" "%%f"
    if errorlevel 1 (
        echo Erreur de compilation du fichier %%~nxf
        exit /b 1
    )
)

REM Création du JAR du framework
echo Creation du JAR du framework...
cd /d "%BUILD_DIR%"
if exist "framework.jar" del "framework.jar"
jar cvf "framework.jar" -C "classes" .

REM 
echo Copie du framework.jar dans le projet Test...
if not exist "%TEST_DIR%\WEB-INF\lib" mkdir "%TEST_DIR%\WEB-INF\lib"
xcopy "%BUILD_DIR%\framework.jar" "%TEST_DIR%\WEB-INF\lib\" /Y >nul

REM 


REM 
if not "%~1"=="" (
    echo Demarrage de Tomcat...
    call "%~1\bin\startup.bat"
)
