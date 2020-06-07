#!/usr/bin/env bash
#build
APP_MODULE="temp_app"
BUILD_FLAVOR="" #todo
./gradlew clean snapshot-server:installDist $APP_MODULE:assembleDebug $APP_MODULE:assembleDebugAndroidTest --no-daemon
mkdir build-artifacts
mv temp_app/build/outputs/apk/debug/temp_app-debug.apk build-artifacts/
mv temp_app/build/outputs/apk/androidTest/debug/temp_app-debug-androidTest.apk build-artifacts/
cp -r snapshot-server/build/install/app build-artifacts/snapshot-server

