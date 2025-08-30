# YSC LMS Production Deployment Checklist

## 🎯 Pre-Deployment Checklist

### Environment Setup
- [ ] **Production Server Configuration**
  - [ ] Minimum 4GB RAM, 2 CPU cores
  - [ ] 50GB+ available disk space
  - [ ] Docker & Docker Compose installed
  - [ ] SSL certificates configured
  - [ ] Domain name configured

- [ ] **Database Setup**
  - [ ] MySQL 8.0+ installed and configured
  - [ ] Database user created with appropriate permissions
  - [ ] Database backup location configured
  - [ ] Connection pooling configured

- [ ] **Environment Variables**
  ```bash
  # Core Configuration
  export SPRING_PROFILES_ACTIVE=production,backup
  export DB_URL="jdbc:mysql://mysql:3306/ycs_lms"
  export DB_USERNAME="ycs_user"
  export DB_PASSWORD="secure_password_here"
  
  # Security
  export JWT_SECRET="your_secure_jwt_secret_256_bits"
  export BACKUP_ENCRYPTION_KEY="your_backup_encryption_key"
  
  # Email Configuration
  export SMTP_HOST="smtp.gmail.com"
  export SMTP_PORT="587"
  export SMTP_USER="your_email@gmail.com"
  export SMTP_PASS="your_app_password"
  
  # Backup Storage
  export AWS_ACCESS_KEY_ID="your_aws_access_key"
  export AWS_SECRET_ACCESS_KEY="your_aws_secret_key"
  export BACKUP_S3_BUCKET="ycs-lms-backups"
  export FILES_BACKUP_S3_BUCKET="ycs-lms-file-backups"
  
  # Monitoring
  export BACKUP_SLACK_WEBHOOK="https://hooks.slack.com/services/..."
  ```

