#!/bin/sh
# Доверенный runner практики 4. Быстрые scoped-тесты, не весь build.
# Использование: sh scripts/check.sh
set -eu
cd "$(dirname "$0")/.."
./gradlew test --console=plain \
  --tests "ru.tusman4ik.taskimpl.t1.*" \
  --tests "ru.tusman4ik.controllers.TaskControllerTest" \
  --tests "ru.tusman4ik.task.nodes.MutatedTest"
