#!/usr/bin/env bash

./gradlew \
  clean \
  :mvicore-common:jvmTest \
  :mvicore-rx:test \
  :mvicore-android:assembleDebug \
  :mvicore-debugdrawer:assembleDebug \
  :mvicore-demo:mvicore-demo-app:assembleDebug \
  --console=plain \
  --stacktrace
