#! /bin/bash

set -x
set -e
set -o pipefail

SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
PROJECT_PATH=$SCRIPT_PATH/..

PKG_PATH=$PROJECT_PATH/gradle/pkg
mkdir -p $PKG_PATH
GRADLE_ALL_PACKAGE=$PKG_PATH/gradle-6.1.1-all.zip
if [[ -f "$GRADLE_ALL_PACKAGE" ]]; then
    echo "$GRADLE_ALL_PACKAGE already exists"
else
    rm -f $GRADLE_ALL_PACKAGE
    wget -O $GRADLE_ALL_PACKAGE https://services.gradle.org/distributions/gradle-6.1.1-all.zip
fi
