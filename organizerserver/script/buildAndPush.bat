@echo off
setlocal enabledelayedexpansion

REM Define the relative path to the directory containing your .env files
set "ENV_DIR=..\dev"

REM Get the absolute path of the ENV_DIR
for %%i in ("%ENV_DIR%") do set ABS_ENV_DIR=%%~fi

REM Define the path to the .env file provided as the first argument
set ENV_FILE=%ABS_ENV_DIR%\docker-compose.env

REM Change to the directory where the docker-compose.yml is located (one level above)
cd ..

REM Load the specified .env file and set environment variables
for /f "usebackq delims=" %%i in ("%ENV_FILE%") do set %%i

@echo off

:: Set the path to your docker-compose.yml file
set DOCKER_COMPOSE_FILE=../docker-compose.yml

:: Get a list of all services defined in the docker-compose file and extract image names
for /f "tokens=*" %%i in ('docker-compose -f %DOCKER_COMPOSE_FILE% config | findstr "image"') do (
    set IMAGE_LINE=%%i

    REM Extract the image name
    for /f "tokens=2 delims=:" %%j in ("!IMAGE_LINE!") do (
        set IMAGE_NAME=%%j

        REM Get the local image hash (digest)
        for /f "tokens=*" %%k in ('docker inspect --format="{{index .RepoDigests 0}}" %IMAGE_NAME%') do (
            set LOCAL_DIGEST=%%k

            REM Get the remote image digest from Docker Hub using the Docker CLI
            for /f "tokens=*" %%l in ('docker manifest inspect %IMAGE_NAME% --verbose ^| findstr "Digest"') do (
                set REMOTE_DIGEST=%%l
            )

            REM Compare the local and remote digests
            if "!LOCAL_DIGEST!" neq "!REMOTE_DIGEST!" (
                echo Image !IMAGE_NAME! is different from Docker Hub version, updating...

                REM Remove the old local image
                docker rmi %IMAGE_NAME%

                REM Pull the new image from Docker Hub
                docker pull %IMAGE_NAME%
            ) else (
                echo Image !IMAGE_NAME! is up to date.
            )
        )
    )
)

:: Optionally clean up unused images
docker image prune -f

endlocal
