#!/usr/bin/env bash

#
# Install JDK for Linux
#
# This script determines the most recent early-access build number,
# downloads the JDK archive to the user home directory and extracts
# it there.
#
# Example usage
#
#   install-jdk.sh                 | install most recent early-access JDK
#   install-jdk.sh -W /usr/opt     | install most recent early-access JDK to /usr/opt
#   install-jdk.sh -F 9            | install most recent OpenJDK 9
#   install-jdk.sh -F 10           | install most recent OpenJDK 10
#   install-jdk.sh -F 10 -L BCL    | install most recent OracleJDK 10
#
# Options
#
#   -F f | Feature number of the JDK release, [9|10|...]
#   -B b | Build number of the JDK release    [?|1|2...]
#   -L l | License of the JDK                 [GPL|BCL]
#   -W w | Working directory and install path [${HOME}]
#
# Exported environment variables
#
#   JAVA_HOME is set to the extracted JDK directory
#   PATH is prepended with ${JAVA_HOME}/bin
#
# (C) 2018 Christian Stein
#
# https://github.com/sormuras/bach/blob/master/install-jdk.sh
#
set -e

JDK_FEATURE='10'
JDK_BUILD='?'
JDK_LICENSE='GPL'
JDK_WORKSPACE=${HOME}

while getopts F:B:L:W: option
do
  case "${option}" in
    F) JDK_FEATURE=${OPTARG};;
    B) JDK_BUILD=${OPTARG};;
    L) JDK_LICENSE=${OPTARG};;
    W) JDK_WORKSPACE=${OPTARG};;
 esac
done

#
# Other constants
#
JDK_DOWNLOAD='https://download.java.net/java'
JDK_BASENAME='openjdk'
if [ "${JDK_LICENSE}" != 'GPL' ]; then
  JDK_BASENAME='jdk'
fi

#
# 9
#
if [ "${JDK_FEATURE}" == '9' ]; then
  if [ "${JDK_BUILD}" == '?' ]; then
    TMP=$(curl -L jdk.java.net/${JDK_FEATURE})
    TMP="${TMP#*<h1>JDK}"                                   # remove everything before the number
    TMP="${TMP%%General-Availability Release*}"             # remove everything after the number
    JDK_BUILD="$(echo -e "${TMP}" | tr -d '[:space:]')"     # remove all whitespace
  fi

  JDK_ARCHIVE=${JDK_BASENAME}-${JDK_BUILD}_linux-x64_bin.tar.gz
  JDK_URL=${JDK_DOWNLOAD}/GA/jdk${JDK_FEATURE}/${JDK_BUILD}/binaries/${JDK_ARCHIVE}
  JDK_HOME=jdk-${JDK_BUILD}
fi

#
# 10
#
if [ "${JDK_FEATURE}" == '10' ]; then
  if [ "${JDK_BUILD}" == '?' ]; then
    TMP=$(curl -L jdk.java.net/${JDK_FEATURE})
    TMP="${TMP#*Most recent build: jdk-${JDK_FEATURE}+}"    # remove everything before the number
    TMP="${TMP%%<*}"                                        # remove everything after the number
    JDK_BUILD="$(echo -e "${TMP}" | tr -d '[:space:]')"     # remove all whitespace
  fi

  JDK_ARCHIVE=${JDK_BASENAME}-${JDK_FEATURE}+${JDK_BUILD}_linux-x64_bin.tar.gz
  JDK_URL=${JDK_DOWNLOAD}/jdk${JDK_FEATURE}/archive/${JDK_BUILD}/${JDK_LICENSE}/${JDK_ARCHIVE}
  JDK_HOME=jdk-${JDK_FEATURE}
fi

#
# Create any missing intermediate paths, switch to workspace, download, unpack, switch back.
#
mkdir -p ${JDK_WORKSPACE}
cd ${JDK_WORKSPACE}
wget ${JDK_URL}
tar -xzf ${JDK_ARCHIVE}
cd -

#
# Update environment and test-drive.
#
export JAVA_HOME=${JDK_WORKSPACE}/${JDK_HOME}
export PATH=${JAVA_HOME}/bin:$PATH

java --version
