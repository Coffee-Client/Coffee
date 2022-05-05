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
export JAVA_HOME="$HOME/.jdks/openjdk-17.0.1/"
./gradlew build
if [[ ! -d bin ]]; then
  mkdir bin
  fi
mv ./build/libs/shadow-1.0.0.jar bin
echo "Made release"