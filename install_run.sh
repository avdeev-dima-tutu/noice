#!/usr/bin/env bash

#install
APK="build-artifacts/temp_app-debug.apk"
TEST_APK="build-artifacts/temp_app-debug-androidTest.apk"
adb install $APK
adb install $TEST_APK

#run
chmod -R 777 ./build-artifacts/snapshot-server
./build-artifacts/snapshot-server/bin/app &
#todo error if server not exists
adb shell am instrument -w -r -e class 'com.github.ashutoshgngwr.noice.fragment.AboutFragmentTest#testAboutItemClick' com.github.ashutoshgngwr.noice.debug.test/androidx.test.runner.AndroidJUnitRunner
#https://developer.android.com/studio/test/command-line
echo "end of script"
kill %1
