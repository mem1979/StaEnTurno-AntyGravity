@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Usando JAVA_HOME: %JAVA_HOME%
java -version

echo.
echo Ejecutando Gradle SIN DAEMON y forzando JAVA_HOME...
call gradlew.bat --no-daemon -Dorg.gradle.java.home="%JAVA_HOME%" %*
