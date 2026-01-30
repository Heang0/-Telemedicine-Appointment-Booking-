@echo off
echo [Telemedicine2 Build Script]
echo Attempting to locate Java...

set JDK_PATH=

:: Try Android Studio embedded JDK (most likely)
if exist "C:\Users\ADMIN\AppData\Local\Android\sdk\jre\bin\java.exe" (
    set JDK_PATH=C:\Users\ADMIN\AppData\Local\Android\sdk\jre
) else if exist "C:\Users\ADMIN\AppData\Local\Android\sdk\jdk\bin\java.exe" (
    set JDK_PATH=C:\Users\ADMIN\AppData\Local\Android\sdk\jdk
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\java.exe" (
    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
        set JDK_PATH=%%i
        goto found
    )
) else if exist "C:\Program Files\Java\jdk-17*\bin\java.exe" (
    for /d %%i in ("C:\Program Files\Java\jdk-17*") do (
        set JDK_PATH=%%i
        goto found
    )
)

:found
if defined JDK_PATH (
    echo Using JDK: %JDK_PATH%
    set "JAVA_HOME=%JDK_PATH%"
    set "PATH=%JDK_PATH%\bin;%PATH%"
    echo Running Gradle build...
    call "%~dp0gradlew.bat" build %*
) else (
    echo ERROR: No JDK 17 found.
    echo Please install Eclipse Temurin JDK 17 from:
    echo   https://adoptium.net/temurin/releases/?version=17
    echo Then run this script again.
    pause
)