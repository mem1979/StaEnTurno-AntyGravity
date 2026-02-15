@echo off
set /p KEYSTORE_PASS="Introduce la contrasena para el Keystore (min 6 caracteres): "
set /p KEY_ALIAS="Introduce el Alias para la llave (ej: mi-app-release): "
set /p KEY_PASS="Introduce la contrasena para la llave '%KEY_ALIAS%' (min 6 caracteres): "

echo Generando Keystore...
keytool -genkeypair -v ^
  -keystore app\release.keystore ^
  -alias %KEY_ALIAS% ^
  -keyalg RSA ^
  -keysize 4096 ^
  -sigalg SHA256withRSA ^
  -validity 10000 ^
  -storepass %KEYSTORE_PASS% ^
  -keypass %KEY_PASS% ^
  -dname "CN=STAenTurno, OU=IT, O=Empresa, L=Ciudad, ST=Estado, C=ES"

echo.
echo ============================================================
echo Keystore generado exitosamente: release.keystore
echo GUARDALO EN UN LUGAR SEGURO. NO LO SUBAS AL REPOSITORIO GIT.
echo ============================================================
echo.
pause
