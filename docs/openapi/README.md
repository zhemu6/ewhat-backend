# OpenAPI Export

This repo exposes OpenAPI at runtime and also keeps a snapshot JSON for frontend usage.

Runtime endpoints (context path is `/api`):
- Swagger UI: `http://localhost:8123/api/doc.html`
- OpenAPI JSON: `http://localhost:8123/api/v3/api-docs/default`

## Export to file

Windows (PowerShell):

```powershell
./scripts/export-openapi.ps1
```

macOS/Linux (bash):

```bash
./scripts/export-openapi.sh
```

Output:
- `docs/openapi/openapi.json`

Notes:
- The backend must be running.
- If you use a different host/port/context-path, set `BaseUrl` / `BASE_URL`.
