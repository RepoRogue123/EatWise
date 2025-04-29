@echo off
setlocal enabledelayedexpansion

:: Check for refresh-dependencies flag first
set REFRESH=false
for %%a in (%*) do (
    if "%%a"=="--refresh-dependencies" set REFRESH=true
)

:: Set default task to assembleDebug if no task is specified
set TASK=assembleDebug
if not "%1"=="" (
    if not "%1"=="--refresh-dependencies" set TASK=%1
)

echo Running Gradle wrapper with task: %TASK%

:: Temporarily save existing JAVA_HOME
set OLD_JAVA_HOME=%JAVA_HOME%

:: Unset JAVA_HOME to let Gradle use its own Java
set JAVA_HOME=

:: Run the Gradle wrapper with the specified task and refresh dependencies if needed
if "%REFRESH%"=="true" (
    echo Including refresh dependencies flag...
    call gradlew.bat %TASK% --refresh-dependencies --warning-mode all
) else (
    call gradlew.bat %TASK% --warning-mode all
)

:: Restore original JAVA_HOME
set JAVA_HOME=%OLD_JAVA_HOME%

endlocal 