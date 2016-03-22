::#!
@echo off
SETLOCAL
SET java_version=1.8.0_66
SET java_build_num=8.11.0.1
SET scala_version=2.11.7
SET download=false
SET RELOAD=false
SET AMANDROID_HOME=%~dp0
SET SCRIPT=%AMANDROID_HOME%%~nx0
SET FILE1=%SCRIPT%
SET FILE2=%SCRIPT%.jar

wget >nul 2>&1
IF %ERRORLEVEL% NEQ 1 (
  ECHO wget wasn't found, you can download it from http://gnuwin32.sourceforge.net/packages/wget.htm
  EXIT /B -1
)
unzip >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
  ECHO unzip wasn't found, you can download it from http://gnuwin32.sourceforge.net/packages/unzip.htm
  EXIT /B -1
)
IF NOT EXIST %AMANDROID_HOME%platform (
  md %AMANDROID_HOME%platform
)
IF NOT EXIST %AMANDROID_HOME%platform\java (
  SET download=true
)
IF %download% == true (
  IF EXIST %AMANDROID_HOME%platform\java (
    RD %AMANDROID_HOME%platform\java /S /Q
  )
  CALL:startDownloadJava
)
IF %download% == true (
  echo "Fail to download java. You can download %java_version% manually from https://www.azul.com/products/zulu/ then unzip it as %AMANDROID_HOME%/platform/java"
  EXIT /B -1
)
IF NOT EXIST %AMANDROID_HOME%platform\scala (
  SET download=true
)
IF %download% == true (
  RD %AMANDROID_HOME%platform\scala /S /Q
  CALL:startDownloadScala
)
IF %download% == true (
  echo "Fail to download scala. You can download %scala_version% manually from http://www.scala-lang.org/download/ then unzip it as %AMANDROID_HOME%/platform/scala."
  EXIT /B -1
)
ENDLOCAL
EXIT /B 0
:startDownloadJava
  SET BASE_URL=http://cdn.azulsystems.com/zulu/bin/
  SET OS=win64
  SET FILENAME=zulu%java_version%-%java_build_num%-%OS%.zip
  echo Downloading %FILENAME%...
  wget --no-verbose --referer=http://www.azulsystems.com/products/zulu/downloads -q -c %BASE_URL%%FILENAME% -O %AMANDROID_HOME%platform\%FILENAME% > NUL 2>&1 && (
    SET download=false
    unzip %AMANDROID_HOME%platform\%FILENAME% -d %AMANDROID_HOME%platform\ > NUL 2>&1
    DEL %AMANDROID_HOME%platform\%FILENAME% > NUL 2>&1
    MOVE /Y %AMANDROID_HOME%platform\zulu%java_version%-%java_build_num%-%OS% %AMANDROID_HOME%platform\java > NUL
    DEL %SCRIPT%.jar > NUL 2>&1
    SET RELOAD=true
  )
EXIT /B 0
:startDownloadScala
  SET BASE_URL=http://downloads.lightbend.com/scala/%scala_version%/
  SET FILENAME=scala-%scala_version%.zip
  echo Downloading %FILENAME%...
  wget --no-verbose -q -c -O %AMANDROID_HOME%platform\%FILENAME% %BASE_URL%%FILENAME% > NUL 2>&1 && (
    SET download=false
    unzip %AMANDROID_HOME%platform\%FILENAME% -d %AMANDROID_HOME%platform\ > NUL 2>&1
    DEL %AMANDROID_HOME%platform\%FILENAME% > NUL 2>&1
    MOVE /Y %AMANDROID_HOME%platform\scala-%scala_version% %AMANDROID_HOME%platform\scala > NUL
    DEL %SCRIPT%.jar > NUL 2>&1
    SET RELOAD=true
  )
EXIT /B 0
::!#
AmandroidMain.main(args)