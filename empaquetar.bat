@echo off
rem ============================================================================
rem  Empaqueta HCE-Demo para Windows (carpeta portable con Java incluido).
rem  Uso: doble clic o ejecutar empaquetar.bat desde la carpeta del proyecto.
rem  Requiere JDK 21 a 25 en esta PC (jpackage). La PC de la demo NO necesita Java.
rem  Resultado: dist\HCE-Demo\HCE-Demo.exe  y  dist\HCE-Demo.zip
rem ============================================================================
setlocal
cd /d "%~dp0"

set NOMBRE=HCE-Demo
set VERSION=0.1.0
set JAR=hce-saas-%VERSION%.jar

rem Busca jpackage: primero en el PATH, luego en JAVA_HOME y luego en Program Files\Java\jdk-*
set JPACKAGE=
where jpackage >nul 2>nul && set "JPACKAGE=jpackage"
if not defined JPACKAGE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"
if not defined JPACKAGE for /d %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%D\bin\jpackage.exe" set "JPACKAGE=%%D\bin\jpackage.exe"
if not defined JPACKAGE (
  echo [ERROR] No se encontro jpackage. Instala un JDK 21 a 25 ^(no basta el JRE^) y revisa JAVA_HOME o el PATH.
  goto error
)
echo Usando: %JPACKAGE%

echo [1/5] Compilando y ejecutando los tests...
call "%~dp0mvnw.cmd" clean package
if errorlevel 1 goto error
if not exist "target\%JAR%" (
  echo [ERROR] No se genero target\%JAR%. Revisa la version en pom.xml.
  goto error
)

echo [2/5] Preparando la entrada de jpackage...
rmdir /s /q "target\jpackage-input" 2>nul
mkdir "target\jpackage-input"
copy /y "target\%JAR%" "target\jpackage-input\%JAR%" >nul
if errorlevel 1 goto error

echo [3/5] Construyendo la carpeta portable con runtime incluido...
rmdir /s /q "dist\%NOMBRE%" 2>nul
del /q "dist\%NOMBRE%.zip" 2>nul
rem Modulos de Java que necesita la app (Spring, JPA/Hibernate, H2, validacion, logging).
set MODULOS=java.base,java.logging,java.sql,java.naming,java.management,java.xml,java.instrument,java.net.http,java.security.jgss,java.security.sasl,java.compiler,java.desktop,jdk.unsupported,jdk.crypto.ec,jdk.management
"%JPACKAGE%" --type app-image --name %NOMBRE% --app-version %VERSION% ^
  --input "target\jpackage-input" --main-jar %JAR% ^
  --dest dist --win-console ^
  --add-modules %MODULOS% ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --arguments "--hce.abrir-navegador=true"
if errorlevel 1 goto error

echo [4/5] Comprobando que quedo el ejecutable...
if not exist "dist\%NOMBRE%\%NOMBRE%.exe" (
  echo [ERROR] No se genero dist\%NOMBRE%\%NOMBRE%.exe
  goto error
)

echo [5/5] Comprimiendo en dist\%NOMBRE%.zip ...
powershell -NoProfile -Command "Compress-Archive -Path 'dist\%NOMBRE%' -DestinationPath 'dist\%NOMBRE%.zip' -Force"
if errorlevel 1 goto error

echo.
echo LISTO. Carpeta: dist\%NOMBRE%   Zip: dist\%NOMBRE%.zip
echo Abre dist\%NOMBRE%\%NOMBRE%.exe con doble clic para probar.
exit /b 0

:error
echo.
echo El empaquetado fallo. Revisa el mensaje de arriba.
exit /b 1
