#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

REMOTE_HOST="${REMOTE_HOST:-contabo}"
REMOTE_DEPLOY_DIR="${REMOTE_DEPLOY_DIR:-~/npg-deploy/pb-service}"
REMOTE_NETWORK="${REMOTE_NETWORK:-postgres_default}"
DEPLOY_ENV_FILE="${DEPLOY_ENV_FILE:-.env.docker}"

IMAGE_NAME="${IMAGE_NAME:-nopunnygames/pb-service}"
IMAGE_TAG="${IMAGE_TAG:-$(date +%Y%m%d%H%M%S)}"
CONTAINER_NAME="${CONTAINER_NAME:-pb-service}"

HOST_BIND="${HOST_BIND:-127.0.0.1}"
HOST_PORT="${HOST_PORT:-8083}"
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
RUNTIME_ENV_PATH="build/deploy/runtime.env"

docker build -t "${IMAGE_REF}" -t "${IMAGE_LATEST}" -f Dockerfile .
docker save "${IMAGE_REF}" | gzip -c > "${ARCHIVE_PATH}"

grep -E '^[A-Za-z_][A-Za-z0-9_]*=' "${DEPLOY_ENV_FILE}" \
  | grep -vE '^(REMOTE_|IMAGE_|CONTAINER_|HOST_|RUN_TESTS|DEPLOY_ENV_FILE|REMOTE_DATABASE_PASSWORD)=' \
  > "${RUNTIME_ENV_PATH}" || true

ssh "${REMOTE_HOST}" "mkdir -p ${REMOTE_DEPLOY_DIR}"
scp "${ARCHIVE_PATH}" "${RUNTIME_ENV_PATH}" "${REMOTE_HOST}:${REMOTE_DEPLOY_DIR}/"

ssh "${REMOTE_HOST}" "\
  set -euo pipefail; \
  cd ${REMOTE_DEPLOY_DIR}; \
  gzip -dc ${ARCHIVE_NAME} | docker load; \
  if docker ps -a --format '{{.Names}}' | grep -qx '${CONTAINER_NAME}'; then docker rm -f '${CONTAINER_NAME}'; fi; \
  docker run -d \
    --name '${CONTAINER_NAME}' \
    --restart unless-stopped \
    --network '${REMOTE_NETWORK}' \
    -p '${HOST_BIND}:${HOST_PORT}:${CONTAINER_PORT}' \
    --env-file runtime.env \
    '${IMAGE_REF}'; \
  docker ps --filter name='^/${CONTAINER_NAME}$'"

echo "Deployed ${IMAGE_REF} to ${REMOTE_HOST} as ${CONTAINER_NAME}."
