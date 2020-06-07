#!/usr/bin/env bash
#build
./gradlew clean server:installDist temp_app:assembleDebug temp_app:assembleDebugAndroidTest --no-daemon
mkdir build-artifacts
mv temp_app/build/outputs/apk/debug/temp_app-debug.apk build-artifacts/
mv temp_app/build/outputs/apk/androidTest/debug/temp_app-debug-androidTest.apk build-artifacts/
cp -r server/build/install/app build-artifacts/snapshot-server

