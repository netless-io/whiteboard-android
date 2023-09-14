Releasing
=========

Cutting a Release
-----------------

1. Update `CHANGELOG.md`.

2. Set versions:
   ```
   # sed -n 's/.*\[/\[/p' sdk/CHANGELOG.md | head -n 1 | sed 's/\[\(.*\)\].*/\1/'
   export RELEASE_VERSION=X.Y.Z
   ```
3. Update versions:
   ```shell
   sed -i "" -r "s/^(.*private final static String SDK_VERSION = )(.*)(;.*)/\1\"${RELEASE_VERSION}\"\3/" \
   sdk/src/main/java/com/herewhite/sdk/WhiteSdk.java
   sed -i "" \
   "s/\"com.github.netless-io:\([^\:]*\):[^\"]*\"/\"com.github.netless-io:\1:$RELEASE_VERSION\"/g" \
   README.md README_zh_CN.md
   ```
4. Tag the release and push to GitHub.
   ```shell
   git add sdk/src/main/assets carrot.yml sdk/src/main/java/com/herewhite/sdk/WhiteSdk.java sdk/CHANGELOG.md README.md README_zh_CN.md
   git commit -m "release $RELEASE_VERSION"
   git tag -a $RELEASE_VERSION -m "Version $RELEASE_VERSION"
   git push -v origin refs/heads/master:refs/heads/master
   git push origin $RELEASE_VERSION
   ```
5. Trigger Jitpack build
   ```shell
   curl https://jitpack.io/api/builds/com.github.netless-io/whiteboard-android/$RELEASE_VERSION
   ```