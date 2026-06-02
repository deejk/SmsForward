@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d C:\dev\smsforward
call gradlew.bat assembleRelease --warning-mode=summary
