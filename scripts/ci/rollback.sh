#!/bin/bash
set -euo pipefail

# Rollback OrganizerServer to previous version
# Usage: ./scripts/ci/rollback.sh [version]

echo "======================================================"
echo "  OrganizerServer Rollback"
echo "======================================================"
echo ""

# Configuration
REMOTE_HOST=${REMOTE_HOST:-root@digitalocean}
REMOTE_DIR=${REMOTE_DIR:-/root/Orchestration/obsRemote}
COMPOSE_FILE=${COMPOSE_FILE:-run_obsidian_remote.yml}
SERVICE_NAME=${SERVICE_NAME:-organizerserver}
DATA_DIR=${DATA_DIR:-/data/organizer}

# Get previous version from argument or user input
if [ $# -eq 1 ]; then
    PREVIOUS_VERSION="$1"
    echo "Rolling back to version: $PREVIOUS_VERSION"
else
    # List available backups/images
    echo "Recent versions on production:"
    ssh "$REMOTE_HOST" "
        docker images registry.alanhoangnguyen.com/admin/organizerserver --format '{{.Tag}}' | head -10
    " 2>/dev/null || echo "Could not retrieve versions"
    echo ""
    read -p "Enter version to rollback to: " PREVIOUS_VERSION
fi

if [ -z "$PREVIOUS_VERSION" ]; then
    echo "Version required"
    exit 1
fi

echo ""
echo "Rollback Configuration:"
echo "------------------------------------------------------"
echo "Version: $PREVIOUS_VERSION"
echo "Server: $REMOTE_HOST"
echo "Service: $SERVICE_NAME"
echo ""

# Confirm
read -p "Continue with rollback? [y/N] " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Rollback cancelled"
    exit 0
fi

# Perform rollback
echo ""
echo "Executing rollback..."
echo "------------------------------------------------------"

ssh "$REMOTE_HOST" <<EOF_REMOTE
set -euo pipefail
cd "${REMOTE_DIR}"

echo "Stopping current container..."
docker compose -f "${COMPOSE_FILE}" stop "${SERVICE_NAME}" || true

echo "Starting previous version: ${PREVIOUS_VERSION}..."
ORGANIZER_VERSION="${PREVIOUS_VERSION}" docker compose -f "${COMPOSE_FILE}" up -d --no-deps --force-recreate "${SERVICE_NAME}"

echo "Rollback complete"
EOF_REMOTE

# Health check
echo ""
echo "Health check..."
sleep 5

for i in {1..5}; do
    if ssh "$REMOTE_HOST" "docker compose -f ${REMOTE_DIR}/${COMPOSE_FILE} exec -T ${SERVICE_NAME} curl -fsS http://localhost:8081/healthcheck" 2>/dev/null; then
        echo "Service healthy"
        break
    fi
    echo "Waiting... ($i/5)"
    sleep 3
done

echo ""
echo "======================================================"
echo "Rollback Complete!"
echo "======================================================"
echo ""
echo "Service rolled back to: $PREVIOUS_VERSION"
echo ""
echo "Monitor:"
echo "  ssh $REMOTE_HOST 'docker compose -f $REMOTE_DIR/$COMPOSE_FILE logs -f $SERVICE_NAME'"
echo ""
