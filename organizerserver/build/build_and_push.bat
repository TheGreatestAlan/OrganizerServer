@echo off

REM Change to the directory where the script is located
cd %~dp0

REM Call the build script
call build.bat

cd %~dp0
REM Call the push script
call push_to_registry.bat

REM Print a message to indicate the tasks have been completed
echo Image has been built and pushed successfully.
