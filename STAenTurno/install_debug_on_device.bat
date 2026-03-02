@echo off
echo Cleaning project...
call gradlew.bat clean

echo Checking for connected devices...
"%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices

echo IMPORTANT: Please ensure your physical device is connected via USB and debugging is enabled.
pause

echo Uninstalling previous version...
"%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe" uninstall com.sta.staenturno

echo Building and installing DEBUG APK...
call gradlew.bat installDebug

echo Launching app...
"%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.sta.staenturno/com.sta.staenturno.MainActivity

echo Done! If this works, the issue was with the Release build configuration (ProGuard).
pause
