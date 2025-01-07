#!/usr/bin/env bash

URL_PATH=$1
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
"$SCRIPT_DIR"/waitForUrl.sh "https://repo1.maven.org/maven2/$URL_PATH"
