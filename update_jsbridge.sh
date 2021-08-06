#!/bin/bash

# 简单参数判断
if [ $# != 3 ]
then
   # ./update_jsbridge.sh 2.13.18 '更新`white-web-sdk`至 2.13.12' 'update web sdk'
   echo "usage: ./update_jsbridge.sh WHITE_TAG CHANGE_LOG COMMIT_MESSAGE"
   exit -1
fi

BASEDIR=$(cd $(dirname "$0"); pwd -P)
set -exo pipefail
cd $BASEDIR

# 版本号
WHITE_TAG=$1
# 更新日志
CHANGE_LOG=$2
# Git 提交日志
COMMIT_MESSAGE=$3

# 编译JsBridge
while true; do
    read -p "JsBridge: Do you wish build jsbridge? " yn
    case $yn in
        [Yy]* ) cd ../Whiteboard-bridge/ && yarn && yarn build && cd - ; break;;
        [Nn]* ) break ;;
        * ) break ;;
    esac
done

# 资源复制
./copy_js_bundle.sh

# 版本号更新
sed -i "" -r "s/^(.*private final static String SDK_VERSION = )(.*)(;.*)/\1\"${WHITE_TAG}\"\3/" sdk/src/main/java/com/herewhite/sdk/WhiteSdk.java

# ChangeLog 更新
sed -i "" "2i\\
## [$WHITE_TAG] - `date +"%Y-%m-%d"`\\
- ${CHANGE_LOG}
" sdk/CHANGELOG.md

# Git add
git add sdk/src/main/assets carrot.yml sdk/src/main/java/com/herewhite/sdk/WhiteSdk.java sdk/CHANGELOG.md

# Print ChangeLog
echo "Git Change Start >>>>>>>>>>>>>>>>>>>>>>>>>>"
git diff --staged |grep "^[+-] "
echo "Git Change End   <<<<<<<<<<<<<<<<<<<<<<<<<<"
while true; do
    read -p "Git: Do you wish to commit with message \"${COMMIT_MESSAGE}\"? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) echo "cancel git commit!!!" && exit;;
        * ) echo "Please answer yes or no.";;
    esac
done
git commit -m "${COMMIT_MESSAGE}"

# Push
while true; do
    read -p "Push: Do you wish to push master to remote? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) echo "cancel push master!!!" && exit;;
        * ) echo "Please answer yes or no.";;
    esac
done
git push -v netless refs/heads/master:refs/heads/master
git push -v origin refs/heads/master:refs/heads/master

# Tag
while true; do
    read -p "Tag: Do you wish to tag and push to remote? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) echo "cancel push tag!!!" && exit;;
        * ) echo "Please answer yes or no.";;
    esac
done
git tag $WHITE_TAG
git push origin $WHITE_TAG
git push netless $WHITE_TAG

# Fetch Jitpack, Should End With "/"
curl "https://jitpack.io/com/github/duty-os/white-sdk-android/$WHITE_TAG/"
curl "https://jitpack.io/com/github/netless-io/whiteboard-android/$WHITE_TAG/"

exit 0