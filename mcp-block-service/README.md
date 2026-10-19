# MCP Block Service

A standalone Spring Boot MCP server that exposes four fixed onboarding workflow actions as MCP tools. It forwards to a separately running workflow/Brain API and returns the upstream HTTP status and body without treating non-2xx responses as success.

## Stack

- Java 25
- Spring Boot 4.1.1
- Spring AI 2.0.1
- MCP Streamable HTTP server

The MCP server exposes the standard Streamable HTTP MCP endpoint at `/mcp` (local base URL: `http://localhost:8082/mcp`). A small `GET /health` endpoint is also included.

## Workflow tools

| MCP tool | HTTP request sent to configured workflow service |
|---|---|
| `verify_otp` | `POST /workflows/otp/verify` |
| `submit_profile` | `POST /workflows/submit-profile` |
| `submit_account_statement` | `POST /workflows/enter-account-statement` |
| `submit_udyam_document` | `POST /workflows/udyam-document` |

Every tool accepts one required argument, `referenceId`, and sends this JSON body:

```json
{
  "referenceId": "R2"
}
```

Each tool returns a JSON string containing `success`, `httpStatus`, `endpoint`, and the raw upstream `responseBody`. For example, HTTP 409 stays a failure and its response body (including `detail` and `allowedEvents`) is preserved.

## Configure the workflow service URL

Default value is `http://localhost:8081`, which matches the local workflow service setup discussed for development. Override with environment variable:

PowerShell:

```powershell
$env:WORKFLOW_API_BASE_URL = "http://localhost:8081"
```

Optional timeout settings:

```powershell
$env:WORKFLOW_API_CONNECT_TIMEOUT = "5s"
$env:WORKFLOW_API_READ_TIMEOUT = "30s"
```

The base URL must be only the server base, not include one of the workflow paths.

## Run in IntelliJ

1. Extract the ZIP.
2. Open the `mcp-block-service` folder as a Maven project.
3. Ensure IntelliJ uses JDK 25.
4. Allow Maven to import dependencies.
5. Run `McpBlockServiceApplication`.

Or run from a terminal with Maven installed:

```powershell
mvn spring-boot:run
```

Verify:

```powershell
Invoke-RestMethod http://localhost:8082/health
```

Expected body:

```json
{
  "service": "mcp-block-service",
  "status": "UP",
  "mcpEndpoint": "/mcp"
}
```

## Test MCP tools

Use an MCP-compatible client / MCP Inspector to connect to:

`http://localhost:8082/mcp`

List tools and call one of the names above. Example arguments for `submit_profile`:

```json
{
  "referenceId": "R2"
}
```

Before calling a state-changing tool, make sure the orchestration layer has authorized that step. The remote workflow service still enforces its own state transition and may return HTTP 409 when the transition is invalid.

## How this fits the architecture

- This project is the **MCP Block** only: it exposes tools and calls the fixed workflow endpoints.
- Your existing Conversation AI application remains responsible for AgentScope / ReActAgent, Ollama, and reading the selected versioned SOP from lakeFS.
- The existing Conversation AI application must be configured as an **MCP client** to discover and invoke tools from this MCP server. Starting this server alone does not automatically attach it to the existing AgentScope toolkit.
- Brain's dedicated current-state lookup/authorization API was not provided, so this project does not invent or simulate a separate Brain pre-check endpoint. Workflow transition errors from the four supplied endpoints are preserved.

## Security note

The MCP HTTP endpoint is not authenticated by this starter configuration. Keep this service on a trusted local network during development. Before exposing it beyond localhost, add authentication/authorization and restrict access, because the tools perform state-changing workflow operations.
