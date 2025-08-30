#!/bin/bash

# YSC LMS Backup Script
# 
# This script provides comprehensive backup functionality for the YSC LMS system
# including database backups, file backups, configuration backups, and disaster recovery.
# 
# Usage: ./backup.sh [OPTION]
# Options:
#   --full              Perform full backup (default)
#   --database          Database backup only
#   --files             File backup only
#   --config            Configuration backup only
#   --incremental       Incremental database backup
#   --restore           Restore from backup
#   --health            Check backup system health
#   --cleanup           Clean up old backups
#   --help              Show this help message

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="${BACKUP_DIR:-$PROJECT_ROOT/backups}"
LOG_FILE="${LOG_FILE:-$BACKUP_DIR/backup.log}"
API_BASE_URL="${API_BASE_URL:-http://localhost:8081/api}"
ADMIN_TOKEN="${ADMIN_TOKEN:-}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

# Initialize backup environment
init_backup() {
    log "Initializing backup environment..."
    
    # Create backup directory structure
    mkdir -p "$BACKUP_DIR"/{database,files,config,audit,scripts,logs}
    
    # Ensure log file exists
    touch "$LOG_FILE"
    
    # Set appropriate permissions
    chmod 750 "$BACKUP_DIR"
    chmod 640 "$LOG_FILE"
    
    log_info "Backup environment initialized"
}

# Check if backend is running
check_backend() {
    log "Checking backend availability..."
    
    if curl -sf "$API_BASE_URL/health" > /dev/null 2>&1; then
        log_info "Backend is running"
        return 0
    else
        log_error "Backend is not accessible at $API_BASE_URL"
        return 1
    fi
}

