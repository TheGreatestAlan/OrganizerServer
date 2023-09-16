@echo off
SET SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%

cd ..
if not exist artifact mkdir artifact
docker build --rm . -t organizerserver

