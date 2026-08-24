# Inventory Local LLM Integration Plan

## Goal

Enable the Inventory app's MCP server to use the private local LLM stack (`clother-local` → LiteLLM proxy → llama.cpp Qwen3.8-27B) for natural-language inventory commands, keeping inventory data off cloud providers and avoiding API costs.

## Current state

The Inventory stack has three layers:

1. **Java Dropwizard backend** — owns the inventory data and exposes a REST API.
2. **Python FastMCP server** (`organizerserver/mcp_server/mcp_inventory/`) — bridges the Java API to MCP clients such as Claude Desktop and the Android voice client.
3. **Android voice client** — sends natural-language requests to the MCP server.

The MCP server currently exposes these tools in `mcp_inventory/server.py`:

- `inventory_get_inventory` — complete overview
- `inventory_get_containers` — list containers
- `inventory_get_container(container_id)` — container contents
- `inventory_find_location(item_name)` — locate an item
- `inventory_create_items(container, items)` — add items
- `inventory_delete_items(container, items)` — remove items
- `inventory(command)` — natural-language meta-tool

The natural-language `inventory` tool exists but only does keyword-based routing; it cannot actually parse arbitrary commands or disambiguate intent. The discrete tools are rigid and require callers to pre-extract `container` and `items` parameters.

There is **no LLM abstraction** in the Inventory codebase today. Unlike DayPlanner, which already has `mcp_server/src/inference/`, the Inventory MCP server talks only to the Java REST API.

## LLM-powered features to add

### 1. Natural-language command parser

Replace the keyword-based router in the `inventory` tool with a local LLM call that parses free-form commands into structured tool calls.

Examples:

- "Put 3 hammers and a box of nails in the toolbox" → `create_items(container="toolbox", items=["3 hammers", "box of nails"])`
- "Where are the nails?" → `find_location(item_name="nails")`
- "Show me what's in the garage" → `get_container(container_id="garage")`
- "Move the hammer from the toolbox to the garage" → `delete_items(container="toolbox", items=["hammer"])` + `create_items(container="garage", items=["hammer"])`
- "I bought 12 2x4s, add them to the shed" → `create_items(container="shed", items=["12 2x4s"])`

The parser should return a JSON object the server can route to the correct discrete tool(s):

```json
{
  "intent": "create_items",
  "container": "toolbox",
  "items": ["3 hammers", "box of nails"],
  "dry_run": false,
  "explanation": "Adding tools to the toolbox"
}
```

For multi-step commands like "move", the model should emit a list of actions.

### 2. Fuzzy item/container matching

The Java API expects exact-ish container and item identifiers. The local LLM can map colloquial names to actual stored names:

- "the big toolbox" → "toolbox"
- "my laptop" → "laptop-computer-001" (if the API supports IDs)
- "screws" → any container containing screws

This reduces friction for voice input where the user may not remember exact names.

### 3. Voice-to-action confirmation summaries

Before executing mutating commands, the LLM can generate a concise confirmation prompt:

> "I'll add 3 hammers and a box of nails to the toolbox. Is that correct?"

This is especially useful for the Android voice client where the user cannot easily review structured parameters.

### 4. Optional: inventory Q&A

Answer questions like "Do I have any hammers?" or "What's in the garage?" by fetching inventory data and asking the LLM to summarize it. This is similar to the existing tools but lets the model decide which tool(s) to call and how to phrase the answer.

## What needs to happen

### 1. Add an inference client abstraction

Create a small LLM client module under `mcp_server/mcp_inventory/inference/`:

```
mcp_inventory/inference/
├── __init__.py
├── base.py          # InferenceClient base class
├── factory.py       # from_env_or_noop()
└── openai.py        # OpenAI-compatible local backend
```

This mirrors DayPlanner's design and can be copied/trimmed. The factory should default to noop so existing installs keep working without any env vars:

```python
provider = os.getenv("INFERENCE_PROVIDER", "noop")

if provider == "openai":
    return OpenAIInferenceClient.from_env()
elif provider == "local":
    return LocalInferenceClient.from_env()
else:
    return NoopInferenceClient()
```

For the first milestone, implement only:

- `complete_json(system_prompt, user_prompt, schema)` — structured JSON output for command parsing.
- (Optional later) `complete()` for summaries.

### 2. Configuration for local inference

The local stack is OpenAI-compatible through LiteLLM. Add these env vars:

```bash
INFERENCE_PROVIDER=openai
OPENAI_API_KEY=sk-test-key
OPENAI_BASE_URL=http://192.168.1.3:4000/v1
OPENAI_MODEL=claude-sonnet-4-20250514
```

Or use a dedicated `local` provider to keep cloud and local configs separate:

```bash
INFERENCE_PROVIDER=local
LOCAL_LLM_BASE_URL=http://192.168.1.3:4000/v1
LOCAL_LLM_API_KEY=sk-test-key
LOCAL_LLM_MODEL=claude-sonnet-4-20250514
```

