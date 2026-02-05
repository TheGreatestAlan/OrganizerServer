#!/bin/bash
set -euo pipefail

# Deploy OrganizerServer to production
# Usage: ./scripts/ci/deploy_container.sh [version]

echo "======================================================"
echo "  OrganizerServer Container Deployment"
echo "======================================================"
echo ""

# Configuration
VERSION=${1:-latest}
REMOTE_HOST=${REMOTE_HOST:-root@digitalocean}
REMOTE_DIR=${REMOTE_DIR:-/root/Orchestration/obsRemote}
COMPOSE_FILE=${COMPOSE_FILE:-run_obsidian_remote.yml}
SERVICE_NAME=${SERVICE_NAME:-organizerserver}
DATA_DIR=${DATA_DIR:-/data/organizer}
HEALTH_RETRIES=${HEALTH_RETRIES:-10}
HEALTH_DELAY=${HEALTH_DELAY:-3}

echo "Deployment Configuration:"
echo "------------------------------------------------------"
echo "Version: $VERSION"
echo "Server: $REMOTE_HOST"
echo "Deploy Dir: $REMOTE_DIR"
echo "Service: $SERVICE_NAME"
echo "Data Dir: $DATA_DIR"
echo ""

# Step 1: Deploy

echo "[1/2] Deploying to production..."
echo "------------------------------------------------------"

ssh "${REMOTE_HOST}" <<EOF_REMOTE
set -euo pipefail
cd "${REMOTE_DIR}"

# Ensure data directory exists with proper permissions
mkdir -p "${DATA_DIR}/db"

# Update environment file to set DATABASE_URL for H2
docker run --rm \
    --entrypoint "" \
    alpine:latest \
    sh -c "echo 'DATABASE_URL=jdbc:h2:file:/data/inventory.db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1' >> /data/.env" 2>/dev/null || true

# Pull and deploy
echo "Pulling image version ${VERSION}..."
ORGANIZER_VERSION="${VERSION}" docker compose -f "${COMPOSE_FILE}" pull "${SERVICE_NAME}"

echo "Starting container..."
ORGANIZER_VERSION="${VERSION}" docker compose -f "${COMPOSE_FILE}" up -d --no-deps --force-recreate "${SERVICE_NAME}"

echo "Container started"
EOF_REMOTE

if [ $? -ne 0 ]; then
    echo "Deployment failed"
    exit 1
fi

echo "Deployment command completed"
echo ""

# Step 2: Health Check

echo "[2/2] Health Check"
echo "------------------------------------------------------"
echo "Waiting for service to be ready..."

VERIFIED=false
for i in $(seq 1 $HEALTH_RETRIES); do
    sleep $HEALTH_DELAY

    HEALTH_STATUS=$(ssh "$REMOTE_HOST" "
        docker compose -f ${REMOTE_DIR}/${COMPOSE_FILE} exec -T ${SERVICE_NAME} \
        curl -fsS http://localhost:8081/healthcheck 2>/dev/null || echo 'unhealthy'
    " 2>/dev/null || echo "unhealthy")

    if [ "$HEALTH_STATUS" != "unhealthy" ]; then
        echo "Health check passed on attempt $i"
        VERIFIED=true
        break
    fi

    echo "Attempt $i/$HEALTH_RETRIES: service not ready yet"
done

if [ "$VERIFIED" = "false" ]; then
    echo ""
    echo "Health check failed after $HEALTH_RETRIES attempts"
    echo ""
    echo "Check logs:"
    echo "  ssh $REMOTE_HOST 'docker compose -f $REMOTE_DIR/$COMPOSE_FILE logs $SERVICE_NAME'"
    exit 1
fi

echo ""
echo "======================================================"
echo "Deployment Complete!"
echo "======================================================"
echo ""
echo "Service: $SERVICE_NAME"
echo "Version: $VERSION"
echo "Data: $DATA_DIR"
echo ""
echo "Monitor logs:"
echo "  ssh $REMOTE_HOST 'docker compose -f $REMOTE_DIR/$COMPOSE_FILE logs -f $SERVICE_NAME'"
echo ""
