#!/usr/bin/env bash

readonly current='documentation/build/current-checksum.txt'
readonly published='documentation/build/published-checksum.txt'
readonly github_pages_url='https://raw.githubusercontent.com/junit-team/junit5/gh-pages/docs/snapshot/published-checksum.txt'

#
# always generate current sums
#
echo "Generating checksum file..."
md5sum documentation/documentation.gradle > "${current}"
md5sum $(find documentation/src/ -type f) >> "${current}"
md5sum $(find . -wholename '**/src/main/java/*.java') >> "${current}"
stat "${current}"
md5sum "${current}"

#
# compare current with published sums
#
curl --silent --output "${published}" "${github_pages_url}"
if cmp --silent "${current}" "${published}" ; then
  #
  # no changes detected: we're done
  #
  echo "Already published documentation with same source checksum."
else
  #
  # update checksum file and trigger new documentation build and upload
  #
  echo "Creating and publishing documentation..."
  cp --force "${current}" "${published}"
  ./gradlew --scan gitPublishPush
fi
