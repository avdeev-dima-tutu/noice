#!/usr/bin/env bash

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

sleep 2
adb shell am start -a android.settings.LOCALE_SETTINGS
sleep 2
adb shell input tap 500 500 # add new language
sleep 2
adb shell input tap 1060 80 #click on find
sleep 2
adb shell input text Russian
sleep 2
adb shell input tap 1 350 # click on Russian #100 vars
sleep 2
adb shell input tap 1 400 # click on Россия
sleep 2
adb shell input swipe 1000 500 1000 300 500 #move russian up
sleep 2
