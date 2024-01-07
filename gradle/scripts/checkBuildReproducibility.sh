#!/bin/bash -e

rm -rf checksums*

BUILD_TIMESTAMP=$(date -Iseconds)

function calculate_checksums() {
    OUTPUT=$1

    ./gradlew \
        --no-build-cache \
        -Porg.gradle.java.installations.auto-download=false \
        -Dscan.tag.Reproducibility \
        -Pmanifest.buildTimestamp="${BUILD_TIMESTAMP}" \
        clean \
        assemble

    find . -name '*.jar' \
        | grep '/build/libs/' \
        | grep --invert-match 'javadoc' \
        | sort \
        | xargs sha256sum > "${OUTPUT}"
}


calculate_checksums checksums-1.txt
calculate_checksums checksums-2.txt

diff checksums-1.txt checksums-2.txt
