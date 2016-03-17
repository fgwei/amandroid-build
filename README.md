amandroid-build
=================
Build Amandroid using the Simple Build Tool (SBT)

## Setup

Requirement: Java 8 or above

a. Execute following command to prepare the codebase: 
```bash
setup.sh
```
b. To resolve sireum-amandroid-build Eclipse project dependency, run:
```bash
tools/bin/sbt-init
```

See http://www.scala-sbt.org/release/docs/index.html for more documentation on SBT. And check [issue 1](https://github.com/fgwei/amandroid-build/issues/1) if you face sbt downloading issues.

> Hints: If you want to build Amandroid with your forked reporitory, you should modify `setup.sh` to have your reporitory's URL.

## Build Amandroid

Execute following command to build Amandroid:
```bash
tools/bin/sbt clean compile package-bin "build-amandroid /path/of/Amandroid"
```

> Hints: `/path/of/Amandroid` is the place you want to have Amandroid generated, and the path should not have any space. For Windows user it's should be `X:\path\of\Amandroid` and path length don't go beyond 1024.

## Run Amandroid

### For MacOS and Linux user

Requirement: wget, tar and unzip.

a. Go to `/path/of/Amandroid`, or set `/path/of/Amandroid` in your PATH.

b. [Optional] If you want to set the Java heap size (most of the time you do need).
```bash
export JAVA_OPTS=-Xms512m-Xmx8g
```
c. Run:
```bash
amandroid your.class.name arg1 arg2 ...
```

Examples commands, which will invoke object `org.sireum.amandroid.cli.TaintAnalysis`'s main method with four arguments:
```bash
amandroid org.sireum.amandroid.cli.TaintAnalysis DATA_LEAKAGE false /amandroid/sources/icc-bench /output/icc-bench
```

### For Windows user

Requirement: wget, unzip (Those two software can be downloaded from http://gnuwin32.sourceforge.net)

a. Go to `X:\path\of\Amandroid`, or set `X:\path\of\Amandroid` in your PATH.

b. [Optional] If you want to set the Java heap size (most of the time you do need).
```bash
SET JAVA_OPTS=-Xms512m-Xmx8g
```
c. Run:
```bash
amandroid.bat your.class.name arg1 arg2 ...
```

Examples commands, which will invoke object `org.sireum.amandroid.cli.TaintAnalysis`'s main method with four arguments:
```bash
amandroid.bat org.sireum.amandroid.cli.TaintAnalysis DATA_LEAKAGE false /amandroid/sources/icc-bench /output/icc-bench
```
