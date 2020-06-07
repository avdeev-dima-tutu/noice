#!/usr/bin/env bash
#build
APP_MODULE="ui_test_snapshot_sample_app"
BUILD_FLAVOR="" #todo
./gradlew clean ui_test_snhp-server:installDist $APP_MODULE:assembleDebug $APP_MODULE:assembleDebugAndroidTest --no-daemon
mkdir build-artifacts
mv $APP_MODULE/build/outputs/apk/debug/$APP_MODULE-debug.apk build-artifacts/
mv $APP_MODULE/build/outputs/apk/androidTest/debug/$APP_MODULE-debug-androidTest.apk build-artifacts/
cp -r ui_test_snhp-server/build/install/app build-artifacts/ui_test_snhp-server

