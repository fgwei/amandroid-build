amandroid-build
=================
Build Amandroid using the Simple Build Tool (SBT)

# Setup

Requirement: Java 8 or above

1. Execute following command to prepare the codebase: 
```bash
setup.sh
```
2. To resolve sireum-amandroid-build Eclipse project dependency, run:
```bash
tools/bin/sbt-init
```

See http://www.scala-sbt.org/release/docs/index.html for more documentation on SBT.

Hints: If you want to build Amandroid with your forked reporitory, you should modify `setup.sh` to have your reporitory's URL.

# Build Amandroid

Execute following command to build Amandroid:
```bash
tools/bin/sbt clean compile package-bin "build-amandroid /path/of/Amandroid"
```

Hints: `/path/of/Amandroid` is the place you want to have Amandroid generated.

# Run Amandroid

Requirement: wget and unzip.

1. Go to `/path/of/Amandroid`, or set `/path/of/Amandroid` in your PATH.
2. Run:
```bash
amandroid your.class.name args ...
```
