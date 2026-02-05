#!/bin/bash
# Full deployment orchestrator for OrganizerServer
# Orchestrates: build > push > backup > migrate (if needed) > deploy > verify

set -euo pipefail

echo "======================================================"
echo "  OrganizerServer Full Deployment Orchestrator"
echo "======================================================"
echo ""

# Configuration
SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd -- "${SCRIPT_DIR}/../.." && pwd)
VERSION_FILE="${ROOT_DIR}/VERSION"
DEPLOY_LOG="${ROOT_DIR}/deployments.log"

# Deployment options
SKIP_BUILD="${SKIP_BUILD:-false}"
SKIP_PUSH="${SKIP_PUSH:-false}"
SKIP_BACKUP="${SKIP_BACKUP:-false}"
SKIP_MIGRATION="${SKIP_MIGRATION:-false}"
SKIP_DEPLOY="${SKIP_DEPLOY:-false}"
SKIP_VERIFY="${SKIP_VERIFY:-false}"

# Production server configuration
PROD_SERVER="${PROD_SERVER:-root@digitalocean}"
PROD_DEPLOY_DIR="${PROD_DEPLOY_DIR:-/root/Orchestration/obsRemote}"
PROD_COMPOSE_FILE="${PROD_COMPOSE_FILE:-run_obsidian_remote.yml}"
PROD_DATA_DIR="${PROD_DATA_DIR:-/data/organizer}"
SERVICE_NAME="${SERVICE_NAME:-organizerserver}"
HEALTH_URL="${HEALTH_URL:-http://localhost:8080/inventory}"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD="true"
            shift
            ;;
        --skip-push)
            SKIP_PUSH="true"
            shift
            ;;
        --skip-backup)
            SKIP_BACKUP="true"
            shift
            ;;
        --skip-migration)
            SKIP_MIGRATION="true"
            shift
            ;;
        --skip-deploy)
            SKIP_DEPLOY="true"
            shift
            ;;
        --skip-verify)
            SKIP_VERIFY="true"
            shift
            ;;
        --help)
            echo "Usage: $0 [options] [patch|minor|major|<version>]"
            echo ""
            echo "Options:"
            echo "  --skip-build      Skip the build step"
            echo "  --skip-push       Skip the push to registry step"
            echo "  --skip-backup     Skip pre-deployment backup"
            echo "  --skip-migration  Skip data migration check"
            echo "  --skip-deploy     Skip the deployment step"
            echo "  --skip-verify     Skip post-deployment verification"
            echo "  --help            Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                    # Full deployment with patch bump"
            echo "  $0 minor              # Full deployment with minor bump"
            echo "  $0 1.2.3              # Deploy specific version"
            echo "  $0 --skip-build       # Push and deploy only"
            echo "  $0 --skip-deploy      # Build and push only"
            exit 0
            ;;
        *)
            BUMP_PART="$1"
            shift
            ;;
    esac
done

BUMP_PART="${BUMP_PART:-patch}"

# Get version info
if [ -f "$VERSION_FILE" ]; then
    VERSION=$(cat "$VERSION_FILE" | tr -d '\n\r ')
else
    VERSION="1.0.0"
    echo "$VERSION" > "$VERSION_FILE"
fi

COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

echo "Deployment Configuration:"
echo "------------------------------------------------------"
echo "Version: $VERSION"
echo "Commit: $COMMIT"
echo "Bump: $BUMP_PART"
echo "Skip Build: $SKIP_BUILD"
echo "Skip Push: $SKIP_PUSH"
echo "Skip Backup: $SKIP_BACKUP"
echo "Skip Migration: $SKIP_MIGRATION"
echo "Skip Deploy: $SKIP_DEPLOY"
echo "Skip Verify: $SKIP_VERIFY"
echo ""

# Step 1: Build Container
echo "[1/6] Container Build Phase"
echo "------------------------------------------------------"
if [ "$SKIP_BUILD" = "true" ]; then
    echo "Skipping build phase (--skip-build)"
    echo ""
else
    cd "$SCRIPT_DIR"
    ./build-and-push.sh "$BUMP_PART"
    if [ $? -ne 0 ]; then
        echo "Build phase failed"
        exit 1
    fi
    echo "Build phase completed"
    echo ""
