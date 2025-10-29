# Troubleshooting Guide

[![Status](https://img.shields.io/badge/Status-Maintained-green)](#)
[![Last Updated](https://img.shields.io/badge/Last%20Updated-2025--01--07-blue)](#)

Comprehensive troubleshooting guide for the FindYourDreamHouseAI application.

## 📋 Table of Contents

- [Quick Diagnostics](#-quick-diagnostics)
- [Common Issues](#-common-issues)
- [Application Issues](#-application-issues)
- [Database Issues](#-database-issues)
- [AWS Integration Issues](#-aws-integration-issues)
- [Security Issues](#-security-issues)
- [Performance Issues](#-performance-issues)
- [Deployment Issues](#-deployment-issues)
- [Debug Tools](#-debug-tools)
- [Emergency Procedures](#-emergency-procedures)

## 🚨 Quick Diagnostics

### Health Check Commands

```bash
# Application health
curl -f http://localhost:8080/actuator/health

# Detailed health information
curl http://localhost:8080/actuator/health | jq

# Application info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics
```

### Quick Status Check

```bash
# Check if application is running
ps aux | grep java

# Check port usage
netstat -tlnp | grep 8080

# Check disk space
df -h

# Check memory usage
free -h

# Check system resources
top -p $(pgrep java)
```

## 🔧 Common Issues

### Issue 1: Application Won't Start

**Symptoms:**
- Application fails to start
- No response on port 8080
- Error messages in logs

**Diagnosis:**
```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check application logs
tail -f logs/application.log

# Check for port conflicts
lsof -i :8080
```

**Solutions:**

1. **Java Version Mismatch:**
   ```bash
   # Ensure Java 22 is installed
   export JAVA_HOME=/path/to/java22
   java -version
   ```

2. **Port Already in Use:**
   ```bash
   # Kill process using port 8080
   sudo kill -9 $(lsof -t -i:8080)
   
   # Or change port in application.yaml
   server:
     port: 8081
   ```

3. **Memory Issues:**
   ```bash
   # Increase heap size
   export JAVA_OPTS="-Xms512m -Xmx2g"
   java $JAVA_OPTS -jar app.jar
   ```

4. **Configuration Issues:**
   ```bash
   # Check configuration
   mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
   ```

### Issue 2: Database Connection Failed

**Symptoms:**
- `Connection refused` errors
- `Database not found` errors
- `Authentication failed` errors

**Diagnosis:**
```bash
# Test database connection
psql -h localhost -U admin -d findyourdreamhouse

# Check PostgreSQL status
systemctl status postgresql

# Check database logs
tail -f /var/log/postgresql/postgresql-15-main.log
```

**Solutions:**

1. **PostgreSQL Not Running:**
   ```bash
   # Start PostgreSQL
   sudo systemctl start postgresql
   sudo systemctl enable postgresql
   ```

2. **Database Doesn't Exist:**
   ```sql
   -- Connect as postgres user
   sudo -u postgres psql
   
   -- Create database
   CREATE DATABASE findyourdreamhouse;
   
   -- Create user
   CREATE USER admin WITH PASSWORD 'admin';
   
   -- Grant permissions
   GRANT ALL PRIVILEGES ON DATABASE findyourdreamhouse TO admin;
   ```

3. **Connection String Issues:**
   ```yaml
   # Check application.yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/findyourdreamhouse
       username: admin
       password: admin
   ```

4. **Network Issues:**
   ```bash
   # Test network connectivity
   telnet localhost 5432
   
   # Check firewall
   sudo ufw status
   sudo ufw allow 5432
   ```

### Issue 3: AWS Integration Problems

**Symptoms:**
- S3 upload failures
- Secrets Manager access denied
- AWS credentials not found

**Diagnosis:**
```bash
# Check AWS credentials
aws sts get-caller-identity

# Check AWS configuration
aws configure list

# Test S3 access
aws s3 ls s3://your-bucket-name

# Check secrets
aws secretsmanager list-secrets
```

**Solutions:**

1. **Missing AWS Credentials:**
   ```bash
   # Configure AWS CLI
   aws configure
   
   # Or set environment variables
   export AWS_ACCESS_KEY_ID=your-access-key
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   export AWS_DEFAULT_REGION=us-east-1
   ```

2. **S3 Bucket Issues:**
   ```bash
   # Create bucket if it doesn't exist
   aws s3 mb s3://your-bucket-name
   
   # Check bucket permissions
   aws s3api get-bucket-policy --bucket your-bucket-name
   ```

3. **Secrets Manager Issues:**
   ```bash
   # Create required secrets
   aws secretsmanager create-secret \
     --name jwt-secret \
     --secret-string "$(openssl rand -base64 32)"
   
   aws secretsmanager create-secret \
     --name region \
     --secret-string "us-east-1"
   
   aws secretsmanager create-secret \
     --name bucket_name \
     --secret-string "your-bucket-name"
   ```

4. **IAM Permissions:**
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Action": [
           "s3:GetObject",
           "s3:PutObject",
           "s3:DeleteObject"
         ],
         "Resource": "arn:aws:s3:::your-bucket-name/*"
       },
       {
         "Effect": "Allow",
         "Action": [
           "secretsmanager:GetSecretValue"
         ],
         "Resource": "arn:aws:secretsmanager:*:*:secret:jwt-secret*"
       }
     ]
   }
   ```

## 🏥 Application Issues

### Issue 4: JWT Token Problems

**Symptoms:**
- 401 Unauthorized errors
- Token validation failures
- Login not working

**Diagnosis:**
```bash
# Check JWT secret
aws secretsmanager get-secret-value --secret-id jwt-secret

# Test login endpoint
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

**Solutions:**

1. **Invalid JWT Secret:**
   ```bash
   # Generate new secret
   openssl rand -base64 32
   
   # Update secret in AWS
   aws secretsmanager update-secret \
     --secret-id jwt-secret \
     --secret-string "new-base64-secret"
   ```

2. **Token Format Issues:**
   ```bash
   # Ensure proper Authorization header
   curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/user/123
   ```

3. **Token Expiration:**
   ```java
   // Check token expiration in application logs
   // Adjust expiration time in SecurityConfiguration
   ```

### Issue 5: File Upload Failures

**Symptoms:**
- Image upload errors
- S3 upload failures
- File size errors

**Diagnosis:**
```bash
# Check file size limits
curl -X POST http://localhost:8080/api/v1/houseAds/123/images \
  -H "Authorization: Bearer <token>" \
  -F "files=@large-image.jpg"

# Check S3 connectivity
aws s3 ls s3://your-bucket-name
```

**Solutions:**

1. **File Size Limits:**
   ```yaml
   # Increase file size limits in application.yaml
   spring:
     servlet:
       multipart:
         max-file-size: 10MB
         max-request-size: 10MB
   ```

2. **S3 Upload Issues:**
   ```bash
   # Check S3 bucket permissions
   aws s3api get-bucket-policy --bucket your-bucket-name
   
   # Test S3 upload manually
   aws s3 cp test-file.txt s3://your-bucket-name/
   ```

3. **Content Type Issues:**
   ```bash
   # Ensure proper content type
   curl -X POST http://localhost:8080/api/v1/houseAds/123/images \
     -H "Authorization: Bearer <token>" \
     -F "files=@image.jpg;type=image/jpeg"
   ```

## 🗄️ Database Issues

### Issue 6: Database Performance

**Symptoms:**
- Slow queries
- Connection timeouts
- High CPU usage

**Diagnosis:**
```sql
-- Check active connections
SELECT * FROM pg_stat_activity;

-- Check slow queries
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- Check database size
SELECT pg_size_pretty(pg_database_size('findyourdreamhouse'));
```

**Solutions:**

1. **Connection Pool Tuning:**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
         connection-timeout: 30000
         idle-timeout: 600000
   ```

2. **Query Optimization:**
   ```sql
   -- Add indexes for frequently queried columns
   CREATE INDEX idx_users_username ON users(username);
   CREATE INDEX idx_house_ads_created_at ON house_ads(created_at);
   ```

3. **Database Maintenance:**
   ```sql
   -- Analyze tables for better query planning
   ANALYZE users;
   ANALYZE house_ads;
   
   -- Vacuum to reclaim space
   VACUUM ANALYZE;
   ```

### Issue 7: Database Schema Issues

**Symptoms:**
- Table not found errors
- Column not found errors
- Migration failures

**Diagnosis:**
```sql
-- Check if tables exist
\dt

-- Check table structure
\d users

-- Check migration status
SELECT * FROM flyway_schema_history;
```

**Solutions:**

1. **Missing Tables:**
   ```bash
   # Run database migrations
   mvn flyway:migrate
   
   # Or let Hibernate create tables
   spring:
     jpa:
       hibernate:
         ddl-auto: update
   ```

2. **Schema Mismatch:**
   ```bash
   # Reset database schema
   mvn flyway:clean
   mvn flyway:migrate
   ```

3. **Column Issues:**
   ```sql
   -- Add missing columns
   ALTER TABLE users ADD COLUMN email VARCHAR(100);
   
   -- Update column types
   ALTER TABLE users ALTER COLUMN created_at TYPE TIMESTAMP;
   ```

## ☁️ AWS Integration Issues

### Issue 8: S3 Access Problems

**Symptoms:**
- S3 access denied errors
- Presigned URL generation failures
- Image not found errors

**Diagnosis:**
```bash
# Test S3 access
aws s3 ls s3://your-bucket-name

# Check bucket policy
aws s3api get-bucket-policy --bucket your-bucket-name

# Test presigned URL generation
aws s3 presign s3://your-bucket-name/test-file.txt --expires-in 3600
```

**Solutions:**

1. **Bucket Policy Issues:**
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "AllowAppAccess",
         "Effect": "Allow",
         "Principal": {
           "AWS": "arn:aws:iam::ACCOUNT:user/your-user"
         },
         "Action": [
           "s3:GetObject",
           "s3:PutObject",
           "s3:DeleteObject"
         ],
         "Resource": "arn:aws:s3:::your-bucket-name/*"
       }
     ]
   }
   ```

2. **Region Mismatch:**
   ```bash
   # Ensure region consistency
   aws configure set region us-east-1
   
   # Check bucket region
   aws s3api get-bucket-location --bucket your-bucket-name
   ```

3. **CORS Configuration:**
   ```json
   {
     "CORSRules": [
       {
         "AllowedHeaders": ["*"],
         "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
         "AllowedOrigins": ["*"],
         "ExposeHeaders": ["ETag"],
         "MaxAgeSeconds": 3000
       }
     ]
   }
   ```

### Issue 9: Secrets Manager Issues

**Symptoms:**
- Secret not found errors
- Access denied to secrets
- Configuration loading failures

**Diagnosis:**
```bash
# List all secrets
aws secretsmanager list-secrets

# Get specific secret
aws secretsmanager get-secret-value --secret-id jwt-secret

# Check secret permissions
aws iam get-role-policy --role-name your-role --policy-name SecretAccess
```

**Solutions:**

1. **Missing Secrets:**
   ```bash
   # Create required secrets
   aws secretsmanager create-secret \
     --name jwt-secret \
     --secret-string "$(openssl rand -base64 32)"
   ```

2. **Permission Issues:**
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Action": [
           "secretsmanager:GetSecretValue"
         ],
         "Resource": "arn:aws:secretsmanager:*:*:secret:jwt-secret*"
       }
     ]
   }
   ```

3. **Secret Format Issues:**
   ```bash
   # Ensure proper JSON format for complex secrets
   aws secretsmanager update-secret \
     --secret-id database-config \
     --secret-string '{"url":"jdbc:postgresql://localhost:5432/db","username":"user","password":"pass"}'
   ```

## 🔒 Security Issues

### Issue 10: Authentication Failures

**Symptoms:**
- Login not working
- Token validation errors
- Permission denied errors

**Diagnosis:**
```bash
# Test login endpoint
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'

# Check security logs
grep "Authentication" logs/application.log
```

**Solutions:**

1. **Password Encoding Issues:**
   ```java
   // Ensure BCrypt is used for password encoding
   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder(12);
   }
   ```

2. **JWT Configuration:**
   ```yaml
   # Check JWT configuration
   spring:
     security:
       jwt:
         secret: ${JWT_SECRET}
         expiration: 86400000
   ```

3. **CORS Issues:**
   ```java
   // Configure CORS properly
   @Bean
   public CorsConfigurationSource corsConfigurationSource() {
       CorsConfiguration configuration = new CorsConfiguration();
       configuration.setAllowedOriginPatterns(Arrays.asList("*"));
       configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
       configuration.setAllowedHeaders(Arrays.asList("*"));
       return source;
   }
   ```

### Issue 11: Authorization Problems

**Symptoms:**
- 403 Forbidden errors
- Role-based access not working
- Permission denied for valid users

**Diagnosis:**
```bash
# Check user roles
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/user/123

# Check security configuration
grep "hasRole\|hasAuthority" src/main/java/com/dreamhouse/ai/authentication/configuration/
```

**Solutions:**

1. **Role Assignment Issues:**
   ```sql
   -- Check user roles
   SELECT u.username, r.role_name 
   FROM users u 
   JOIN user_roles ur ON u.user_uid = ur.user_uid 
   JOIN roles r ON ur.role_id = r.role_id;
   ```

2. **Security Configuration:**
   ```java
   // Ensure proper role-based access control
   .authorizeHttpRequests(auth -> auth
       .requestMatchers("/api/v1/houseAds/create").hasRole("USER")
       .requestMatchers("/api/v1/auth/authority/edit").hasRole("ADMIN")
   )
   ```

## 🤖 AI Service Issues

### Qwen Model Connection Problems

**Symptoms:**
- AI search endpoints returning 500 errors
- "Connection refused" errors in logs
- Timeout errors when calling AI services

**Diagnostics:**
```bash
# Check if Qwen service is running
curl http://localhost:11434/api/tags

# Verify model availability
curl http://localhost:11434/api/show -d '{"name": "qwen-vl-plus"}'

# Test embedding endpoint
curl http://localhost:11434/api/embeddings -d '{"model": "qwen-turbo", "input": "test"}'
```

**Solutions:**

1. **Qwen Service Not Running:**
   ```bash
   # Start Qwen service (adjust path as needed)
   ./qwen-server --model qwen-vl-plus --port 11434
   
   # Or using Docker
   docker run -p 11434:11434 qwen/qwen-vl-plus
   ```

2. **Model Not Found:**
   ```bash
   # Download required models
   ./qwen-server --model qwen-vl-plus --download
   
   # List available models
   curl http://localhost:11434/api/tags
   ```

3. **Memory Issues:**
   ```bash
   # Check available memory
   free -h
   
   # Monitor memory usage
   htop
   
   # Consider using smaller models or increasing swap
   sudo fallocate -l 8G /swapfile
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile
   ```

4. **Configuration Issues:**
   ```bash
   # Verify environment variables
   echo $LLM_URL
   echo $LLM_MODEL
   echo $LLM_EMBEDDING_MODEL
   
   # Check application logs
   tail -f logs/application.log | grep -i "qwen\|ai\|llm"
   ```

5. **Performance Optimization:**
   ```bash
   # Use GPU acceleration if available
   ./qwen-server --model qwen-vl-plus --gpu-layers 32
   
   # Reduce context length for faster processing
   export LLM_MAX_TOKENS=2048
   ```

## ⚡ Performance Issues

### Issue 12: Slow Response Times

**Symptoms:**
- High response times
- Timeout errors
- Slow database queries

**Diagnosis:**
```bash
# Check application metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Check database performance
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC;
```

**Solutions:**

1. **Database Optimization:**
   ```sql
   -- Add indexes
   CREATE INDEX idx_house_ads_user_id ON house_ads(user_id);
   CREATE INDEX idx_house_ads_created_at ON house_ads(created_at);
   
   -- Analyze tables
   ANALYZE house_ads;
   ```

2. **Connection Pool Tuning:**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
         connection-timeout: 30000
   ```

3. **Caching:**
   ```java
   // Add caching for frequently accessed data
   @Cacheable(value = "users", key = "#userId")
   public UserDTO getUserById(String userId) {
       // Implementation
   }
   ```

### Issue 13: Memory Issues

**Symptoms:**
- OutOfMemoryError
- High memory usage
- Application crashes

**Diagnosis:**
```bash
# Check memory usage
jstat -gc $(pgrep java)

# Check heap dump
jmap -dump:format=b,file=heap.hprof $(pgrep java)
```

**Solutions:**

1. **Increase Heap Size:**
   ```bash
   export JAVA_OPTS="-Xms1g -Xmx4g -XX:+UseG1GC"
   java $JAVA_OPTS -jar app.jar
   ```

2. **Memory Leak Detection:**
   ```bash
   # Use jvisualvm or similar tools
   jvisualvm
   ```

3. **Garbage Collection Tuning:**
   ```bash
   export JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m"
   ```

## 🚀 Deployment Issues

### Issue 14: Docker Build Failures

**Symptoms:**
- Docker build errors
- Image not found
- Container won't start

**Diagnosis:**
```bash
# Check Docker build logs
docker build -t findyourdreamhouse-ai . 2>&1 | tee build.log

# Check Docker daemon
docker info

# Check available space
df -h
```

**Solutions:**

1. **Build Context Issues:**
   ```bash
   # Ensure .dockerignore is properly configured
   cat .dockerignore
   
   # Clean build context
   docker system prune -a
   ```

2. **Multi-stage Build Issues:**
   ```dockerfile
   # Ensure proper multi-stage build
   FROM openjdk:24-jdk-slim as builder
   # ... build steps
   
   FROM openjdk:24-jre-slim
   COPY --from=builder /app/target/app.jar app.jar
   ```

3. **Resource Constraints:**
   ```bash
   # Increase Docker resources
   # In Docker Desktop: Settings → Resources → Memory
   ```

### Issue 15: Kubernetes Deployment Issues

**Symptoms:**
- Pods not starting
- Image pull errors
- Service not accessible

**Diagnosis:**
```bash
# Check pod status
kubectl get pods -n findyourdreamhouse

# Check pod logs
kubectl logs -f deployment/findyourdreamhouse-app -n findyourdreamhouse

# Check events
kubectl get events -n findyourdreamhouse --sort-by='.lastTimestamp'
```

**Solutions:**

1. **Image Pull Issues:**
   ```bash
   # Check image availability
   docker pull your-registry/findyourdreamhouse-ai:latest
   
   # Update image pull secrets
   kubectl create secret docker-registry regcred \
     --docker-server=your-registry.com \
     --docker-username=your-username \
     --docker-password=your-password
   ```

2. **Resource Constraints:**
   ```yaml
   # Update resource limits
   resources:
     requests:
       memory: "1Gi"
       cpu: "500m"
     limits:
       memory: "2Gi"
       cpu: "1000m"
   ```

3. **Service Issues:**
   ```bash
   # Check service endpoints
   kubectl get endpoints findyourdreamhouse-service -n findyourdreamhouse
   
   # Test service connectivity
   kubectl port-forward service/findyourdreamhouse-service 8080:80 -n findyourdreamhouse
   ```

## 🛠️ Debug Tools

### Application Debugging

```bash
# Enable debug logging
export SPRING_PROFILES_ACTIVE=debug

# Check application logs
tail -f logs/application.log | grep ERROR

# Monitor JVM metrics
jstat -gc $(pgrep java) 5s

# Check thread dumps
jstack $(pgrep java) > thread-dump.txt
```

### Database Debugging

```sql
-- Enable query logging
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_min_duration_statement = 1000;
SELECT pg_reload_conf();

-- Check active queries
SELECT pid, now() - pg_stat_activity.query_start AS duration, query 
FROM pg_stat_activity 
WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes';
```

### Network Debugging

```bash
# Check port connectivity
telnet localhost 8080
telnet localhost 5432

# Check DNS resolution
nslookup your-database-host

# Check firewall rules
sudo ufw status
sudo iptables -L
```

## 🚨 Emergency Procedures

### Application Recovery

1. **Rollback to Previous Version:**
   ```bash
   # Docker
   docker run -d --name app-backup your-registry/findyourdreamhouse-ai:previous-version
   
   # Kubernetes
   kubectl rollout undo deployment/findyourdreamhouse-app -n findyourdreamhouse
   ```

2. **Database Recovery:**
   ```bash
   # Restore from backup
   pg_restore -h localhost -U admin -d findyourdreamhouse backup.sql
   
   # Point-in-time recovery
   aws rds restore-db-instance-to-point-in-time \
     --source-db-instance-identifier findyourdreamhouse-db \
     --target-db-instance-identifier findyourdreamhouse-db-restored \
     --restore-time 2025-01-07T10:00:00Z
   ```

3. **Service Restart:**
   ```bash
   # Restart application
   sudo systemctl restart findyourdreamhouse-app
   
   # Or in Kubernetes
   kubectl rollout restart deployment/findyourdreamhouse-app -n findyourdreamhouse
   ```

### Data Recovery

1. **S3 Data Recovery:**
   ```bash
   # Restore from cross-region backup
   aws s3 sync s3://findyourdreamhouse-backup-images s3://findyourdreamhouse-prod-images
   
   # Restore from versioning
   aws s3api list-object-versions --bucket findyourdreamhouse-prod-images
   aws s3api get-object --bucket findyourdreamhouse-prod-images --key image.jpg --version-id version-id image-restored.jpg
   ```

2. **Database Backup:**
   ```bash
   # Create emergency backup
   pg_dump -h localhost -U admin findyourdreamhouse > emergency-backup.sql
   
   # Upload to S3
   aws s3 cp emergency-backup.sql s3://findyourdreamhouse-backups/
   ```

### Contact Information

- **Development Team**: dev-team@company.com
- **DevOps Team**: devops@company.com
- **Emergency Hotline**: +1-555-EMERGENCY
- **Slack Channel**: #findyourdreamhouse-support

---

**Still having issues?** [Open a support ticket](https://github.com/your-org/FindYourDreamHouseAI/issues) with detailed error logs and system information.

