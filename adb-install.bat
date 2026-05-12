@echo off
set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;%PATH%"
cd /d C:\dev\smsforward
echo === adb devices ===
adb devices
echo.
echo === adb install ===
adb install -r app\build\outputs\apk\debug\app-debug.apk
