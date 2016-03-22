::#!
@echo off
SETLOCAL
SET AMANDROID_DIST=true
SET AMANDROID_HOME=%~dp0
SET SCRIPT=%AMANDROID_HOME%%~nx0
SET FILE1=%SCRIPT%
SET FILE2=%SCRIPT%.jar

IF NOT EXIST %AMANDROID_HOME%platform (
  md %AMANDROID_HOME%platform
)
IF NOT EXIST %AMANDROID_HOME%platform\java (
  echo "Did not find java. Run setup.bat first."
  EXIT /B -1
)
IF NOT EXIST %AMANDROID_HOME%platform\scala (
  echo "Did not find scala. Run setup.bat first."
  EXIT /B -1
)

SET JAVA_HOME=%AMANDROID_HOME%platform\java
SET SCALA_HOME=%AMANDROID_HOME%platform\scala
SET "PATH=%AMANDROID_HOME%platform\java\bin;%AMANDROID_HOME%platform\scala\bin;%PATH%"

CALL scala -target:jvm-1.8 -nocompdaemon -savecompiled %SCALA_OPTIONS% %SCRIPT% %AMANDROID_HOME% %*
SET CODE=%ERRORLEVEL%
ENDLOCAL
EXIT /B %CODE%
::!#
AmandroidMain.main(args)