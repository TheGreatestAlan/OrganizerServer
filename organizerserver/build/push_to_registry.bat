@echo off

REM Set the image name and tag you want to push
SET IMAGE_NAME=organizerserver
SET IMAGE_TAG=latest

REM Set Docker Hub username and repository name
SET DOCKERHUB_USER=happydance
SET REPO_NAME=organizerserver

REM Check if DOCKERHUB_USER environment variable is set, if not, exit with error
IF "%DOCKERHUB_USER%"=="" (
    echo Error: DOCKERHUB_USER is not set. Exiting.
    exit /b 1
)

REM Check if REPO_NAME environment variable is set, if not, exit with error
IF "%REPO_NAME%"=="" (
    echo Error: REPO_NAME is not set. Exiting.
    exit /b 1
)

REM Tag the image for Docker Hub
docker tag %IMAGE_NAME%:%IMAGE_TAG% %DOCKERHUB_USER%/%REPO_NAME%:%IMAGE_TAG%

REM Push the image to Docker Hub
docker push %DOCKERHUB_USER%/%REPO_NAME%:%IMAGE_TAG%

REM Print a message to indicate the image has been pushed
echo Image %IMAGE_NAME%:%IMAGE_TAG% has been pushed to Docker Hub at %DOCKERHUB_USER%/%REPO_NAME%.
