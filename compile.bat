@echo off
REM Fiabrica Build Script (Windows)
REM Auto-downloads gradle-wrapper.jar, detects setup, and builds project

setlocal enabledelayedexpansion

echo ========================================
echo   Fiabrica - Automated Build Script
echo ========================================
echo.

set WRAPPER_JAR=gradle\wrapper\gradle-wrapper.jar

REM Check if gradle-wrapper.jar exists
if not exist "%WRAPPER_JAR%" (
    echo [INFO] gradle-wrapper.jar not found. Downloading...
    
    REM Create wrapper directory if not exists
    if not exist "gradle\wrapper" mkdir gradle\wrapper
    
    REM Download using PowerShell (available on Windows 7+)
    set WRAPPER_URL=https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradle/wrapper/gradle-wrapper.jar
    
    echo [OK] Using PowerShell to download wrapper...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%' -UseBasicParsing}"
    
    if exist "%WRAPPER_JAR%" (
        echo [OK] gradle-wrapper.jar downloaded successfully!
    ) else (
        echo [ERROR] Failed to download gradle-wrapper.jar
        echo [INFO] Please download manually from:
        echo         %WRAPPER_URL%
        pause
        exit /b 1
    )
) else (
    echo [OK] gradle-wrapper.jar already exists.
)

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat script not found!
    pause
    exit /b 1
)

REM Check Java version
echo.
echo [INFO] Checking Java version...

java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found! Please install Java 21 or higher.
    echo [INFO] Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Get Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "delims=. tokens=1" %%v in ("%JAVA_VERSION%") do set JAVA_MAJOR=%%v

if %JAVA_MAJOR% LSS 21 (
    echo [ERROR] Java %JAVA_MAJOR% detected, but Java 21+ is required!
    echo [INFO] Please install Java 21 or higher from: https://adoptium.net/
    pause
    exit /b 1
) else (
    echo [OK] Java %JAVA_MAJOR% detected (required: 21+)
)

REM Clean previous builds (optional)
echo.
set /p CLEAN_BUILD="Clean previous builds? (y/N): "
if /i "%CLEAN_BUILD%"=="y" (
    echo [INFO] Cleaning build directory...
    call gradlew.bat clean
    echo [OK] Build cleaned.
)

REM Build the project
echo.
echo [INFO] Building fiabrica...
echo ========================================
call gradlew.bat build --no-daemon

REM Check if build was successful
if errorlevel 1 (
    echo.
    echo ========================================
    echo [FAILED] Build failed! Check errors above.
    echo ========================================
    pause
    exit /b 1
)

echo.
echo ========================================
echo [SUCCESS] Build completed successfully!
echo ========================================
echo.

REM Find built jar
for /f "delims=" %%i in ('dir /b /s build\libs\fiabrica-*.jar 2^>nul ^| findstr /v "sources"') do set JAR_FILE=%%i

if defined JAR_FILE (
    echo [OK] Built JAR: !JAR_FILE!
    for %%A in ("!JAR_FILE!") do echo [OK] Size: %%~zA bytes
    echo.
    echo [INFO] Place this JAR in your Minecraft mods folder:
    echo          %%APPDATA%%\.minecraft\mods\
)

echo.
echo Done!
pause
