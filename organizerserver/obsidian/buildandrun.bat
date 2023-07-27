@echo off

REM Build the Docker image with the tag "obsidian"
docker build -t obsidian .

REM Run the Docker container with the specified environment variable
docker run -d -p 8080:8080 --name obsidiancontainer -e TODO_LOCATION=/config/SyncedVault/Obsidian/Todo.md obsidian
