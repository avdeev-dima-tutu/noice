#!/usr/bin/env bash
#build
./gradlew clean server:installDist app:assembleDebug app:assembleDebugAndroidTest --no-daemon
mkdir build-artifacts
mv app/build/outputs/apk/debug/app-debug.apk build-artifacts/
mv app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk build-artifacts/
cp -r server/build/install/app build-artifacts/snapshot-server