fi

# Re-read version (may have been bumped)
VERSION=$(cat "$VERSION_FILE" | tr -d '\n\r ')

# Step 2: Push to Registry
echo "[2/6] Registry Push Phase"
echo "------------------------------------------------------"
if [ "$SKIP_PUSH" = "true" ]; then
    echo "Skipping push phase (--skip-push)"
    echo ""
else
    if [ "$SKIP_BUILD" = "false" ]; then
        echo "Push phase completed (included in build-and-push.sh)"
        echo ""
    else
        cd "$SCRIPT_DIR"
        ./push-only.sh
        if [ $? -ne 0 ]; then
            echo "Push phase failed"
            exit 1
        fi
        echo "Push phase completed"
        echo ""
    fi
fi

# Step 3: Pre-Deployment Backup
echo "[3/6] Pre-Deployment Backup"
echo "------------------------------------------------------"
if [ "$SKIP_BACKUP" = "true" ]; then
    echo "Skipping backup (--skip-backup)"
    echo ""
else
    BACKUP_TIMESTAMP=$(date +%Y%m%d-%H%M%S)
    BACKUP_NAME="pre-deploy-${VERSION}-${BACKUP_TIMESTAMP}"

    echo "Creating backup: $BACKUP_NAME"

    # Capture current version on production
    PREVIOUS_VERSION=$(ssh "$PROD_SERVER" "docker compose -f ${PROD_DEPLOY_DIR}/${PROD_COMPOSE_FILE} exec -T ${SERVICE_NAME} curl -fsS http://localhost:8081/healthcheck 2>/dev/null | grep -o 'version[^,]*' || echo 'unknown'" 2>/dev/null || echo "unknown")

    echo "Current production version: $PREVIOUS_VERSION"

    # Backup H2 database if it exists
    ssh "$PROD_SERVER" "
        if [ -f ${PROD_DATA_DIR}/db/inventory.db.mv.db ]; then
            mkdir -p ${PROD_DATA_DIR}/backups
            cp ${PROD_DATA_DIR}/db/inventory.db.mv.db ${PROD_DATA_DIR}/backups/inventory-${BACKUP_NAME}.mv.db
            echo 'Database backed up'
        else
            echo 'No existing database to backup'
        fi
    " || echo "Backup warning (non-fatal)"

    echo "Backup phase completed"
    echo ""
fi

# Step 4: Data Migration (if needed)
echo "[4/6] Data Migration Check"
echo "------------------------------------------------------"
if [ "$SKIP_MIGRATION" = "true" ]; then
    echo "Skipping migration check (--skip-migration)"
    echo ""