Recommendation: support both. The `local` provider makes intent explicit; the `openai` provider lets Inventory reuse the same LiteLLM endpoint if the user already configured it for DayPlanner.

### 3. Decide default vs. opt-in behavior

Options:

- **A. Env-only opt-in (recommended)** — default to keyword-based routing unless `INFERENCE_PROVIDER` is set. Existing behavior is unchanged.
- **B. Local-first default** — use the LLM parser whenever `LOCAL_LLM_BASE_URL` or `OPENAI_BASE_URL` is set.

Start with **A**. The keyword router still works and the app does not silently change behavior on installs that happen to have local LLM env vars.

### 4. Implement the LLM-powered `inventory` tool

Update `inventory()` in `server.py` to:

1. Check if inference is configured. If not, fall back to the existing keyword router.
2. Build a prompt containing:
   - Available tools and their schemas.
   - Current container names (from `get_containers`) to help with fuzzy matching.
   - The user's natural-language command.
3. Call `inference_client.complete_json()` with a strict schema.
4. Validate the response and execute the requested tool(s).
5. Return the tool result(s) to the caller.

A minimal action schema:

```json
{
  "type": "object",
  "properties": {
    "intent": {"enum": ["create_items", "delete_items", "find_location", "get_container", "get_containers", "get_inventory", "clarify"]},
    "container": {"type": "string"},
    "items": {"type": "array", "items": {"type": "string"}},
    "item_name": {"type": "string"},
    "dry_run": {"type": "boolean"},
    "explanation": {"type": "string"},
    "clarification_question": {"type": "string"}
  },
  "required": ["intent"]
}
```

### 5. Add context for fuzzy matching

For commands that mention containers or items, include a compact snapshot in the prompt:

```
Available containers:
- toolbox (hammer, screwdriver, wrench)
- garage (lawnmower, ladder)
- kitchen (blender, toaster)
```

This lets the model resolve "the toolbox" to the actual container name. Watch prompt size: if the inventory grows large, fetch only container names first and lazily load container contents when needed.

### 6. Wire through the Android voice client

The voice client currently sends natural-language commands. It should continue calling the `inventory` tool; the server-side parser change is transparent. If the client does its own command parsing today, remove it and let the server handle parsing so improvements apply to all callers.

### 7. Error handling and graceful degradation

- If the LLM returns invalid JSON or an unknown intent, fall back to keyword routing and return a helpful message.
- If inference is unavailable, keyword routing still works.
- For mutating operations, default `dry_run=True` when confidence is low and ask the user to confirm.

## Suggested implementation order

1. Copy/adapt DayPlanner's inference client into `mcp_inventory/inference/`.
2. Add env-based configuration (`INFERENCE_PROVIDER`, `OPENAI_*`, `LOCAL_LLM_*`).
3. Implement a JSON-mode prompt for command parsing.
4. Update `inventory()` to use the LLM parser when configured, with keyword fallback.
5. Test end-to-end with the local LiteLLM/Qwen stack using voice-like commands.
6. Document env vars in `docs/deployment/` or `docs/ops/`.
7. (Optional) Add fuzzy matching by including container names in the prompt.
8. (Optional) Add a confirmation/summary step before mutating commands.

## Acceptance criteria

- [ ] Inventory MCP server can be configured to use the local LiteLLM/Qwen backend via env vars.
- [ ] The `inventory` natural-language tool parses commands via the local backend when configured.
- [ ] Parsed commands map correctly to discrete tools (`create_items`, `delete_items`, `find_location`, etc.).
- [ ] Multi-step commands (e.g., "move X from A to B") are handled correctly.
- [ ] When inference is not configured, the existing keyword-based behavior still works.
- [ ] Response format matches the existing tool behavior (human-readable strings).
- [ ] Required env vars are documented.

## Risks / considerations

- **Latency**: first call in a session pays the ~24k-token system-prompt cost (~49s) on the local Qwen stack. The `inventory` prompt is small, so subsequent calls should be fast (~2s) thanks to KV cache reuse.
- **Inventory size**: loading all containers/items into the prompt for fuzzy matching could exceed context or slow inference. Cap the snapshot or load lazily.
- **Quality**: Qwen3.8-27B may not follow JSON instructions as reliably as Claude. Validate and possibly add retry/fallback logic.
- **Mutating commands**: voice input plus LLM parsing increases the risk of incorrect deletes/adds. Default to `dry_run` previews and require explicit confirmation for destructive operations.
- **Data privacy**: traffic stays on the local network (192.168.1.3), but the LiteLLM proxy and llama.cpp server are on a separate worker. Acceptable for home lab.
- **No existing abstraction**: Inventory needs a new inference module, unlike DayPlanner which already has one. This is a one-time setup cost.

## Recommended first step

1. Add a minimal `mcp_inventory/inference/` module with OpenAI-compatible local client.
2. Update the `inventory` tool to call the LLM parser when `INFERENCE_PROVIDER` is set.
3. Run a few natural-language commands against the local backend.
4. Compare outputs to the keyword-based baseline.
5. If quality is good, document the config and iterate on fuzzy matching and confirmation flows.
