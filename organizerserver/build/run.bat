@echo off

docker run -p 8080:8080 -e OBSIDIAN_VAULT_REPO_LOCATIONS="C:\Users\Alan\Documents\Obsidian Vault\Organizer" organizerserver
