#!/usr/bin/env bash

URL=$1
printf 'Waiting for %s' "$URL"
until curl --output /dev/null --silent --location --head --fail "$URL"; do
    printf '.'
    sleep 5
done
echo ' OK'
