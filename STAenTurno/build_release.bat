@echo off
setlocal

rem Configurar JAVA_HOME explicitamente para evitar conflicto con Java 25
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem Check/Prompt for environment variables
if "%KEYSTORE_PATH%"=="" set KEYSTORE_PATH=release.keystore

if not "%KEYSTORE_PASSWORD%"=="" goto check_alias

echo [INFO] Variables de entorno no detectadas.
echo Por favor ingresa los datos ahora (o configura KEYSTORE_PASSWORD antes de ejecutar).
echo.
set /p KEYSTORE_PASSWORD="Introduce la contrasena del Keystore: "

:check_alias
if "%KEY_ALIAS%"=="" set /p KEY_ALIAS="Introduce el Alias de la llave relative (ej: mi-app-release_V1): "
if "%KEY_PASSWORD%"=="" set /p KEY_PASSWORD="Introduce la contrasena de la llave: "

if "%KEYSTORE_PASSWORD%"=="" (
    echo [ERROR] Contrasena requerida.
    exit /b 1
)

echo [1/3] Limpiando y generando APK Release...
call gradlew.bat clean assembleRelease

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Fallo al generar el APK via Gradle.
    exit /b %ERRORLEVEL%
)

rem Usando apksigner de build-tools 36.0.0
call "C:\Users\Soporte\AppData\Local\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --verbose app\build\outputs\apk\release\app-release.apk
if errorlevel 1 goto verify_error

echo.
echo [3/3] Calculando SHA-256 para distribucion...
certutil -hashfile app\build\outputs\apk\release\app-release.apk SHA256
goto end

:verify_error
echo [WARNING] Fallo la verificacion de firma.
goto end

:end
echo.
echo ============================================================
echo PROCESO FINALIZADO
echo APK generado en: app\build\outputs\apk\release\app-release.apk
echo ============================================================

endlocal
pause
