#!/usr/bin/env bash

readonly checksum_directory='documentation/build/checksum'
readonly current="${checksum_directory}/current-checksum.txt"
readonly published="${checksum_directory}/published-checksum.txt"
readonly github_pages_url='https://raw.githubusercontent.com/junit-team/junit5/gh-pages/docs/snapshot/published-checksum.txt'

#
# always generate current sums
#
echo "Generating checksum file ${current}..."
mkdir --parents "${checksum_directory}"
md5sum documentation/documentation.gradle > "${current}"
md5sum $(find documentation/src/ -type f) >> "${current}"
md5sum $(find . -wholename '**/src/main/java/*.java') >> "${current}"
sort --output "${current}" "${current}"
echo
md5sum "${current}"

#
# compare current with published sums
#
curl --silent --output "${published}" "${github_pages_url}"
md5sum "${published}"
if cmp --silent "${current}" "${published}" ; then
  #
  # no changes detected: we're done
  #
  echo
  echo "Already published documentation with same source checksum."
  echo
else
  #
  # update checksum file and trigger new documentation build and upload
  #
  echo
  echo "Creating and publishing documentation..."
  echo
  cp --force "${current}" "${published}"
  ./gradlew --scan gitPublishPush
fi
