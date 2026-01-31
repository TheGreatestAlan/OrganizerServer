#!/bin/bash

set -euo pipefail

APP_NAME="organizer-server"
HOST_VAULT="${HOME}/workspace/obsidian/gitVault"
CONTAINER_VAULT="/data/obsidian/gitVault/Organizer"

PY_BIN="$(command -v python3 || command -v python || true)"
if [ -z "${PY_BIN}" ]; then
  echo "python or python3 is required to find free ports." >&2
  exit 1
fi

if [ ! -d "${HOST_VAULT}" ]; then
  echo "Host vault path does not exist: ${HOST_VAULT}" >&2
  exit 1
fi

read -r APP_PORT ADMIN_PORT MCP_HOST_PORT <<<"$(${PY_BIN} - <<'PY'
import socket

def free_port():
    s = socket.socket()
    s.bind(("", 0))
    port = s.getsockname()[1]
    s.close()
    return port

app = free_port()
admin = free_port()
while admin == app:
    admin = free_port()
mcp = free_port()
while mcp in (app, admin):
    mcp = free_port()
print(f"{app} {admin} {mcp}")
PY
)"

if docker ps -a --format '{{.Names}}' | grep -qx "${APP_NAME}"; then
  docker rm -f "${APP_NAME}" >/dev/null
fi

if [ -z "${MCP_API_KEY:-}" ]; then
  MCP_API_KEY="$(${PY_BIN} - <<'PY'
import secrets
print(secrets.token_hex(24))
PY
)"
fi

docker run -d \
  --name "${APP_NAME}" \
  -p "${APP_PORT}:8080" \
  -p "${ADMIN_PORT}:8081" \
  -p "${MCP_HOST_PORT}:3000" \
  -e OBSIDIAN_VAULT_REPO_LOCATION="${CONTAINER_VAULT}" \
  -e NOTE_BASE_LOCATION="/data/obsidian/gitVault" \
  -e GIT_SYNC_ENABLED=false \
  -e BASE_GIT_REPO_LOCATION= \
  -e GIT_USER= \
  -e GIT_TOKEN= \
  -e MCP_TRANSPORT=sse \
  -e MCP_PORT=3000 \
  -e MCP_API_KEY="${MCP_API_KEY}" \
  -e INVENTORY_API_BASE_URL="http://localhost:8080" \
  -v "${HOST_VAULT}:/data/obsidian/gitVault" \
  organizer-server

echo "Organizer Server running:"
echo "  App:   http://localhost:${APP_PORT}"
echo "  Admin: http://localhost:${ADMIN_PORT}"
echo "  MCP:   http://localhost:${MCP_HOST_PORT}/sse"
echo "  MCP_API_KEY: ${MCP_API_KEY}"
