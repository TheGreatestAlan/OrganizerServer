#!/bin/bash
set -euo pipefail

VERSION=${1:-latest}
REMOTE_HOST=${REMOTE_HOST:-root@digitalocean}
REMOTE_DIR=${REMOTE_DIR:-/root/Orchestration/obsRemote}
COMPOSE_FILE=${COMPOSE_FILE:-run_obsidian_remote.yml}
SERVICE_NAME=${SERVICE_NAME:-organizerserver}

ssh "${REMOTE_HOST}" <<EOF_REMOTE
set -euo pipefail
cd "${REMOTE_DIR}"
ORGANIZER_VERSION="${VERSION}" docker compose -f "${COMPOSE_FILE}" pull "${SERVICE_NAME}"
ORGANIZER_VERSION="${VERSION}" docker compose -f "${COMPOSE_FILE}" up -d --no-deps --force-recreate "${SERVICE_NAME}"
EOF_REMOTE

echo "Deploy triggered for ${SERVICE_NAME}:${VERSION}"
