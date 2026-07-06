@echo off
cd /d "%~dp0"

set "APP_JAR=target\biomed-digital-system-1.0.0.jar"
set "MAVEN_CMD=D:\IntelliJ IDEA 2024.3.1.1\plugins\maven\lib\maven3\bin\mvn.cmd"

netstat -ano | findstr /R /C:":8088 .*LISTENING" >nul
if not errorlevel 1 (
  echo.
  echo The system is already running at http://localhost:8088
  echo.
  ping 127.0.0.1 -n 4 >nul
  exit /b 0
)

if not exist "%APP_JAR%" (
  echo Application package not found. Building the project...
  if exist "%MAVEN_CMD%" (
    call "%MAVEN_CMD%" -DskipTests package
  ) else (
    call mvn.cmd -DskipTests package
  )
  if errorlevel 1 (
    echo.
    echo Build failed. Check the network and Maven configuration.
    pause
    exit /b 1
  )
)

echo.
echo Starting Biomed Digital Information System...
echo Open http://localhost:8088 after startup.
echo Keep this window open.
echo.

java -jar "%APP_JAR%" --spring.profiles.active=local

if errorlevel 1 (
  echo.
  echo Startup failed. Check the error messages above.
  pause
  exit /b 1
)