else
    # Check if legacy data exists and H2 database doesn't
    MIGRATION_NEEDED=$(ssh "$PROD_SERVER" "
        if [ -f /data/legacy/ItemLocation.md ] && [ ! -f ${PROD_DATA_DIR}/db/inventory.db.mv.db ]; then
            echo 'MIGRATION_NEEDED'
        else
            echo 'NO_MIGRATION'
        fi
    " 2>/dev/null || echo "NO_MIGRATION")

    if [ "$MIGRATION_NEEDED" = "MIGRATION_NEEDED" ]; then
        echo "Legacy data found. Migration required!"
        echo ""
        read -p "Run data migration from file-based to H2? [y/N] " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Running migration..."
            ssh "$PROD_SERVER" "
                cd ${PROD_DEPLOY_DIR}
                mkdir -p ${PROD_DATA_DIR}/db

                # Run migration using the new container
                docker run --rm \
                    -v /data/legacy:/legacy:ro \
                    -v ${PROD_DATA_DIR}/db:/data \
                    registry.alanhoangnguyen.com/admin/organizerserver:${VERSION} \
                    java -cp /usr/local/lib/organizer.jar:/usr/local/lib/* \
                    com.nguyen.server.migration.FileToH2Migration \
                    /legacy/ItemLocation.md \
                    jdbc:h2:file:/data/inventory.db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
            "
            echo "Migration completed"
        else
            echo "Migration skipped - will start with empty database"
        fi
    else
        echo "No migration needed"
    fi
    echo ""
fi

# Step 5: Deploy to Production
echo "[5/6] Production Deployment Phase"
echo "------------------------------------------------------"
if [ "$SKIP_DEPLOY" = "true" ]; then
    echo "Skipping deployment phase (--skip-deploy)"
    echo ""
else
    cd "$SCRIPT_DIR"
    ./deploy_container.sh "$VERSION"
    if [ $? -ne 0 ]; then
        echo "Deployment phase failed"
        exit 1
    fi
    echo "Deployment phase completed"
    echo ""
fi

# Step 6: Post-Deployment Verification
echo "[6/6] Post-Deployment Verification"
echo "------------------------------------------------------"
if [ "$SKIP_VERIFY" = "true" ]; then
    echo "Skipping verification (--skip-verify)"
    echo ""
else
    echo "Waiting for service to start..."
    sleep 5

    MAX_RETRIES=10
    RETRY_DELAY=3
    VERIFIED=false

    for i in $(seq 1 $MAX_RETRIES); do
        RESPONSE=$(ssh "$PROD_SERVER" "docker compose -f ${PROD_DEPLOY_DIR}/${PROD_COMPOSE_FILE} exec -T ${SERVICE_NAME} curl -fsS http://localhost:8080/inventory" 2>/dev/null || echo "")

        if [ -n "$RESPONSE" ]; then
            echo "Service responding on attempt $i"
            VERIFIED=true
            break
        fi

        echo "Verification attempt $i/$MAX_RETRIES failed, retrying in ${RETRY_DELAY}s..."
        sleep $RETRY_DELAY
    done

    if [ "$VERIFIED" = "false" ]; then
        echo "Verification failed after $MAX_RETRIES attempts"
        echo ""
        echo "Rollback command:"
        echo "  ssh $PROD_SERVER 'cd $PROD_DEPLOY_DIR && ORGANIZER_VERSION=$PREVIOUS_VERSION docker compose -f $PROD_COMPOSE_FILE up -d --no-deps --force-recreate $SERVICE_NAME'"
        exit 1
    fi

    echo "Verification passed"
    echo ""
fi

# Final Summary
TIMESTAMP=$(date -Iseconds)
DEPLOYER=$(git config user.name 2>/dev/null || echo "unknown")

echo "$TIMESTAMP | v$VERSION | $COMMIT | from:$PREVIOUS_VERSION | $DEPLOYER" >> "$DEPLOY_LOG"

echo "======================================================"
echo "Full Deployment Orchestration Complete!"
echo "======================================================"
echo ""
echo "Deployment Summary:"
echo "  Version: v$VERSION ($COMMIT)"
echo "  Previous: $PREVIOUS_VERSION"
echo "  Build:   $([ "$SKIP_BUILD" = "true" ] && echo "Skipped" || echo "Completed")"
echo "  Push:    $([ "$SKIP_PUSH" = "true" ] && echo "Skipped" || echo "Completed")"
echo "  Backup:  $([ "$SKIP_BACKUP" = "true" ] && echo "Skipped" || echo "Completed")"
echo "  Deploy:  $([ "$SKIP_DEPLOY" = "true" ] && echo "Skipped" || echo "Completed")"
echo "  Verify:  $([ "$SKIP_VERIFY" = "true" ] && echo "Skipped" || echo "Passed")"
echo ""
echo "Production Environment:"
echo "  - Service: $SERVICE_NAME"
echo "  - Version: $VERSION"
echo "  - Data Dir: $PROD_DATA_DIR"
echo ""
echo "Individual Scripts:"
echo "  - Build & Push:  ./scripts/ci/build-and-push.sh"
echo "  - Push only:     ./scripts/ci/push-only.sh"
echo "  - Deploy only:   ./scripts/ci/deploy_container.sh"
echo "  - Full deploy:   ./scripts/ci/full_deployment.sh"
echo ""
echo "Rollback (if needed):"
echo "  ORGANIZER_VERSION=$PREVIOUS_VERSION ./scripts/ci/deploy_container.sh"
echo ""
echo "Monitor production:"
echo "  ssh $PROD_SERVER 'docker logs -f \$(docker ps -q -f name=$SERVICE_NAME)'"
echo ""
