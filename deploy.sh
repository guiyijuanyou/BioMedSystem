#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

if [ -f .env ]; then
    set -a
    source .env
    set +a
    log_info "Loaded .env file"
fi

COMMAND="${1:-deploy}"

case "$COMMAND" in
    build)
        log_info "Building Docker images..."
        docker compose build
        ;;
    start)
        log_info "Starting services..."
        docker compose up -d
        log_info "Services started. Check status with: docker compose ps"
        ;;
    stop)
        log_info "Stopping services..."
        docker compose down
        ;;
    restart)
        log_info "Restarting services..."
        docker compose down
        docker compose up -d
        ;;
    deploy)
        log_info "Full deployment..."
        docker compose build
        docker compose up -d --remove-orphans
        log_info "Deployment complete."
        ;;
    logs)
        docker compose logs -f "${2:-backend}"
        ;;
    ps)
        docker compose ps
        ;;
    migrate)
        log_info "Running Flyway migrations..."
        docker compose exec -T backend java -jar app.jar \
            --spring.flyway.clean-disabled=false
        ;;
    backup)
        log_info "Creating database backup..."
        mkdir -p data/backup
        docker compose exec -T mysql \
            mysqldump -u root -p"${DB_PASSWORD:-root}" biomed \
            > "data/backup/biomed-$(date +%Y%m%d-%H%M%S).sql"
        log_info "Backup created."
        ;;
    *)
        echo "Usage: $0 {build|start|stop|restart|deploy|logs|ps|migrate|backup}"
        exit 1
        ;;
esac
