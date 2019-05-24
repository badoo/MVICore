#!/usr/bin/env bash

./gradlew :mvicore:test :mvicore-android:assembleDebug :mvicore-debugdrawer:assembleDebug :mvicore-demo:mvicore-demo-app:assembleDebug --info --stacktrace
