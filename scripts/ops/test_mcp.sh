#!/bin/bash
set -euo pipefail

API_KEY_FILE=${1:-}
BASE_URL=${2:-https://alanhoangnguyen.com/mcp}

if [[ -z "${API_KEY_FILE}" ]]; then
  echo "Usage: $0 <api_key_file> [base_url]" >&2
  exit 1
fi

if [[ ! -f "${API_KEY_FILE}" ]]; then
  echo "API key file not found: ${API_KEY_FILE}" >&2
  exit 1
fi

API_KEY=$(head -n 1 "${API_KEY_FILE}" | tr -d '\r\n')
if [[ -z "${API_KEY}" ]]; then
  echo "API key file is empty: ${API_KEY_FILE}" >&2
  exit 1
fi

sse_url="${BASE_URL%/}/sse"
meta_url="${BASE_URL%/}/../.well-known/oauth-protected-resource"

printf "\n== MCP SSE (no auth) ==\n"
http_code=$(curl -s -o /dev/null -w "%{http_code}" "${sse_url}")
printf "HTTP %s\n" "${http_code}"

printf "\n== MCP SSE (API key) ==\n"
# Use --max-time to avoid hanging on SSE stream
http_code=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${API_KEY}" --max-time 5 "${sse_url}")
printf "HTTP %s (expected 200)\n" "${http_code}"

if [[ -n "${OAUTH_TOKEN_FILE:-}" && -f "${OAUTH_TOKEN_FILE}" ]]; then
  OAUTH_TOKEN=$(head -n 1 "${OAUTH_TOKEN_FILE}" | tr -d '\r\n')
  if [[ -n "${OAUTH_TOKEN}" ]]; then
    printf "\n== MCP SSE (OAuth token) ==\n"
    http_code=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${OAUTH_TOKEN}" --max-time 5 "${sse_url}")
    printf "HTTP %s (expected 200)\n" "${http_code}"
  fi
fi

printf "\n== MCP Protected Resource Metadata ==\n"
if command -v jq >/dev/null 2>&1; then
  curl -s "${meta_url}" | jq
else
  curl -s "${meta_url}"
fi

printf "\nDone.\n"
