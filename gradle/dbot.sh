#!/usr/bin/env bash

[ ! -d build ] && mkdir build

# clean the dirs
rm -rf build/binaries
mkdir build/binaries
mkdir build/binaries/natives

# variables
BUILD_DIR="build/binaries/natives"
C_SRC="src/main/c"
D_SRC="src/main/d"

build_type=$(uname -m)
if [ "$build_type" == "x86_64" ]; then
    build_type=64
else
    build_type=32
fi

# functions
clean() {
    rm -f build/binaries/natives/*.o
    rm -f build/binaries/natives/*.a
}

update_modules() {
    git submodule init
    git submodule update
}

exit_on_bad_status() {
    local status=$?
    if [ $status -ne 0 ]; then
        clean
        exit $status
    fi
}

# Check for directory
[ ! -d depends/drirc/ ] && update_modules

# DMD build
dmd -m"$build_type" -defaultlib=phobos2 -fPIC -lib $(find $D_SRC -type f -name '*.d') $(find 'depends/drirc/src' -type f -name '*.d') -of"$BUILD_DIR"/libbot.a
exit_on_bad_status

# GCC build
JAVA_DIR="${1}/include"
g++ -Wl,-rpath='$ORIGIN' -m"$build_type" -fPIC -shared -I"$JAVA_DIR" -I"$JAVA_DIR"/linux -lphobos2 -Wl,-whole-archive "$BUILD_DIR"/libbot.a -Wl,-no-whole-archive "$C_SRC"/wrapper.cpp -o "$BUILD_DIR"/libbot.so
exit_on_bad_status

# Cleanup
clean