# Get admin authentication token
get_admin_token() {
    if [ -n "$ADMIN_TOKEN" ]; then
        echo "$ADMIN_TOKEN"
        return 0
    fi
    
    log "Attempting to get admin token..."
    
    # Try to login with default admin credentials
    local response
    response=$(curl -sf -X POST "$API_BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "email": "yadmin@ycs.com",
            "password": "YSCAdmin2024!"
        }' 2>/dev/null || echo "")
    
    if [ -n "$response" ]; then
        local token
        token=$(echo "$response" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
        if [ -n "$token" ]; then
            echo "$token"
            return 0
        fi
    fi
    
    log_error "Failed to obtain admin token"
    return 1
}

# Call backup API
call_backup_api() {
    local endpoint="$1"
    local method="${2:-POST}"
    
    local token
    if ! token=$(get_admin_token); then
        return 1
    fi
    
    log "Calling backup API: $method $API_BASE_URL/backup$endpoint"
    
    local response
    response=$(curl -sf -X "$method" "$API_BASE_URL/backup$endpoint" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" 2>/dev/null || echo "")
    
    if [ -n "$response" ]; then
        echo "$response"
        return 0
    else
        log_error "Failed to call backup API"
        return 1
    fi
}

# Perform full backup
backup_full() {
    log "Starting full backup..."
    
    if check_backend; then
        local response
        if response=$(call_backup_api "/full"); then
            log_info "Full backup initiated successfully"
            echo "$response" | grep -o '"message":"[^"]*' | cut -d'"' -f4
        else
            log_error "Failed to initiate full backup via API"
            return 1
        fi
    else
        log_error "Cannot perform backup: backend not available"
        return 1
    fi
}

# Perform database backup
backup_database() {
    local incremental="${1:-false}"
    local backup_type="full"
    
    if [ "$incremental" = "true" ]; then
        backup_type="incremental"
    fi
    
    log "Starting $backup_type database backup..."
    
    if check_backend; then
        local response
        if response=$(call_backup_api "/database?incremental=$incremental"); then
            log_info "Database backup completed successfully"
            echo "$response"
        else
            log_error "Failed to perform database backup"
            return 1
        fi
    else
        # Fallback to direct database backup
        log_warn "Backend not available, attempting direct database backup..."
        backup_database_direct "$incremental"
    fi
}

# Direct database backup (fallback)
backup_database_direct() {
    local incremental="${1:-false}"
    local timestamp
    timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$BACKUP_DIR/database/ycs_lms_${timestamp}.sql"
    
    # Get database connection details
    local db_host="${DB_HOST:-localhost}"
    local db_port="${DB_PORT:-3306}"
    local db_name="${DB_NAME:-ycs_lms}"
    local db_user="${DB_USER:-root}"
    local db_password="${DB_PASSWORD:-}"
    
    log "Performing direct database backup to: $backup_file"
    
    # Create database backup directory
    mkdir -p "$(dirname "$backup_file")"
    
    if command -v mysqldump > /dev/null 2>&1; then
        local mysqldump_cmd="mysqldump"
        
        if [ -n "$db_password" ]; then
            mysqldump_cmd="$mysqldump_cmd -p$db_password"
        fi
        
        $mysqldump_cmd -h "$db_host" -P "$db_port" -u "$db_user" \
            --single-transaction \
            --routines \
            --triggers \
            --events \
            --hex-blob \
            "$db_name" > "$backup_file"
        
        # Compress the backup
        if command -v gzip > /dev/null 2>&1; then
            gzip "$backup_file"
            backup_file="${backup_file}.gz"
            log_info "Database backup compressed: $backup_file"
        fi
        
        local file_size
        file_size=$(du -h "$backup_file" | cut -f1)
        log_info "Database backup completed: $backup_file ($file_size)"
        
    else
        log_error "mysqldump not found, cannot perform database backup"
        return 1
    fi
}

# Perform file backup
backup_files() {
    log "Starting file backup..."
    
    if check_backend; then
        local response
        if response=$(call_backup_api "/files"); then
            log_info "File backup completed successfully"
            echo "$response"
        else
            log_error "Failed to perform file backup"
            return 1
        fi
    else
        log_warn "Backend not available, attempting direct file backup..."
        backup_files_direct
    fi
}

# Direct file backup (fallback)
backup_files_direct() {
    local timestamp
    timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$BACKUP_DIR/files/files_backup_${timestamp}.tar.gz"
    
    log "Performing direct file backup to: $backup_file"
    
    # Create file backup directory
    mkdir -p "$(dirname "$backup_file")"
    
    # Define directories to backup
    local dirs_to_backup=()
    
    # Add directories if they exist
    [ -d "$PROJECT_ROOT/uploads" ] && dirs_to_backup+=("uploads")
    [ -d "$PROJECT_ROOT/logs" ] && dirs_to_backup+=("logs")
    [ -d "$PROJECT_ROOT/config" ] && dirs_to_backup+=("config")
    
    if [ ${#dirs_to_backup[@]} -eq 0 ]; then
        log_warn "No directories found for file backup"
        return 0
    fi
    
    # Create tar archive
    cd "$PROJECT_ROOT"
    tar -czf "$backup_file" \
        --exclude='*.tmp' \
        --exclude='*.log.gz' \
        --exclude='cache/*' \
        "${dirs_to_backup[@]}" 2>/dev/null || {
        log_error "Failed to create file backup archive"
        return 1
    }
    
    local file_size
    file_size=$(du -h "$backup_file" | cut -f1)
    log_info "File backup completed: $backup_file ($file_size)"
}

# Perform configuration backup
backup_config() {
    log "Starting configuration backup..."
    
    if check_backend; then
        local response
        if response=$(call_backup_api "/config"); then
            log_info "Configuration backup completed successfully"
            echo "$response"
        else
            log_error "Failed to perform configuration backup"
            return 1
        fi
    else
        log_warn "Backend not available, attempting direct config backup..."
        backup_config_direct
    fi
}

# Direct configuration backup (fallback)
backup_config_direct() {
    local timestamp
    timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_dir="$BACKUP_DIR/config/config_backup_$timestamp"
    
    log "Performing direct configuration backup to: $backup_dir"
    
    # Create config backup directory
    mkdir -p "$backup_dir"
    
    # Copy configuration files
    local files_copied=0
    
    # Backend configuration
    if [ -d "$PROJECT_ROOT/backend/src/main/resources" ]; then
        find "$PROJECT_ROOT/backend/src/main/resources" -name "application*.yml" -o -name "application*.properties" | while read -r file; do
            cp "$file" "$backup_dir/"
            ((files_copied++))
        done
    fi
    
    # Docker configuration
    [ -f "$PROJECT_ROOT/docker-compose.yml" ] && cp "$PROJECT_ROOT/docker-compose.yml" "$backup_dir/" && ((files_copied++))
    [ -f "$PROJECT_ROOT/Dockerfile" ] && cp "$PROJECT_ROOT/Dockerfile" "$backup_dir/" && ((files_copied++))
    
    # Kubernetes configuration
    if [ -d "$PROJECT_ROOT/k8s" ]; then
        cp -r "$PROJECT_ROOT/k8s" "$backup_dir/" && ((files_copied++))
    fi
    
    # Environment files
    [ -f "$PROJECT_ROOT/.env" ] && cp "$PROJECT_ROOT/.env" "$backup_dir/.env.backup" && ((files_copied++))
    [ -f "$PROJECT_ROOT/.env.production" ] && cp "$PROJECT_ROOT/.env.production" "$backup_dir/" && ((files_copied++))
    
    log_info "Configuration backup completed: $files_copied files copied to $backup_dir"
}

# Check backup system health
check_health() {
    log "Checking backup system health..."
    
    if check_backend; then
        local response
        if response=$(call_backup_api "/health" "GET"); then
            echo "$response" | python3 -m json.tool 2>/dev/null || echo "$response"
        else
            log_error "Failed to get backup health status"
            return 1
        fi
    else
        log_error "Cannot check health: backend not available"
        return 1
    fi
}

# Get backup statistics
get_statistics() {
    log "Getting backup statistics..."
    
    if check_backend; then
        local response
        if response=$(call_backup_api "/statistics" "GET"); then
            echo "$response" | python3 -m json.tool 2>/dev/null || echo "$response"
        else
            log_error "Failed to get backup statistics"
            return 1
        fi
    else
        log_error "Cannot get statistics: backend not available"
        return 1
    fi
}

# Clean up old backups
cleanup_backups() {
    log "Cleaning up old backup files..."
    
    local retention_days="${BACKUP_RETENTION_DAYS:-30}"
    local deleted_count=0
    
    # Clean up database backups
    if [ -d "$BACKUP_DIR/database" ]; then
        find "$BACKUP_DIR/database" -type f -mtime +$retention_days -exec rm {} \; -print | while read -r file; do
            log_info "Deleted old database backup: $(basename "$file")"
            ((deleted_count++))
        done
    fi
    
    # Clean up file backups
    if [ -d "$BACKUP_DIR/files" ]; then
        find "$BACKUP_DIR/files" -type f -mtime +$retention_days -exec rm {} \; -print | while read -r file; do
            log_info "Deleted old file backup: $(basename "$file")"
            ((deleted_count++))
        done
    fi
    
    # Clean up config backups
    if [ -d "$BACKUP_DIR/config" ]; then
        find "$BACKUP_DIR/config" -type d -mtime +$retention_days -exec rm -rf {} \; -print | while read -r dir; do
            log_info "Deleted old config backup: $(basename "$dir")"
            ((deleted_count++))
        done
    fi
    
    # Clean up old log files
    if [ -f "$LOG_FILE" ]; then
        # Keep only last 100MB of logs
        tail -c 100M "$LOG_FILE" > "${LOG_FILE}.tmp" && mv "${LOG_FILE}.tmp" "$LOG_FILE"
    fi
    
    log_info "Cleanup completed"
}

# Restore from backup
restore_backup() {
    log_warn "Restore functionality is not yet implemented"
    log_warn "For manual restore, please refer to the backup files in $BACKUP_DIR"
    log_warn "Database backups: $BACKUP_DIR/database/"
    log_warn "File backups: $BACKUP_DIR/files/"
    log_warn "Config backups: $BACKUP_DIR/config/"
}

# Show usage information
show_help() {
    cat << EOF
YSC LMS Backup Script

USAGE:
    $0 [OPTION]

OPTIONS:
    --full              Perform full backup (default)
    --database          Database backup only
    --incremental       Incremental database backup
    --files             File backup only
    --config            Configuration backup only
    --health            Check backup system health
    --statistics        Get backup statistics
    --cleanup           Clean up old backups
    --restore           Restore from backup (not implemented)
    --help              Show this help message

ENVIRONMENT VARIABLES:
    BACKUP_DIR          Backup directory (default: ./backups)
    LOG_FILE            Log file location (default: \$BACKUP_DIR/backup.log)
    API_BASE_URL        Backend API URL (default: http://localhost:8081/api)
    ADMIN_TOKEN         Admin authentication token
    BACKUP_RETENTION_DAYS Backup retention period (default: 30)

EXAMPLES:
    $0 --full                    # Perform full backup
    $0 --database               # Database backup only
    $0 --incremental            # Incremental database backup
    $0 --cleanup                # Clean up old backups
    $0 --health                 # Check backup system health

For more information, see the documentation at:
https://github.com/your-org/ycs-lms/wiki/Backup-Guide
EOF
}

# Main function
main() {
    # Initialize backup environment
    init_backup
    
    log "YSC LMS Backup Script started with arguments: $*"
    
    # Parse command line arguments
    case "${1:---full}" in
        --full)
            backup_full
            ;;
        --database)
            backup_database false
            ;;
        --incremental)
            backup_database true
            ;;
        --files)
            backup_files
            ;;
        --config)
            backup_config
            ;;
        --health)
            check_health
            ;;
        --statistics)
            get_statistics
            ;;
        --cleanup)
            cleanup_backups
            ;;
        --restore)
            restore_backup
            ;;
        --help|-h)
            show_help
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
    
    log "Backup script completed"
}

# Run main function with all arguments
main "$@"