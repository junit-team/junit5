#!/bin/bash
set -e

JDK_FEATURE=10
JDK_BUILD=32
JDK_ARCHIVE=jdk-${JDK_FEATURE}-ea+${JDK_BUILD}_linux-x64_bin.tar.gz

cd ~
wget http://download.java.net/java/jdk${JDK_FEATURE}/archive/${JDK_BUILD}/binaries/${JDK_ARCHIVE}
tar -xzf ${JDK_ARCHIVE}
export JAVA_HOME=~/jdk-${JDK_FEATURE}
export PATH=${JAVA_HOME}/bin:$PATH
cd -
java --version
