#!/usr/bin/env bash
set -e
mkdir -p out
javac -d out src/main/Main.java src/engine/*.java src/fighters/*.java src/ai/*.java src/ui/*.java src/util/*.java
java -cp out main.Main
