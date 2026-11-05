Releasing
=========

Cutting a Release
-----------------

1. Update `CHANGELOG.md`.

   Build Bridge from a clean committed checkout with registry dependencies, then
   run `./copy_js_bundle.sh /absolute/path/to/bridge/build`. The argument is
   optional and defaults to `../Whiteboard-bridge/build`. Update `carrot.yml`
   to that Bridge commit and verify that its short-hash marker is empty.

2. Set versions:
   ```shell
   export RELEASE_VERSION=$(awk -F '[][]' '/\[/{print $2; exit}' CHANGELOG.md) \
   && echo "RELEASE_VERSION=$RELEASE_VERSION"
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
   git add sdk/src/main/assets carrot.yml sdk/src/main/java/com/herewhite/sdk/WhiteSdk.java CHANGELOG.md README.md README_zh_CN.md
   git commit -m "release $RELEASE_VERSION"
   git tag -a $RELEASE_VERSION -m "Version $RELEASE_VERSION"
   git push -v origin refs/heads/master:refs/heads/master
   git push origin $RELEASE_VERSION
   ```
5. Request the Maven POM to trigger or retrieve the JitPack build, then query its status.
   ```shell
   curl --fail --location --max-time 60 \
     "https://jitpack.io/com/github/netless-io/whiteboard-android/${RELEASE_VERSION}/whiteboard-android-${RELEASE_VERSION}.pom"
   curl --fail --location --max-time 30 \
     "https://jitpack.io/api/builds/com.github.netless-io/whiteboard-android/${RELEASE_VERSION}"
   ```

   The build-status API only queries the outcome; it is not a build trigger.
   If a request returns HTTP 429, retain the existing tag and response evidence,
   allow time before retrying, and do not recreate the tag or repeatedly trigger requests.

6. Verify publication before announcing Maven availability:
   - Build status is `ok` and its commit matches the release tag.
   - Anonymous POM and AAR downloads succeed at the published coordinates.
   - The POM version, AAR SDK version and embedded Bridge resources match the release.

   A pushed tag, a GitHub Release, or a `Queued` entry alone does not confirm
   that the package is available to Maven consumers. See the workspace
   `docs/upgrade-release.md` for operator identity and release verification requirements.
