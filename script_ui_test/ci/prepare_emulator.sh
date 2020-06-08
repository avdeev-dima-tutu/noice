#!/usr/bin/env bash

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

#Костыль для включения русского языка. Работате на девайсе -skin 1080x1920 -dpi-device 420 api 27
sleep 2
adb shell am start -a android.settings.LOCALE_SETTINGS # открываем системную настройку языка
sleep 2
adb shell input tap 500 500 # Нажимаем кнопку add new language
sleep 2
adb shell input tap 1060 80 # Нажиамаем на лупу поиска
sleep 2
adb shell input text Russian # Вводим в поиск текст Russian
sleep 2
adb shell input tap 1 350 # Click on русский
sleep 2
adb shell input tap 1 400 # Click on Россия
sleep 2
adb shell input swipe 1000 500 1000 300 500 # swipe russian language up
sleep 2
#Всё, русский язык включен
