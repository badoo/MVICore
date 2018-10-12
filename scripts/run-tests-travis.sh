#!/bin/bash

./gradlew --settings-file settings-travis.gradle :mvicore:test :mvicore-android:assembleDebug :mvicore-debugdrawer:assembleDebug :mvicore-demo:mvicore-demo-app:assembleDebug --info --stacktrace
