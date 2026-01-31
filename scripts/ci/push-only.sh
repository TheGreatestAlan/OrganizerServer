#!/bin/bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd -- "${SCRIPT_DIR}/../.." && pwd)
VERSION_FILE="${ROOT_DIR}/VERSION"
IMAGE="registry.alanhoangnguyen.com/admin/organizerserver"

if [[ ! -f "${VERSION_FILE}" ]]; then
  echo "Missing VERSION file at ${VERSION_FILE}" >&2
  exit 1
fi

VERSION=$(cat "${VERSION_FILE}")
COMMIT_HASH=$(git -C "${ROOT_DIR}" rev-parse --short HEAD 2>/dev/null || echo "unknown")
BUILD_DATE=$(date -u +%Y%m%d%H%M%S)

# Ensure latest image exists locally
if ! docker image inspect "${IMAGE}:latest" >/dev/null 2>&1; then
  echo "Local image ${IMAGE}:latest not found. Build first." >&2
  exit 1
fi

docker tag "${IMAGE}:latest" "${IMAGE}:${VERSION}"
if [[ "${COMMIT_HASH}" != "unknown" ]]; then
  docker tag "${IMAGE}:latest" "${IMAGE}:${COMMIT_HASH}"
fi
docker tag "${IMAGE}:latest" "${IMAGE}:${BUILD_DATE}"

docker push "${IMAGE}:latest"
docker push "${IMAGE}:${VERSION}"
if [[ "${COMMIT_HASH}" != "unknown" ]]; then
  docker push "${IMAGE}:${COMMIT_HASH}"
fi
docker push "${IMAGE}:${BUILD_DATE}"

echo "Push complete: ${VERSION}"
