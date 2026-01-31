# MCP + OrganizerServer Production Plan (Single Container, API Key Auth)

Date: 2026-01-31
Owner: OrganizerServer repo
Status: Implemented (code added)

## Purpose
Provide a complete plan to ship the MCP server alongside OrganizerServer in a single container, expose MCP to the internet with API key auth, and deploy via the existing production docker compose flow on the DigitalOcean droplet.

This file is intended to be the single source of truth for implementation after context reset.

---

## Confirmed Inputs
- Registry: same as flofluent, `registry.alanhoangnguyen.com`.
- Image name: `registry.alanhoangnguyen.com/admin/organizerserver`.
- Prod host: same DigitalOcean droplet used by flofluent (`root@digitalocean`).
- Compose file on prod: `/root/Orchestration/obsRemote/run_obsidian_remote.yml`.
- Service name in compose: `organizerserver`.
- Nginx routing is managed by a separate agent on prod (do not edit nginx here).
- MCP endpoint will be internet-exposed; no IP allowlist.
- Auth method: API key using `Authorization: Bearer <API_KEY>`.
- OrganizerServer-to-MCP internal auth is not required.
- Deployment should restart the service in the existing compose file (no new deploy directory).
- Container should fail fast: if either Java or MCP dies, the container exits so Docker restarts it.

---

## Current Production Compose (Key Facts)
From `/root/Orchestration/obsRemote/run_obsidian_remote.yml`:
- `organizerserver` currently uses `happydance/organizerserver:latest`.
- `organizerserver` has **no public ports** published.
- `agent-server` already uses `ORGANIZER_SERVER_URL=http://organizerserver:8080`.
- Other services use registry `registry.alanhoangnguyen.com/admin/*`.

---

## Architecture Plan

### Runtime Topology
- One container runs **two processes**:
  - Java OrganizerServer
  - Python MCP server (SSE transport)
- No direct public ports from `organizerserver` container.
- Nginx handles public routing to MCP externally (managed elsewhere).

### Internal Ports
- Java app: 8080 (app) and 8081 (admin/healthcheck) as today.
- MCP server: 3000 (SSE).

### MCP -> Java URL (same container)
- Use `INVENTORY_API_BASE_URL=http://localhost:8080` inside the container.

---

## Authentication Plan

### MCP Access (Public)
- Require API key on every request:
  - Header: `Authorization: Bearer <API_KEY>`
- Enforced **in MCP server** regardless of nginx.
- Nginx also expected to enforce the same header (managed separately).

### OrganizerServer Access (Internal)
- No additional auth required between MCP and Java (internal only).

---

## Process Management (Fail-Fast)

### Requirement
If either Java or MCP exits, the container should exit so Docker restarts it.

### Chosen Approach
**Fail-fast bash entrypoint**:
- Start Java
- Start MCP
- Monitor both PIDs
- If one exits, kill the other and exit non-zero

This aligns with “restart at container level” and avoids heavy process managers.

---

## Container Build Plan

### Docker Image
- Extend current Dockerfile to include MCP server code and Python dependencies.
- Add an entrypoint script that starts both services and enforces fail-fast behavior.

### Tagging Strategy (mirror flofluent)
Use tags:
- `latest`
- semantic version from `VERSION`
- commit hash
- build date

---

## Production Compose Changes (Existing File)
Update `organizerserver` service in `run_obsidian_remote.yml`:

1) **Image**
- Change to `registry.alanhoangnguyen.com/admin/organizerserver:${ORGANIZER_VERSION:-latest}`

2) **Environment**
Add MCP env vars:
- `MCP_TRANSPORT=sse`
- `MCP_PORT=3000`
- `MCP_API_KEY=$MCP_API_KEY`
- `INVENTORY_API_BASE_URL=http://localhost:8080`

3) **Restart policy**
- `restart: unless-stopped`

4) **Optional Healthcheck**
- `curl http://localhost:8081/healthcheck` inside container

No port exposure changes needed; MCP remains behind nginx routing.

---

## Deployment Script Plan (Mirror flofluent)
Create `scripts/ci/` in this repo with:

1) `build-and-push.sh`
- Bump `VERSION`
- Tag git
- Build Docker image
- Tag and push to `registry.alanhoangnguyen.com/admin/organizerserver`

2) `push-only.sh`
- Tag/push existing image

3) `deploy_container.sh`
- SSH to `root@digitalocean`
- `cd /root/Orchestration/obsRemote`
- `docker compose -f run_obsidian_remote.yml pull organizerserver`
- `docker compose -f run_obsidian_remote.yml up -d --no-deps --force-recreate organizerserver`
- Health verification (see below)

4) `full_deployment.sh`
- Orchestrate build → push → deploy → tests
- Match flofluent behavior for skips and status output

---

## Health & Verification Plan

### OrganizerServer Health
- Primary check: `http://localhost:8081/healthcheck` (admin port)
- Since no ports are published, verification should be done via:
  - docker healthcheck, or
  - `docker compose exec organizerserver curl http://localhost:8081/healthcheck`

### MCP Verification
- Use a Bearer token to hit the MCP endpoint after nginx routing is set.

---

## Secrets & Env Management
- `MCP_API_KEY` must live in the prod env file used by docker compose (same pattern as other secrets in obsRemote).
- Do **not** commit secrets in repo.
- Nginx auth uses the same key but is managed separately.

---

## Deployment Path (Exact)
1) Build and push:
   - `./scripts/ci/build-and-push.sh patch`
2) Deploy:
   - `./scripts/ci/deploy_container.sh <version>`
3) Verify:
   - healthcheck via compose exec
   - MCP endpoint with Bearer token

---

## Rollback Plan
- Re-run deploy with previous version tag:
  - `./scripts/ci/deploy_container.sh <previous_version>`

---

## Notes & Dependencies
- Nginx routing is handled by a separate agent; no nginx changes are done in this repo.
- The compose file lives on the prod host and is not in this repo; deploy script must SSH into `root@digitalocean` and operate there.
- The MCP server must run with SSE transport in production to allow HTTP clients.

---

## Implementation Checklist (for next phase)
- [ ] Extend Dockerfile to include MCP server + Python deps
- [ ] Add fail-fast entrypoint script
- [ ] Update README with production MCP notes
- [ ] Create scripts in `scripts/ci/` (mirroring flofluent)
- [ ] Update prod compose (organizerserver service) for image/env/restart/healthcheck
- [ ] Ensure `MCP_API_KEY` set in prod env file
