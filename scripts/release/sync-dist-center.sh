#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
DIST_ROOT="${DIST_ROOT:-$PROJECT_ROOT/../dist-center}"
PROJECT_SLUG="${PROJECT_SLUG:-law-firm-clients}"

COMPOSE_SRC="$PROJECT_ROOT/docker/docker-compose.yml"
ENV_SRC="$PROJECT_ROOT/docker/.env.example"
INIT_DB_SRC="$PROJECT_ROOT/scripts/init-db"
SECRETS_SRC=""
for candidate in \
  "$PROJECT_ROOT/scripts/init-secrets.sh" \
  "$PROJECT_ROOT/deploy/init-secrets.sh" \
  "$PROJECT_ROOT/docker/secrets/init-secrets.sh"
do
  if [[ -f "$candidate" ]]; then
    SECRETS_SRC="$candidate"
    break
  fi
done

COMPOSE_DEST="$DIST_ROOT/assets/compose/$PROJECT_SLUG"
SCRIPTS_DEST="$DIST_ROOT/assets/scripts"

mkdir -p "$COMPOSE_DEST/secrets" "$SCRIPTS_DEST/init-db"

cp "$COMPOSE_SRC" "$COMPOSE_DEST/docker-compose.yml"
cp "$ENV_SRC" "$COMPOSE_DEST/.env.example"
if [[ -n "$SECRETS_SRC" ]]; then
  cp "$SECRETS_SRC" "$COMPOSE_DEST/secrets/init-secrets.sh"
fi

if [[ -d "$INIT_DB_SRC" ]]; then
  if command -v rsync >/dev/null 2>&1; then
    rsync -a --delete "$INIT_DB_SRC/" "$SCRIPTS_DEST/init-db/"
  else
    rm -rf "$SCRIPTS_DEST/init-db"
    mkdir -p "$SCRIPTS_DEST/init-db"
    cp -R "$INIT_DB_SRC"/. "$SCRIPTS_DEST/init-db/"
  fi
  tar -czf "$SCRIPTS_DEST/${PROJECT_SLUG}-init-db.tar.gz" -C "$SCRIPTS_DEST" init-db
fi

cat <<EOF
Synced distribution-center assets.
Dist root: $DIST_ROOT
Project: $PROJECT_SLUG
Compose: $COMPOSE_DEST/docker-compose.yml
Env: $COMPOSE_DEST/.env.example
Archive: $SCRIPTS_DEST/${PROJECT_SLUG}-init-db.tar.gz
EOF
