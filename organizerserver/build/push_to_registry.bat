@echo off

REM Set the image name and tag you want to push
SET IMAGE_NAME=organizerserver:latest

REM Check if LOCAL_REGISTRY environment variable is set, if not, default to localhost:5000
IF "%LOCAL_REGISTRY%"=="" SET LOCAL_REGISTRY=localhost:5000

REM Tag the image for the local registry
docker tag %IMAGE_NAME% %LOCAL_REGISTRY%/%IMAGE_NAME%

REM Push the image to the local registry
docker push %LOCAL_REGISTRY%/%IMAGE_NAME%

REM Print a message to indicate the image has been pushed
echo Image %IMAGE_NAME% has been pushed to the local registry at %LOCAL_REGISTRY%.
