#!/usr/bin/env bash

adb shell getprop #print emulator props
./script_ui_test/ci/prepare_emulator.sh

#install apk's
APK="build-artifacts/ui_test_snapshot_sample_app-debug.apk"
TEST_APK="build-artifacts/ui_test_snapshot_sample_app-debug-androidTest.apk"
adb install $APK
adb install $TEST_APK

# Run snapshot server in background
chmod -R 777 ./build-artifacts/ui_test_snapshot_server
./build-artifacts/ui_test_snapshot_server/bin/app & #todo error if server not exists

#run ui-tests
adb shell am instrument -w -r -e class 'com.github.ashutoshgngwr.noice.fragment.AboutFragmentTest#testAboutItemClick' com.github.ashutoshgngwr.noice.debug.test/androidx.test.runner.AndroidJUnitRunner
#https://developer.android.com/studio/test/command-line
echo "end of script"
kill %1