### Security Configuration
- [ ] **SSL/TLS Setup**
  - [ ] SSL certificates obtained (Let's Encrypt recommended)
  - [ ] HTTPS enforced for all connections
  - [ ] HTTP to HTTPS redirection configured
  - [ ] HSTS headers configured

- [ ] **Firewall Configuration**
  - [ ] Only necessary ports open (80, 443, 22)
  - [ ] Database ports restricted to application servers only
  - [ ] SSH key-based authentication enabled

- [ ] **Application Security**
  - [ ] JWT secret key generated (256-bit minimum)
  - [ ] Database passwords secure and rotated
  - [ ] Admin default password changed
  - [ ] CORS properly configured
  - [ ] Rate limiting configured

### Monitoring & Logging
- [ ] **Log Management**
  - [ ] Centralized logging configured
  - [ ] Log rotation policies set
  - [ ] Error monitoring alerts configured
  - [ ] Performance monitoring enabled

- [ ] **Backup Monitoring**
  - [ ] Backup success/failure notifications
  - [ ] Storage usage monitoring
  - [ ] Backup integrity testing scheduled
  - [ ] Disaster recovery procedures documented

## 🚀 Deployment Steps

### Step 1: Initial Deployment

1. **Clone Repository**
   ```bash
   git clone https://github.com/your-org/ycs-lms.git
   cd ycs-lms
   ```

2. **Configure Environment**
   ```bash
   cp .env.example .env.production
   # Edit .env.production with production values
   ```

3. **Deploy with Docker Compose**
   ```bash
   docker-compose -f docker/docker-compose.backup.yml up -d
   ```

4. **Initialize Database**
   ```bash
   docker exec ycs-lms-backend java -jar app.jar --spring.profiles.active=production --init-database
   ```

5. **Create Admin User**
   ```bash
   docker exec ycs-lms-backend node scripts/create-admin.js
   ```

### Step 2: Health Verification

1. **System Health Check**
   ```bash
   curl https://your-domain.com/api/health
   ```

2. **Frontend Accessibility**
   ```bash
   curl https://your-domain.com
   ```

3. **Database Connectivity**
   ```bash
   docker exec ycs-lms-mysql mysql -u ycs_user -p -e "SELECT 1"
   ```

4. **Backup System Test**
   ```bash
   curl -X POST https://your-domain.com/api/backup/health \
     -H "Authorization: Bearer $ADMIN_TOKEN"
   ```

### Step 3: Performance Optimization

1. **Database Optimization**
   ```sql
   -- MySQL Configuration
   SET GLOBAL innodb_buffer_pool_size = 1073741824; -- 1GB
   SET GLOBAL max_connections = 1000;
   SET GLOBAL query_cache_size = 268435456; -- 256MB
   ```

2. **Application Tuning**
   ```yaml
   server:
     tomcat:
       threads:
         max: 200
         min-spare: 10
       connection-timeout: 20000
   
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
   ```

3. **Frontend Optimization**
   ```bash
   # Build optimized production bundle
   cd frontend
   npm run build
   ```

## 📊 Post-Deployment Validation

### Functional Testing
- [ ] **User Authentication**
  - [ ] Admin login works
  - [ ] User registration works
  - [ ] Password reset works
  - [ ] Session management works

- [ ] **Core Features**
  - [ ] Order creation with CBM calculation
  - [ ] Warehouse scanning operations
  - [ ] Admin approval workflows
  - [ ] File upload and download
  - [ ] Email notifications

- [ ] **Business Rules**
  - [ ] CBM > 29m³ auto-conversion to AIR
  - [ ] THB > 1500 extra recipient warning
  - [ ] Member code validation
  - [ ] EMS/HS code validation

### Performance Testing
- [ ] **Load Testing**
  ```bash
  # Use Apache Bench or similar
  ab -n 1000 -c 10 https://your-domain.com/api/health
  ```

- [ ] **Database Performance**
  ```sql
  SHOW PROCESSLIST;
  SHOW ENGINE INNODB STATUS;
  ```

- [ ] **Memory Usage**
  ```bash
  docker stats
  ```

### Security Testing
- [ ] **SSL Configuration**
  ```bash
  nmap --script ssl-enum-ciphers -p 443 your-domain.com
  ```

- [ ] **Vulnerability Scanning**
  ```bash
  # Use tools like OWASP ZAP or Nessus
  ```

- [ ] **Penetration Testing**
  - [ ] SQL injection attempts
  - [ ] XSS prevention
  - [ ] CSRF protection
  - [ ] Authentication bypass attempts

## 🔧 Maintenance & Operations

### Daily Operations
- [ ] **Backup Verification**
  - Check backup completion status
  - Verify backup file integrity
  - Monitor storage usage

- [ ] **System Monitoring**
  - Check application logs for errors
  - Monitor system resource usage
  - Verify all services are running

- [ ] **Security Monitoring**
  - Review authentication logs
  - Check for suspicious activities
  - Update security patches

### Weekly Operations
- [ ] **Performance Review**
  - Analyze response time metrics
  - Review database query performance
  - Check for memory leaks

- [ ] **Backup Testing**
  - Test backup restoration procedures
  - Verify backup integrity
  - Test disaster recovery scenarios

- [ ] **Security Updates**
  - Update container images
  - Apply security patches
  - Review access logs

### Monthly Operations
- [ ] **Capacity Planning**
  - Review storage growth
  - Plan for scaling requirements
  - Optimize database indexes

- [ ] **Security Audit**
  - Review user access permissions
  - Audit configuration changes
  - Update security policies

- [ ] **Disaster Recovery Testing**
  - Full system recovery test
  - Database restoration test
  - Documentation updates

## 🚨 Emergency Procedures

### System Down
1. **Immediate Response**
   ```bash
   # Check service status
   docker ps
   docker logs ycs-lms-backend
   docker logs ycs-lms-frontend
   
   # Restart services if needed
   docker-compose restart
   ```

2. **Database Issues**
   ```bash
   # Check database status
   docker exec ycs-lms-mysql mysql -u root -p -e "SHOW STATUS"
   
   # Restore from backup if needed
   ./scripts/backup.sh --restore
   ```

3. **Escalation**
   - Primary: ops@ycs.com
   - Secondary: admin@ycs.com
   - Emergency: +1-555-0199

### Data Corruption
1. **Immediate Actions**
   - Stop affected services
   - Identify scope of corruption
   - Isolate affected data

2. **Recovery Steps**
   - Restore from latest backup
   - Verify data integrity
   - Resume operations

3. **Post-Incident**
   - Document incident
   - Update procedures
   - Prevent recurrence

## 📋 Production Environment Configuration

### Docker Compose Override
```yaml
# docker-compose.production.yml
version: '3.8'
services:
  backend:
    environment:
      - SPRING_PROFILES_ACTIVE=production,backup
    deploy:
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 1G
    restart: always
    
  mysql:
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
    volumes:
      - /opt/ycs-lms/mysql:/var/lib/mysql
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

### Nginx Configuration
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    
    location / {
        proxy_pass http://frontend:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api/ {
        proxy_pass http://backend:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Monitoring Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'ycs-lms'
    static_configs:
      - targets: ['backend:8081']
    metrics_path: /actuator/prometheus
```

## ✅ Final Production Readiness Checklist

### Infrastructure
- [ ] Load balancer configured
- [ ] CDN configured for static assets
- [ ] Database replication setup
- [ ] Backup storage configured
- [ ] Monitoring dashboards setup

### Security
- [ ] WAF configured
- [ ] DDoS protection enabled
- [ ] Security headers configured
- [ ] Regular security scans scheduled
- [ ] Incident response plan ready

### Operations
- [ ] Runbooks created
- [ ] On-call schedule established
- [ ] Alerting rules configured
- [ ] Documentation complete
- [ ] Team training completed

### Compliance
- [ ] Data privacy compliance
- [ ] Audit logging configured
- [ ] Backup retention policies
- [ ] Disaster recovery tested
- [ ] Change management process

---

**Deployment Date**: _______________  
**Deployed By**: _______________  
**Verified By**: _______________  
**Next Review**: _______________