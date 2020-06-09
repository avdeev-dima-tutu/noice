#!/usr/bin/env bash

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

./gradlew ui_test_prepate_emulator_app:cAT

##Костыль для включения русского языка. Работает на девайсе -skin 1080x1920 -dpi-device 420 api 27
#
#sleep 5
#
#echo "открываем системную настройку языка"
#adb shell am start -a android.settings.LOCALE_SETTINGS
#
#sleep 3
#
#echo "Нажимаем кнопку add new language"
#adb shell input tap 500 500
#
#sleep 3
#
#echo "Нажимаем на лупу поиска"
#adb shell input tap 1060 80
#
#sleep 3
#
#echo "Вводим в поиск текст Russian"
#adb shell input text Russian
#
#sleep 3
#
#echo "Click on русский"
#adb shell input tap 1 350
#
#sleep 3
#
#echo "Click on Россия"
#adb shell input tap 1 400
#
#sleep 3
#
#echo "swipe russian language up"
#adb shell input swipe 1000 500 1000 300 500
#
#sleep 1
#echo "Всё, русский язык включен"

