@echo off
set "ADB=%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe"

echo Checking for connected devices...
"%ADB%" devices
if %ERRORLEVEL% NEQ 0 (
    echo ADB not found or error checking devices. Make sure Android SDK is installed.
    pause
    exit /b
)

echo Cleaning project...
call gradlew.bat clean

echo Uninstalling previous version...
"%ADB%" uninstall com.sta.staenturno

echo Building and installing debug APK...
call gradlew.bat installDebug

echo Launching app...
"%ADB%" shell am start -n com.sta.staenturno/com.sta.staenturno.MainActivity

echo Done! The app should be running now.
pause
