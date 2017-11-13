#!/bin/bash
set -e

JDK_FEATURE=9.0.1
JDK_ARCHIVE=openjdk-${JDK_FEATURE}_linux-x64_bin.tar.gz

cd ~
wget http://download.java.net/java/GA/jdk9/${JDK_FEATURE}/binaries/${JDK_ARCHIVE}
tar -xzf ${JDK_ARCHIVE}
export JAVA_HOME=~/jdk-${JDK_FEATURE}
export PATH=${JAVA_HOME}/bin:$PATH
cd -
java --version
