@echo off
REM Ensure Docker is running
docker --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Docker is not running or not installed. Please start Docker.
    exit /b 1
)

REM Define variables
set IMAGE_NAME=happydance/organizerserver
set TAG=latest

REM Change to the directory containing your Dockerfile (if needed)
REM Uncomment and modify the line below if your Dockerfile is in a different directory
REM cd path\to\your\dockerfile

REM Build the Docker image
echo Building the Docker image: %IMAGE_NAME%:%TAG%
cd ..
docker build -t %IMAGE_NAME%:%TAG% .

if %ERRORLEVEL% neq 0 (
    echo Failed to build the Docker image.
    exit /b 1
)

REM Push the Docker image to Docker Hub
echo Pushing the Docker image to Docker Hub...
docker push %IMAGE_NAME%:%TAG%

if %ERRORLEVEL% neq 0 (
    echo Failed to push the Docker image to Docker Hub.
    exit /b 1
)

echo Docker image successfully built and pushed to %IMAGE_NAME%:%TAG%
pause
