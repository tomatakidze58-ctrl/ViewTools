@echo off
setlocal
cd /d "%~dp0"
call gradlew.bat build --no-daemon
if errorlevel 1 (
    echo.
    echo Build failed. Make sure Java JDK 25 is installed and available in PATH.
    pause
    exit /b 1
)
echo.
echo Build complete. Your installable mod is in build\libs.
echo Use viewtools-1.0.0+mc26.2.jar, not the -sources.jar file.
pause
