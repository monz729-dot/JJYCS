@REM Maven Wrapper Script for Windows

@echo off
set MAVEN_WRAPPER_VERSION=3.9.6
set MAVEN_WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar

if not exist .mvn\wrapper mkdir .mvn\wrapper

if not exist %MAVEN_WRAPPER_JAR% (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '%MAVEN_WRAPPER_JAR%'"
)

"C:\Program Files\Microsoft\jdk-17.0.16.8-hotspot\bin\java.exe" -classpath %MAVEN_WRAPPER_JAR% -Dmaven.multiModuleProjectDirectory="%CD%" org.apache.maven.wrapper.MavenWrapperMain %*