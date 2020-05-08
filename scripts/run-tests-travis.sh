#!/usr/bin/env bash

./gradlew \
  clean \
  :mvicore-common:build \
  :mvicore-rx:test \
  :mvicore-android:assembleDebug \
  :mvicore-debugdrawer:assembleDebug \
  :mvicore-demo:mvicore-demo-app:assembleDebug \
  --console=plain \
  --stacktrace
