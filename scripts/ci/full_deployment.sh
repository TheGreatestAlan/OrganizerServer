#!/bin/bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd -- "${SCRIPT_DIR}/../.." && pwd)
VERSION_FILE="${ROOT_DIR}/VERSION"

BUMP_PART=${1:-patch}

if [[ "${SKIP_BUILD:-}" != "1" ]]; then
  "${SCRIPT_DIR}/build-and-push.sh" "${BUMP_PART}"
else
  echo "Skipping build and push"
fi

if [[ ! -f "${VERSION_FILE}" ]]; then
  echo "Missing VERSION file at ${VERSION_FILE}" >&2
  exit 1
fi

VERSION=$(cat "${VERSION_FILE}")

if [[ "${SKIP_DEPLOY:-}" != "1" ]]; then
  "${SCRIPT_DIR}/deploy_container.sh" "${VERSION}"
else
  echo "Skipping deploy"
fi

if [[ "${SKIP_HEALTHCHECK:-}" != "1" ]]; then
  REMOTE_HOST=${REMOTE_HOST:-root@digitalocean}
  REMOTE_DIR=${REMOTE_DIR:-/root/Orchestration/obsRemote}
  COMPOSE_FILE=${COMPOSE_FILE:-run_obsidian_remote.yml}
  SERVICE_NAME=${SERVICE_NAME:-organizerserver}
  ssh "${REMOTE_HOST}" <<EOF_REMOTE
set -euo pipefail
cd "${REMOTE_DIR}"
docker compose -f "${COMPOSE_FILE}" exec -T "${SERVICE_NAME}" curl -fsS http://localhost:8081/healthcheck
EOF_REMOTE
  echo "Healthcheck OK"
else
  echo "Skipping healthcheck"
fi
