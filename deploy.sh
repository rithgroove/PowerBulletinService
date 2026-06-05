#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

REMOTE_HOST="${REMOTE_HOST:-contabo}"
REMOTE_DEPLOY_ROOT="${REMOTE_DEPLOY_ROOT:-~/npg-deploy}"
REMOTE_DEPLOY_DIR="${REMOTE_DEPLOY_DIR:-${REMOTE_DEPLOY_ROOT}/pb-service}"
REMOTE_COMPOSE_FILE="${REMOTE_COMPOSE_FILE:-docker-compose.local.yml}"
COMPOSE_SERVICE="${COMPOSE_SERVICE:-pb-service}"
DEPLOY_ENV_FILE="${DEPLOY_ENV_FILE:-.env.docker}"

IMAGE_NAME="${IMAGE_NAME:-nopunnygames/pb-service}"
IMAGE_TAG="${IMAGE_TAG:-$(date +%Y%m%d%H%M%S)}"
CONTAINER_NAME="${CONTAINER_NAME:-pb-service}"
CONTAINER_PORT="${CONTAINER_PORT:-8083}"

RUN_TESTS="${RUN_TESTS:-false}"

if [[ ! -f "${DEPLOY_ENV_FILE}" ]]; then
  echo "Missing ${DEPLOY_ENV_FILE}. Create it with the production Docker environment." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${DEPLOY_ENV_FILE}"
set +a

REMOTE_DATABASE_PASSWORD="${REMOTE_DATABASE_PASSWORD:-${DATABASE_PASSWORD:-${PB_DB_PASSWORD:-}}}"

if [[ -z "${REMOTE_DATABASE_PASSWORD}" ]]; then
  echo "Missing database password. Set DATABASE_PASSWORD or PB_DB_PASSWORD in ${DEPLOY_ENV_FILE}." >&2
  exit 1
fi

if [[ -z "${JWT_SECRET:-}" ]]; then
  echo "Missing JWT_SECRET in ${DEPLOY_ENV_FILE}." >&2
  exit 1
fi

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_command docker
require_command gzip
require_command scp
require_command ssh

if [[ "${RUN_TESTS}" == "true" ]]; then
  ./gradlew clean test bootJar
else
  ./gradlew clean bootJar
fi

mkdir -p build/deploy

APP_JAR="$(find build/libs -maxdepth 1 -type f -name '*.jar' \
  ! -name '*-plain.jar' \
  ! -name '*-sources.jar' \
  ! -name '*-javadoc.jar' \
  | sort \
  | tail -n 1)"

if [[ -z "${APP_JAR}" ]]; then
  echo "No runnable jar found in build/libs." >&2
  exit 1
fi

cp "${APP_JAR}" build/deploy/app.jar

IMAGE_REF="${IMAGE_NAME}:${IMAGE_TAG}"
IMAGE_LATEST="${IMAGE_NAME}:latest"
ARCHIVE_NAME="pb-service-${IMAGE_TAG}.tar.gz"
ARCHIVE_PATH="build/deploy/${ARCHIVE_NAME}"

docker build -t "${IMAGE_REF}" -t "${IMAGE_LATEST}" -f Dockerfile .
docker save "${IMAGE_REF}" | gzip -c > "${ARCHIVE_PATH}"

ssh "${REMOTE_HOST}" "mkdir -p ${REMOTE_DEPLOY_DIR}"
scp "${ARCHIVE_PATH}" "${REMOTE_HOST}:${REMOTE_DEPLOY_DIR}/"

ssh "${REMOTE_HOST}" "\
  set -euo pipefail; \
  cd ${REMOTE_DEPLOY_ROOT}; \
  gzip -dc ${COMPOSE_SERVICE}/${ARCHIVE_NAME} | docker load; \
  cp '${REMOTE_COMPOSE_FILE}' '${REMOTE_COMPOSE_FILE}.bak-${IMAGE_TAG}-${COMPOSE_SERVICE}'; \
  sed -i '/^  ${COMPOSE_SERVICE}:/,/^  [A-Za-z0-9_-]*:/ s#^\([[:space:]]*image: \).*#\1${IMAGE_REF}#' '${REMOTE_COMPOSE_FILE}'; \
  docker compose -f '${REMOTE_COMPOSE_FILE}' up -d --no-deps '${COMPOSE_SERVICE}'; \
  docker compose -f '${REMOTE_COMPOSE_FILE}' ps '${COMPOSE_SERVICE}'"

echo "Deployed ${IMAGE_REF} to ${REMOTE_HOST} compose service ${COMPOSE_SERVICE}."
