#!/bin/bash
BASEDIR=$(cd $(dirname "$0"); pwd -P)
set -exo pipefail
cd $BASEDIR

rm -rf sdk/src/main/assets/
mkdir -p sdk/src/main/assets/whiteboard
cp -R ../whiteboard-bridge/build/* sdk/src/main/assets/whiteboard
rabbit