#!/bin/bash -e

rm -rf checksums*

export SOURCE_DATE_EPOCH=$(date +%s)

function calculate_checksums() {
    OUTPUT=$1

    ./gradlew --no-build-cache clean assemble --parallel

    find . -name '*.jar' \
        | grep '/build/libs/' \
        | grep --invert-match 'javadoc' \
        | sort \
        | xargs sha256sum > ${OUTPUT}
}


calculate_checksums checksums-1.txt
calculate_checksums checksums-2.txt

diff checksums-1.txt checksums-2.txt
