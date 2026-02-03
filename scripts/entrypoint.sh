#!/bin/bash
set -euo pipefail

JAVA_CMD=("java")
if [[ -n "${JAVA_OPTS:-}" ]]; then
  read -r -a JAVA_OPTS_ARR <<< "${JAVA_OPTS}"
  JAVA_CMD+=("${JAVA_OPTS_ARR[@]}")
fi
JAVA_CMD+=("-jar" "/usr/local/lib/organizer.jar" "server" "/usr/local/lib/configuration.yml")

export PYTHONPATH="/usr/local/lib/mcp_server"

log() {
  echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] $*"
}

shutdown() {
  log "Stopping services..."
  if [[ -n "${JAVA_PID:-}" ]]; then
    kill "${JAVA_PID}" 2>/dev/null || true
  fi
  if [[ -n "${MCP_PID:-}" ]]; then
    kill "${MCP_PID}" 2>/dev/null || true
  fi
}

trap shutdown TERM INT

log "Starting OrganizerServer (Java)..."
"${JAVA_CMD[@]}" &
JAVA_PID=$!

log "Starting MCP server..."
python3 -m mcp_inventory.server --transport "${MCP_TRANSPORT:-streamable-http}" --port "${MCP_PORT:-3000}" --host "${MCP_HOST:-0.0.0.0}" &
MCP_PID=$!

log "OrganizerServer PID: ${JAVA_PID}"
log "MCP server PID: ${MCP_PID}"

# Wait for the first process to exit
set +e
wait -n "${JAVA_PID}" "${MCP_PID}"
EXIT_CODE=$?
set -e

log "A process exited (code ${EXIT_CODE}). Stopping the other..."
shutdown

wait "${JAVA_PID}" 2>/dev/null || true
wait "${MCP_PID}" 2>/dev/null || true

exit "${EXIT_CODE}"
