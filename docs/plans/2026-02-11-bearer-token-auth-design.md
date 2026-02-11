# Bearer Token Authentication for MCP Servers

## Overview

Add standardized API key (bearer token) authentication to the OrganizerServer MCP server, enabling OpenWebUI and other clients to connect with a consistent `sk-` prefixed key format.

## Goals

- Support OpenWebUI's `bearer` auth_type for MCP tool servers
- Align API key format with todo-infrastructure (`sk-organizer-<64-char-hex>`)
- Maintain backward compatibility with existing OAuth and plain API keys
- Simple env-based key management (no database)
- Secure by default with high-entropy keys

## Non-Goals

- Multi-key management (for now, single key via env var)
- Key rotation automation
- Per-user or per-client access control
- Rate limiting per key

## Architecture

### Current Flow (OAuth + Plain API Key)

```
Client → Nginx → MCP Server → Auth Check
                               ↓
                    ┌─────────┴─────────┐
                    ↓                   ↓
              Bearer <any>         Bearer eyJ... (JWT)
                    ↓                   ↓
              Compare to           OAuth validate
              MCP_API_KEY          (PyJWKClient)
```

### New Flow (Standardized Bearer + OAuth)

```
Client → Nginx → MCP Server → Auth Middleware
                                   ↓
                              Check Authorization header
                                   ↓
                         ┌────────┴────────┐
                         ↓                 ↓
                    Bearer sk-xxx      Bearer eyJ... (JWT)
                         ↓                 ↓
                   Validate against    OAuth validate
                   MCP_API_KEY env     (PyJWKClient)
```

## Design

### 1. API Key Format

```
sk-organizer-<64-char-hex>

Example: sk-organizer-a3f7c9d2e8b115f6c4d9e2a7b5f8c1d3e6a9b2c5d8e1f4a7b0c3d6e9f2a5b8
```

- `sk-` = secret key prefix (industry convention)
- `organizer-` = service identifier
- 64 hex chars = 256 bits entropy

### 2. Configuration

**Environment Variables:**

```bash
# API key for bearer token auth (supports both old plain format and new sk- format)
MCP_API_KEY=sk-organizer-<64-char-hex>

# Existing: OAuth still works
MCP_OAUTH_ENABLED=true
MCP_OAUTH_ISSUER=https://alanhoangnguyen.com/oauth/realms/mcp
MCP_OAUTH_AUDIENCE=https://alanhoangnguyen.com/mcp
```

**Key Generation:**

```bash
# One-liner for generating keys
openssl rand -hex 32 | xargs -I {} echo "sk-organizer-{}"
```

### 3. Validation Logic

Update the `is_authorized()` function in `server.py`:

```python
def is_authorized(auth_header: str | None, api_key: str | None) -> bool:
    """Validate Authorization header against API key or OAuth token.

    Supports:
    1. Bearer sk-organizer-... (API key - new format)
    2. Bearer <any-plain-key> (API key - backward compatible)
    3. Bearer eyJ... (JWT OAuth token)
    """
    token = extract_bearer_token(auth_header)
    if not token:
        return False

    # Case 1: API key (sk- prefix or plain)
    if api_key:
        # Support both sk- prefixed keys and legacy plain keys
        if token.startswith("sk-"):
            # New format: constant-time comparison
            if hmac.compare_digest(token, api_key):
                return True
        else:
            # Legacy plain key comparison
            if hmac.compare_digest(token, api_key):
                return True

    # Case 2: JWT token (eyJ prefix - base64 encoded JWT)
    if token.startswith("eyJ"):
        return validate_oauth_token(token)

    return False
```

### 4. OpenWebUI Configuration

Users configure the MCP server in OpenWebUI with:

```json
{
  "name": "organizer-inventory",
  "url": "https://alanhoangnguyen.com/mcp/organizer",
  "auth_type": "bearer",
  "key": "sk-organizer-a3f7c9d2e8b115..."
}
```

OpenWebUI sends:

```
Authorization: Bearer sk-organizer-a3f7c9d2e8b115...
```

### 5. Security Considerations

**Threat: Key leakage**

- Mitigation: Keys are long (78 chars) and high-entropy
- Rotation: Change `MCP_API_KEY` env var and restart container

**Threat: Man-in-the-middle**

- Mitigation: HTTPS only (enforced at nginx)

**Threat: Brute force**

- Mitigation: 256-bit entropy = 2^256 possible keys
- Nginx rate limiting already in place

**Threat: Key in logs**

- Mitigation: Mask key in logs (show first 12 chars only: `sk-organizer-a3f7...`)

### 6. Implementation Plan

**Phase 1: Update Auth Logic**

- Update `is_authorized()` to recognize `sk-` prefix
- Add helper function for safe key logging
- Ensure backward compatibility with plain API keys

**Phase 2: Testing**

- Unit tests for auth logic
- Integration test with curl
- Verify OAuth still works
- Verify legacy API keys still work

**Phase 3: Documentation**

- Update README with bearer token section
- Document key generation
- OpenWebUI setup instructions

**Phase 4: Deployment**

- Generate new `sk-organizer-` format key
- Set `MCP_API_KEY` in production env
- Deploy new version
- Verify OpenWebUI can connect

## Files to Modify

1. `mcp_server/mcp_inventory/server.py` - Update `is_authorized()` and add logging helper
2. `mcp_server/mcp_inventory/README.md` - Document bearer token usage

## Rollback Plan

If issues arise:

1. Revert to previous API key format (plain key still supported)
2. Redeploy previous version

## Migration from OAuth/Plain Keys

- Existing OAuth clients (Claude.ai, Claude Code) continue working unchanged
- Existing plain API keys continue working (backward compatible)
- New OpenWebUI clients use `sk-organizer-` bearer token format

No migration needed - all auth methods work simultaneously.

## Future Enhancements (Out of Scope)

- Multiple API keys via database table
- Key expiration/rotation UI
- Per-key rate limiting
- Audit logging of key usage
