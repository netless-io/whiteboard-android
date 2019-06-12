#!/bin/bash
BASEDIR=$(cd $(dirname "$0"); pwd -P)
set -exo pipefail
cd $BASEDIR

rm -rf sdk/src/main/assets/cloud/
mkdir sdk/src/main/assets/cloud
cp -R ../white-native-sdk/build/index.html sdk/src/main/assets/cloud/
cp -R ../white-native-sdk/build/favicon.ico sdk/src/main/assets/cloud/
cp -R ../white-native-sdk/build/static sdk/src/main/assets/cloud/
rabbit