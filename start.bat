@echo off
cd /d "%~dp0"

set "APP_JAR=backend\target\biomed-digital-system-1.0.0.jar"
set "MAVEN_CMD=D:\IntelliJ IDEA 2024.3.1.1\plugins\maven\lib\maven3\bin\mvn.cmd"
set "MAVEN_SETTINGS=%USERPROFILE%\.m2\settings.xml"


netstat -ano | findstr /R /C:":8088 .*LISTENING" >nul
if not errorlevel 1 (
  echo.
  echo The system is already running at http://localhost:8088
  echo.
  ping 127.0.0.1 -n 4 >nul
  exit /b 0
)

echo Building Vue frontend...
pushd frontend
if not exist "node_modules" (
  call npm install
  if errorlevel 1 (
    echo.
    echo Frontend dependency installation failed.
    popd
    pause
    exit /b 1
  )
)
call npm run build
if errorlevel 1 (
  echo.
  echo Frontend build failed.
  popd
  pause
  exit /b 1
)
popd

echo Building Spring Boot backend...
pushd backend
if exist "%MAVEN_CMD%" (
  if exist "%MAVEN_SETTINGS%" (
    call "%MAVEN_CMD%" -s "%MAVEN_SETTINGS%" -DskipTests package
  ) else (
    call "%MAVEN_CMD%" -DskipTests package
  )
) else (
  if exist "%MAVEN_SETTINGS%" (
    call mvn.cmd -s "%MAVEN_SETTINGS%" -DskipTests package
  ) else (
    call mvn.cmd -DskipTests package
  )
)
if errorlevel 1 (
  echo.
  echo Backend build failed. Check the network and Maven configuration.
  popd
  pause
  exit /b 1
)
popd

if not exist "%APP_JAR%" (
  echo.
  echo Application package was not generated.
  pause
  exit /b 1
)

echo.
echo Starting Biomed Digital Information System...
echo Open http://localhost:8088 after startup.
echo Keep this window open.
echo.

java -jar "%APP_JAR%"

if errorlevel 1 (
  echo.
  echo Startup failed. Check the error messages above.
  pause
  exit /b 1
)
