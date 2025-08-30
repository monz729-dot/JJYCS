# YSC LMS Backup and Disaster Recovery Guide

## Table of Contents

1. [Overview](#overview)
2. [Backup Strategy](#backup-strategy)
3. [Backup Components](#backup-components)
4. [Configuration](#configuration)
5. [Automated Backups](#automated-backups)
6. [Manual Backups](#manual-backups)
7. [Monitoring and Alerting](#monitoring-and-alerting)
8. [Disaster Recovery](#disaster-recovery)
9. [Testing and Validation](#testing-and-validation)
10. [Troubleshooting](#troubleshooting)

## Overview

The YSC LMS backup and disaster recovery system provides comprehensive data protection through multiple layers of backup strategies, automated scheduling, and robust recovery procedures. The system is designed to meet business continuity requirements while maintaining data integrity and minimizing downtime.

### Key Features

- **Multi-tier backup strategy**: Full, incremental, and differential backups
- **Multiple data types**: Database, files, configuration, and audit logs
- **Automated scheduling**: Configurable backup windows and retention policies
- **Cloud integration**: S3, Azure Blob, and other cloud storage providers
- **Encryption and compression**: Data protection in transit and at rest
- **Monitoring and alerting**: Real-time backup health monitoring
- **API-driven operations**: RESTful API for backup management

## Backup Strategy

### 3-2-1 Backup Rule

The YSC LMS follows the industry-standard 3-2-1 backup rule:

- **3 copies** of critical data (1 primary + 2 backups)
- **2 different storage media** (local and cloud)
- **1 offsite backup** (cloud storage or remote location)

### Backup Types

| Type | Frequency | Retention | Description |
|------|-----------|-----------|-------------|
| **Full Database** | Weekly (Sunday 3 AM) | 12 weeks | Complete database dump |
| **Incremental Database** | Daily (2 AM) | 7 days | Changes since last backup |
| **File Backup** | Daily (2:30 AM) | 90 days | Application files and uploads |
| **Configuration** | Daily (1 AM) | 30 days | Config files and settings |
| **Audit Archive** | Daily (4 AM) | 7 years | Compliance audit logs |

### Recovery Time Objectives (RTO)

- **Critical Systems**: 4 hours
- **Database Recovery**: 2 hours
- **Full System Recovery**: 8 hours
- **File Recovery**: 1 hour

### Recovery Point Objectives (RPO)

- **Database**: 1 hour (incremental backups)
- **Files**: 24 hours
- **Configuration**: 24 hours
- **Audit Logs**: Real-time archival

## Backup Components

### 1. Database Backup Service

**Location**: `com.ycs.lms.service.backup.DatabaseBackupService`

Features:
- MySQL dump with compression and encryption
- Incremental backups using binary logs
- Read replica support for backup operations
- Automated cleanup based on retention policies

```yaml
ycs:
  backup:
    database:
      enabled: true
      type: "mysql"
      full-backup-schedule: "0 0 3 * * SUN"
      incremental-backup-schedule: "0 0 2 * * MON-SAT"
      retention:
        daily: 7
        weekly: 4
        monthly: 12
        yearly: 5
```

### 2. File Backup Service

**Location**: `com.ycs.lms.service.backup.FileBackupService`

Features:
- ZIP compression with pattern-based exclusions
- S3 synchronization
- Directory structure preservation
- Incremental file tracking

```yaml
ycs:
  backup:
    files:
      enabled: true
      schedule: "0 30 2 * * ?"
      source-directories:
        - "/app/uploads"
        - "/app/logs"
        - "/app/config"
      exclude-patterns:
        - "*.tmp"
        - "*.log.gz"
        - "cache/*"
```

### 3. Configuration Backup Service

**Location**: `com.ycs.lms.service.backup.ConfigBackupService`

Features:
- Application configuration files
- Git repository integration
- Docker and Kubernetes configurations
- Environment variable backup

### 4. Audit Archival Service

**Location**: `com.ycs.lms.service.backup.AuditArchivalService`

Features:
- Compliance-grade audit log archival
- JSONL format for easy parsing
- Configurable retention (7 years default)
- Cloud storage integration

## Configuration

### Environment Variables

```bash
# Backup Storage
BACKUP_S3_BUCKET=ycs-lms-backups
FILES_BACKUP_S3_BUCKET=ycs-lms-file-backups
AUDIT_ARCHIVE_LOCATION=s3://ycs-lms-audit-archives/

# Encryption
BACKUP_ENCRYPTION_KEY=your-256-bit-encryption-key

# Git Configuration Backup
CONFIG_BACKUP_REPO=git@github.com:your-org/ycs-config-backup.git

# Notifications
BACKUP_SLACK_WEBHOOK=https://hooks.slack.com/services/...

# AWS Credentials
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
```

### Application Configuration

**File**: `application-backup.yml`

```yaml
spring:
  profiles: backup

ycs:
  backup:
    database:
      enabled: true
      full-backup-schedule: "0 0 3 * * SUN"
      incremental-backup-schedule: "0 0 2 * * MON-SAT"
      compression: gzip
      encryption:
        enabled: true
        algorithm: "AES-256-CBC"
    
    files:
      enabled: true
      schedule: "0 30 2 * * ?"
      retention-days: 90
      sync-to-s3: true
    
    monitoring:
      enabled: true
      notification:
        email:
          enabled: true
          recipients: ["admin@ycs.com"]
        slack:
          enabled: true
          channel: "#ops"
```

## Automated Backups

### Scheduling

Backups are scheduled using Spring's `@Scheduled` annotation:

```java
// Full backup - Weekly on Sundays at 3 AM
@Scheduled(cron = "${ycs.backup.database.full-backup-schedule:0 0 3 * * SUN}")
public void scheduledFullBackup() {
    // Implementation
}

// Incremental backup - Daily Monday-Saturday at 2 AM  
@Scheduled(cron = "${ycs.backup.database.incremental-backup-schedule:0 0 2 * * MON-SAT}")
public void scheduledIncrementalBackup() {
    // Implementation
}
```

### Docker Deployment

Use the backup-enabled Docker Compose configuration:

```bash
# Start with backup services
docker-compose -f docker/docker-compose.backup.yml up -d

# Check backup container logs
docker logs ycs-lms-backup-scheduler

# Manual backup trigger
docker exec ycs-lms-backup-scheduler /app/scripts/backup.sh --full
```

### Kubernetes CronJobs

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: ycs-lms-backup
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: ycs-lms-backup:latest
            command:
            - /app/scripts/backup.sh
            - --full
            env:
            - name: API_BASE_URL
              value: "http://ycs-lms-backend:8081/api"
```

## Manual Backups

### Using the Backup Script

The comprehensive backup script provides various backup options:

```bash
# Full system backup
./scripts/backup.sh --full

# Database only
./scripts/backup.sh --database

# Incremental database backup
./scripts/backup.sh --incremental

# Files only
./scripts/backup.sh --files

# Configuration only
./scripts/backup.sh --config

# Check system health
./scripts/backup.sh --health

# Clean up old backups
./scripts/backup.sh --cleanup
```

### Using the REST API

```bash
# Get admin token
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yadmin@ycs.com","password":"YSCAdmin2024!"}' \
  | jq -r '.token')

# Trigger full backup
curl -X POST http://localhost:8081/api/backup/full \
  -H "Authorization: Bearer $TOKEN"

# Check backup health
curl -X GET http://localhost:8081/api/backup/health \
  -H "Authorization: Bearer $TOKEN" | jq

# Get backup statistics
curl -X GET http://localhost:8081/api/backup/statistics \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Manual Database Backup

```bash
# MySQL dump with compression
mysqldump -h localhost -u ycs_user -p \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  ycs_lms | gzip > ycs_lms_backup_$(date +%Y%m%d_%H%M%S).sql.gz

# Verify backup
gunzip -t ycs_lms_backup_*.sql.gz
```

## Monitoring and Alerting

### Health Endpoints

```bash
# Application health (includes backup status)
GET /actuator/health

# Backup-specific health
GET /api/backup/health

# Backup statistics
GET /api/backup/statistics
```

### Grafana Dashboard

The system includes pre-configured Grafana dashboards for monitoring:

- **Backup Success Rate**: Percentage of successful backups
- **Backup Duration**: Time taken for each backup type
- **Storage Usage**: Backup storage consumption over time
- **Recovery Testing**: RTO/RPO metrics

### Alerts Configuration

**Prometheus Alerts** (`monitoring/alerts.yml`):

```yaml
groups:
- name: backup.rules
  rules:
  - alert: BackupFailed
    expr: backup_success_total{job="ycs-lms"} == 0
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "Backup failed for {{ $labels.backup_type }}"
      
  - alert: BackupDurationHigh
    expr: backup_duration_seconds > 3600
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "Backup taking too long"
```

### Email Notifications

```yaml
ycs:
  backup:
    monitoring:
      notification:
        email:
          enabled: true
          recipients: ["admin@ycs.com", "ops@ycs.com"]
          on-failure: true
          on-success: false  # Only on failures
```

### Slack Integration

```yaml
ycs:
  backup:
    monitoring:
      notification:
        slack:
          enabled: true
          webhook-url: "${BACKUP_SLACK_WEBHOOK}"
          channel: "#ops"
```

## Disaster Recovery

### Scenario 1: Database Corruption

**Steps:**

1. **Stop the application**:
   ```bash
   docker-compose stop backend
   ```

2. **Identify the latest backup**:
   ```bash
   ls -la backups/database/ | grep -E "\\.sql\\.gz$" | tail -1
   ```

3. **Restore the database**:
   ```bash
   # Drop and recreate database
   mysql -u root -p -e "DROP DATABASE ycs_lms; CREATE DATABASE ycs_lms;"
   
   # Restore from backup
   gunzip -c backups/database/ycs_lms_20240824_020000.sql.gz | \
     mysql -u root -p ycs_lms
   ```

4. **Restart the application**:
   ```bash
   docker-compose start backend
   ```

5. **Verify data integrity**:
   ```bash
   curl -f http://localhost:8081/actuator/health
   ```

### Scenario 2: Complete System Failure

**Steps:**

1. **Provision new infrastructure**
2. **Restore configuration files**
3. **Restore database from latest backup**
4. **Restore application files**
5. **Update DNS/load balancer**
6. **Verify system functionality**

### Scenario 3: File System Corruption

**Steps:**

1. **Mount backup volume**:
   ```bash
   docker run --rm -v backup_data:/backup ubuntu ls -la /backup/files/
   ```

2. **Extract latest file backup**:
   ```bash
   cd /app
   tar -xzf /backup/files/files_backup_20240824_023000.tar.gz
   ```

3. **Verify file integrity**
4. **Restart affected services**

## Testing and Validation

### Backup Validation Tests

**Daily Validation Script** (`scripts/validate-backups.sh`):

```bash
#!/bin/bash
# Validate backup integrity

# Test database backup
echo "Testing database backup..."
gunzip -t /backup/database/latest.sql.gz
mysql --batch --skip-column-names -e "SELECT 'OK'" < /backup/database/latest.sql.gz

# Test file backup
echo "Testing file backup..."
tar -tzf /backup/files/latest.tar.gz > /dev/null

# Test configuration backup
echo "Testing configuration backup..."
yaml-lint /backup/config/latest/application.yml
```

### Recovery Testing Schedule

| Test Type | Frequency | Scope |
|-----------|-----------|--------|
| **Database Point-in-Time Recovery** | Weekly | Last 24 hours |
| **File Recovery** | Weekly | Random file selection |
| **Full System Recovery** | Monthly | Complete DR scenario |
| **Configuration Recovery** | Monthly | Config-only restore |

### Recovery Time Testing

```bash
# Measure database recovery time
time {
  mysql -u root -p -e "DROP DATABASE ycs_lms_test; CREATE DATABASE ycs_lms_test;"
  gunzip -c latest_backup.sql.gz | mysql -u root -p ycs_lms_test
}

# Expected: < 2 hours for full database
# Expected: < 30 minutes for incremental
```

## Troubleshooting

### Common Issues

#### 1. Backup Script Fails

**Symptoms**: Script exits with error code 1

**Debug Steps**:
```bash
# Check log file
tail -f /app/backups/backup.log

# Test API connectivity
curl -f http://localhost:8081/api/health

# Check permissions
ls -la /app/backups/
```

#### 2. S3 Upload Fails

**Symptoms**: "Access Denied" or timeout errors

**Debug Steps**:
```bash
# Test AWS credentials
aws s3 ls s3://your-backup-bucket/

# Check IAM permissions
aws iam get-user
aws iam list-attached-user-policies --user-name backup-user
```

#### 3. Database Backup Hangs

**Symptoms**: mysqldump process stuck

**Debug Steps**:
```bash
# Check database locks
mysql -e "SHOW PROCESSLIST;"

# Check available space
df -h /app/backups/

# Monitor backup progress
tail -f /var/log/mysql/mysql-slow.log
```

#### 4. Email Notifications Not Sent

**Symptoms**: No backup status emails received

**Debug Steps**:
```bash
# Check SMTP configuration
telnet smtp.gmail.com 587

# Test email service
curl -X POST http://localhost:8081/api/test/email \
  -H "Authorization: Bearer $TOKEN"
```

### Log Analysis

**Key Log Locations**:
- Application logs: `/app/logs/application.log`
- Backup logs: `/app/backups/backup.log`
- MySQL logs: `/var/log/mysql/error.log`
- Docker logs: `docker logs ycs-lms-backend`

**Important Log Patterns**:
```bash
# Successful backups
grep "Backup.*completed successfully" /app/backups/backup.log

# Failed backups
grep "ERROR.*backup" /app/logs/application.log

# Performance issues
grep "Backup.*duration.*exceeded" /app/logs/application.log
```

### Performance Tuning

#### Database Backup Optimization

```yaml
ycs:
  backup:
    database:
      # Use read replica for backups
      read-replica:
        enabled: true
        url: "jdbc:mysql://mysql-replica:3306/ycs_lms"
      
      # Optimize compression
      compression-level: 6  # Balance between speed and size
      
      # Parallel processing
      parallel-dumps: true
      thread-count: 4
```

#### Storage Optimization

```yaml
ycs:
  backup:
    files:
      # Exclude large temporary files
      exclude-patterns:
        - "*.tmp"
        - "*.log.gz"
        - "cache/*"
        - "node_modules/*"
        - ".git/*"
      
      # Compression settings
      compression-level: 9  # Maximum compression for file backups
```

## Security Considerations

### Encryption at Rest

- All database backups are encrypted using AES-256-CBC
- File backups are encrypted before cloud upload
- Encryption keys stored in secure key management service

### Access Control

- Backup API endpoints require ADMIN role
- S3 buckets use IAM policies with least privilege
- Backup files have restricted file system permissions (640)

### Audit Trail

- All backup operations are logged to audit table
- Failed access attempts are monitored and alerted
- Backup file access is tracked and reported

### Data Retention Compliance

- Audit logs retained for 7 years (configurable)
- PII data in backups subject to GDPR/data protection laws
- Automatic purging of expired backup data

## Contacts and Escalation

### Emergency Contacts

- **Primary DBA**: ops@ycs.com
- **System Administrator**: admin@ycs.com  
- **On-call Engineer**: +1-555-0199

### Escalation Matrix

1. **Level 1**: Automated alerts to ops team
2. **Level 2**: SMS notifications to on-call engineer
3. **Level 3**: Executive escalation for extended outages

---

**Last Updated**: August 24, 2024  
**Document Version**: 1.0  
**Next Review**: September 24, 2024