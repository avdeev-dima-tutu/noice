#!/usr/bin/env bash
#build
./gradlew clean server:installDist app:assembleDebug app:assembleDebugAndroidTest --no-daemon
mkdir artifacts
mv app/build/outputs/apk/debug/app-debug.apk artifacts/
mv app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk artifacts/

