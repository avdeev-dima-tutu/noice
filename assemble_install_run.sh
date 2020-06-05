#!/usr/bin/env bash
#build
./gradlew clean server:installDist app:assembleDebug app:assembleDebugAndroidTest --no-daemon
APK="app/build/outputs/apk/debug/app-debug.apk"
TEST_APK="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"

#install
adb install $APK
adb install $TEST_APK

#run
./server/build/install/app/bin/app &
adb shell am instrument -w -r -e class 'com.github.ashutoshgngwr.noice.fragment.AboutFragmentTest#testAboutItemClick' com.github.ashutoshgngwr.noice.debug.test/androidx.test.runner.AndroidJUnitRunner
#https://developer.android.com/studio/test/command-line
echo "end of script"
kill %1
