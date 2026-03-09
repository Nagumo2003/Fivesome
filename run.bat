@echo off
if not exist out mkdir out
javac -d out src\main\Main.java src\engine\*.java src\fighters\*.java src\ai\*.java src\ui\*.java src\util\*.java
if %errorlevel% neq 0 pause & exit /b %errorlevel%
java -cp out main.Main
pause
