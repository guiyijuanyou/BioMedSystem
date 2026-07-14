@echo off
cd /d "%~dp0"

set "APP_JAR=backend\target\biomed-digital-system-1.0.0.jar"
set "MVNW_CMD=%~dp0backend\mvnw.cmd"


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
call npm install
if errorlevel 1 (
  echo.
  echo Frontend dependency installation failed.
  popd
  pause
  exit /b 1
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
if not exist "%MVNW_CMD%" (
  echo.
  echo Maven Wrapper not found. Run: cd backend ^&^& mvn wrapper:wrapper -Dmaven=3.9.9
  pause
  exit /b 1
)
pushd backend
call "%MVNW_CMD%" -Pprod -DskipTests package
popd
if errorlevel 1 (
  echo.
  echo Backend build failed. Check the network and Maven configuration.
  pause
  exit /b 1
)

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
