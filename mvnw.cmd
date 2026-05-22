@ECHO OFF
SETLOCAL

SET "APP_HOME=%~dp0"
IF "%APP_HOME:~-1%"=="\" SET "APP_HOME=%APP_HOME:~0,-1%"

SET "WRAPPER_JAR=%APP_HOME%\.mvn\wrapper\maven-wrapper.jar"
SET "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

IF DEFINED JAVA_HOME (
    SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
    SET "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" -cp "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%APP_HOME%" %WRAPPER_LAUNCHER% %*

EXIT /B %ERRORLEVEL%
