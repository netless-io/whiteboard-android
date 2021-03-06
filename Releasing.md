Releasing
=========

Cutting a Release
-----------------

1. Update `CHANGELOG.md`.

2. Set versions:
   ```
   export RELEASE_VERSION=X.Y.Z
   ```
3. Update versions:
   ```
   sed -i "" -r "s/^(.*private final static String SDK_VERSION = )(.*)(;.*)/\1\"${RELEASE_VERSION}\"\3/" \
   sdk/src/main/java/com/herewhite/sdk/WhiteSdk.java
   ```
4. Tag the release and push to GitHub.
   ```
   git add sdk/src/main/assets carrot.yml sdk/src/main/java/com/herewhite/sdk/WhiteSdk.java sdk/CHANGELOG.md
   git commit -m "release $RELEASE_VERSION"
   git tag -a $RELEASE_VERSION -m "Version $RELEASE_VERSION"
   git push -v origin refs/heads/master:refs/heads/master
   git push origin $RELEASE_VERSION
   ```