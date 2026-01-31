#!/bin/bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd -- "${SCRIPT_DIR}/../.." && pwd)
VERSION_FILE="${ROOT_DIR}/VERSION"
IMAGE="registry.alanhoangnguyen.com/admin/organizerserver"

bump_version() {
  local current=$1
  local part=$2
  local major minor patch
  IFS='.' read -r major minor patch <<< "${current}"
  case "${part}" in
    major)
      major=$((major + 1))
      minor=0
      patch=0
      ;;
    minor)
      minor=$((minor + 1))
      patch=0
      ;;
    patch)
      patch=$((patch + 1))
      ;;
    *)
      echo "Unknown bump part: ${part}" >&2
      exit 1
      ;;
  esac
  echo "${major}.${minor}.${patch}"
}

if [[ ! -f "${VERSION_FILE}" ]]; then
  echo "Missing VERSION file at ${VERSION_FILE}" >&2
  exit 1
fi

CURRENT_VERSION=$(cat "${VERSION_FILE}")
ACTION=${1:-patch}

if [[ "${ACTION}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  NEW_VERSION="${ACTION}"
else
  NEW_VERSION=$(bump_version "${CURRENT_VERSION}" "${ACTION}")
fi

echo "${NEW_VERSION}" > "${VERSION_FILE}"

if [[ "${SKIP_GIT_TAG:-}" != "1" ]]; then
  git -C "${ROOT_DIR}" tag "v${NEW_VERSION}"
fi

COMMIT_HASH=$(git -C "${ROOT_DIR}" rev-parse --short HEAD 2>/dev/null || echo "unknown")
BUILD_DATE=$(date -u +%Y%m%d%H%M%S)

cd "${ROOT_DIR}"

docker build \
  -t "${IMAGE}:latest" \
  -t "${IMAGE}:${NEW_VERSION}" \
  -t "${IMAGE}:${COMMIT_HASH}" \
  -t "${IMAGE}:${BUILD_DATE}" \
  .

docker push "${IMAGE}:latest"
docker push "${IMAGE}:${NEW_VERSION}"
if [[ "${COMMIT_HASH}" != "unknown" ]]; then
  docker push "${IMAGE}:${COMMIT_HASH}"
fi
docker push "${IMAGE}:${BUILD_DATE}"

echo "Build and push complete: ${NEW_VERSION}"
