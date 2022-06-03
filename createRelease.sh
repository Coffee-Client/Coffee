#!/usr/bin/env bash
echo "Input changelog, enter \"end\" when done"
echo -n "" > ./src/main/resources/changelogLatest.txt
while [[ true ]]; do
  read -p "> " line
  if [ "$line" == "end" ]; then
    break
    fi
  echo $line >> ./src/main/resources/changelogLatest.txt
done

ver=`cat ./src/main/resources/version.txt`
verNew=$((ver+1))
echo "Version: $ver -> $verNew"
echo -n "$verNew" > ./src/main/resources/version.txt

echo "Running build"
./gradlew build
if [[ ! -d bin ]]; then
  mkdir bin
  fi
mv ./build/libs/coffee-1.0.0.jar bin/latest.jar
mv ./build/libs/coffee-1.0.0-dev.jar bin/latest-sdk.jar
mv ./build/libs/coffee-1.0.0-sources-dev.jar bin/latest-sdk-sources.jar
echo "Made release"
