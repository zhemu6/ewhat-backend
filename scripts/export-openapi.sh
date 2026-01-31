#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8123/api}"
GROUP="${GROUP:-default}"
OUT_FILE="${OUT_FILE:-docs/openapi/openapi.json}"

API_DOCS_URL="$BASE_URL/v3/api-docs/$GROUP"

mkdir -p "$(dirname "$OUT_FILE")"

echo "Fetching OpenAPI: $API_DOCS_URL"
echo "Writing to: $OUT_FILE"

curl -fsSL -H 'Accept: application/json' "$API_DOCS_URL" -o "$OUT_FILE"

echo "Done."
