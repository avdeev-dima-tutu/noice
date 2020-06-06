#!/usr/bin/env bash

#install
APK="apk-artifacts/app-debug.apk"
TEST_APK="apk-artifacts/app-debug-androidTest.apk"
adb install $APK
adb install $TEST_APK

#run
./server/build/install/app/bin/app &
adb shell am instrument -w -r -e class 'com.github.ashutoshgngwr.noice.fragment.AboutFragmentTest#testAboutItemClick' com.github.ashutoshgngwr.noice.debug.test/androidx.test.runner.AndroidJUnitRunner
#https://developer.android.com/studio/test/command-line
echo "end of script"
kill %1
