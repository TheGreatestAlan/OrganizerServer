# Bearer Token Auth Implementation Plan

## Overview

Implement standardized bearer token authentication for the OrganizerServer MCP server based on the design document.

## Implementation Steps

### Step 1: Update Auth Logic in Server

**File:** `mcp_server/mcp_inventory/server.py`

Update the `is_authorized()` function to support `sk-` prefixed tokens:

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
        if hmac.compare_digest(token, api_key):
            return True
        # Log safe version if key doesn't match (for debugging)
        if token.startswith("sk-"):
            safe_token = token[:16] + "..." if len(token) > 16 else token
            logger.warning(f"Invalid sk- format API key attempt: {safe_token}")

    # Case 2: JWT token (eyJ prefix - base64 encoded JWT)
    if token.startswith("eyJ"):
        return validate_oauth_token(token)

    return False
```

**Acceptance Criteria:**
- [ ] `sk-organizer-` prefixed tokens are validated correctly
- [ ] Legacy plain API keys still work (backward compatible)
- [ ] JWT tokens still work (OAuth)
- [ ] Invalid `sk-` keys are logged safely (masked)

### Step 2: Add Safe Key Logging Helper

**File:** `mcp_server/mcp_inventory/server.py`

Add a helper function for masking API keys in logs:

```python
def mask_api_key(key: str | None, visible_chars: int = 12) -> str:
    """Mask an API key for safe logging.

    Shows first N characters followed by ellipsis.
    Returns '<not set>' if key is None.
    """
    if not key:
        return "<not set>"
    if len(key) <= visible_chars:
        return "***"
    return f"{key[:visible_chars]}..."
```

Update the main() function to use safe logging:

```python
# In main(), replace:
# logger.info(f"API Key: {api_key}")
# With:
if api_key:
    logger.info(f"API Key: {mask_api_key(api_key)}")
```

**Acceptance Criteria:**
- [ ] API keys are masked in log output
- [ ] Function handles None and short keys gracefully

### Step 3: Test Implementation

**Test with curl:**

```bash
# Set a test key
export MCP_API_KEY="sk-organizer-a3f7c9d2e8b115f6c4d9e2a7b5f8c1d3e6a9b2c5d8e1f4a7b0c3d6e9f2a5b8"

# Test 1: No auth (should fail)
curl -s -X POST "http://localhost:3000/mcp" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
# Expected: 401 Unauthorized

# Test 2: Invalid sk- key (should fail)
curl -s -X POST "http://localhost:3000/mcp" \
  -H "Authorization: Bearer sk-organizer-invalid0000000000000000000000000000000000000000000000000000" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
# Expected: 401 Unauthorized

# Test 3: Valid sk- key (should succeed)
curl -s -X POST "http://localhost:3000/mcp" \
  -H "Authorization: Bearer $MCP_API_KEY" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
# Expected: 200 with JSON response

# Test 4: Legacy plain key still works
export MCP_API_KEY="my-plain-api-key-123"
curl -s -X POST "http://localhost:3000/mcp" \
  -H "Authorization: Bearer my-plain-api-key-123" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
# Expected: 200 with JSON response

# Test 5: OAuth still works (if OAuth is enabled)
curl -s -X POST "http://localhost:3000/mcp" \
  -H "Authorization: Bearer $OAUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
# Expected: 200 with JSON response
```

**Acceptance Criteria:**
- [ ] All test cases pass
- [ ] OAuth tokens still work (backward compatibility)
- [ ] API key authentication works with both formats

### Step 4: Generate Production Key

```bash
# Generate a secure key
openssl rand -hex 32 | xargs -I {} echo "sk-organizer-{}"

# Example output:
# sk-organizer-a3f7c9d2e8b115f6c4d9e2a7b5f8c1d3e6a9b2c5d8e1f4a7b0c3d6e9f2a5b8
```

Save this for production deployment.

### Step 5: Update Documentation

**File:** `mcp_server/mcp_inventory/README.md`

Add new section "## Bearer Token Authentication (OpenWebUI)":

```markdown
## Bearer Token Authentication (OpenWebUI)

For OpenWebUI and other clients that don't support OAuth 2.1 Dynamic Client Registration:

### Generating an API Key

Generate a secure API key with the `sk-organizer-` prefix:

```bash
openssl rand -hex 32 | xargs -I {} echo "sk-organizer-{}"
```

This produces a high-entropy key like:
`sk-organizer-a3f7c9d2e8b115f6c4d9e2a7b5f8c1d3e6a9b2c5d8e1f4a7b0c3d6e9f2a5b8`

### OpenWebUI Configuration

1. Go to **Admin Panel > Tools > Add Tool Server**
2. Configure:
   - **URL:** `https://alanhoangnguyen.com/mcp/organizer`
   - **Auth Type:** `bearer`
   - **Key:** `sk-organizer-your-api-key-here`
3. Click "Verify Connection" to test

### Testing with curl

```bash
curl -s -X POST "https://alanhoangnguyen.com/mcp/organizer" \
  -H "Authorization: Bearer sk-organizer-your-api-key" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {"name": "test", "version": "1.0"}
    }
  }'
```

### Backward Compatibility

The server also accepts:
- Legacy plain API keys (for existing integrations)
- OAuth 2.1 tokens (JWT format with `eyJ` prefix)
```

**Acceptance Criteria:**
- [ ] Documentation includes OpenWebUI setup steps
- [ ] Example curl commands work
- [ ] Key generation instructions are clear
- [ ] Clear distinction between OAuth, sk- keys, and plain keys

### Step 6: Deploy

1. Generate new `sk-organizer-` format key
2. Set `MCP_API_KEY` in production environment
3. Build and deploy new version
4. Test with OpenWebUI

**Environment setup:**

```bash
# Add to production docker-compose.env
MCP_API_KEY=sk-organizer-your-generated-key-here
```

## Rollback Steps

If issues occur:

1. Revert to previous API key format (plain key)
2. Redeploy

## Verification Checklist

- [ ] Bearer token auth works with OpenWebUI
- [ ] OAuth still works for existing clients
- [ ] Legacy plain API keys still work
- [ ] Invalid tokens are rejected with 401
- [ ] API keys are masked in logs
- [ ] Documentation updated
