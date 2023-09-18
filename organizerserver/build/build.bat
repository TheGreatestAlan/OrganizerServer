@echo off
SET SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%

cd ..
if not exist artifact mkdir artifact
docker buildx build --platform linux/arm64 --rm . -t organizerserver

