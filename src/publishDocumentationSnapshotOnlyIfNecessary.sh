#!/usr/bin/env bash

readonly current='documentation/build/current-checksum.txt'
readonly published='documentation/build/published-checksum.txt'
readonly github_pages_url='https://raw.githubusercontent.com/junit-team/junit5/gh-pages/docs/snapshot/published-checksum.txt'

#
# always generate current sums
#
rm --force "${current}"
md5sum documentation/documentation.gradle > "${current}"
find documentation/src/ -type f -exec md5sum {} + > "${current}"

#
# compare current with published sums
#
rm --force "${published}"
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
  echo "./gradlew --scan gitPublishPush"
fi
