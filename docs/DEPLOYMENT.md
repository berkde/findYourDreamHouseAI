# Deployment Guide

[![Docker](https://img.shields.io/badge/Docker-20.10+-blue?logo=docker)](#)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20ECS%20%7C%20EKS-orange?logo=amazon-aws)](#)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.20+-blue?logo=kubernetes)](#)

Comprehensive deployment guide for the FindYourDreamHouseAI application.

## 📋 Table of Contents

- [Deployment Options](#-deployment-options)
- [Docker Deployment](#-docker-deployment)
- [AWS Deployment](#-aws-deployment)
- [Kubernetes Deployment](#-kubernetes-deployment)
- [Production Configuration](#-production-configuration)
- [Monitoring & Logging](#-monitoring--logging)
- [Security Considerations](#-security-considerations)
- [Backup & Recovery](#-backup--recovery)
- [Troubleshooting](#-troubleshooting)

## 🚀 Deployment Options

### Environment Tiers

| Environment | Purpose | Infrastructure | Database | Storage |
|-------------|---------|----------------|----------|---------|
| **Development** | Local development | Docker Compose | PostgreSQL (Docker) | Local S3 (MinIO) |
| **Staging** | Pre-production testing | AWS ECS/EKS | AWS RDS | AWS S3 |
| **Production** | Live application | AWS ECS/EKS | AWS RDS (Multi-AZ) | AWS S3 |

### Deployment Strategies

1. **Blue-Green Deployment**: Zero-downtime deployments
2. **Rolling Updates**: Gradual replacement of instances
3. **Canary Deployment**: Gradual traffic shifting
4. **Recreate**: Stop all instances, deploy new version

## 🐳 Docker Deployment

### Dockerfile

```dockerfile
# Multi-stage build for optimized image
FROM openjdk:22-jdk-slim as builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM openjdk:22-jre-slim

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/FindYourDreamHouseAI-0.0.1-SNAPSHOT.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/findyourdreamhouse
      - SPRING_DATASOURCE_USERNAME=app_user
      - SPRING_DATASOURCE_PASSWORD=app_password
      - AWS_REGION=us-east-1
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - app-network
    restart: unless-stopped

  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=findyourdreamhouse
      - POSTGRES_USER=app_user
      - POSTGRES_PASSWORD=app_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U app_user -d findyourdreamhouse"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - app-network
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:

networks:
  app-network:
    driver: bridge
```

### Build and Deploy

```bash
# Build Docker image
docker build -t findyourdreamhouse-ai:latest .

# Run with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f app

# Scale application
docker-compose up -d --scale app=3

# Stop services
docker-compose down
```

## ☁️ AWS Deployment

### Prerequisites

1. **AWS CLI Configuration:**
   ```bash
   aws configure
   # Enter Access Key ID, Secret Access Key, Region, Output format
   ```

2. **Required AWS Services:**
   - EC2/ECS/EKS for compute
   - RDS for database
   - S3 for file storage
   - Secrets Manager for configuration
   - CloudWatch for monitoring
   - Application Load Balancer for traffic distribution

### ECS Deployment

#### 1. Create ECS Cluster

```bash
# Create cluster
aws ecs create-cluster --cluster-name findyourdreamhouse-cluster

# Create task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json
```

#### 2. Task Definition

```json
{
  "family": "findyourdreamhouse-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "findyourdreamhouse-app",
      "image": "ACCOUNT.dkr.ecr.REGION.amazonaws.com/findyourdreamhouse-ai:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT:secret:rds-connection-string"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT:secret:jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/findyourdreamhouse",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

#### 3. Create Service

```bash
# Create ECS service
aws ecs create-service \
  --cluster findyourdreamhouse-cluster \
  --service-name findyourdreamhouse-service \
  --task-definition findyourdreamhouse-task:1 \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-12345,subnet-67890],securityGroups=[sg-12345],assignPublicIp=ENABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:REGION:ACCOUNT:targetgroup/findyourdreamhouse-tg,containerName=findyourdreamhouse-app,containerPort=8080"
```

### EKS Deployment

#### 1. Create EKS Cluster

```bash
# Create EKS cluster
eksctl create cluster \
  --name findyourdreamhouse-cluster \
  --region us-east-1 \
  --nodegroup-name workers \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 1 \
  --nodes-max 5 \
  --managed
```

#### 2. Kubernetes Manifests

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: findyourdreamhouse

---
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: findyourdreamhouse-app
  namespace: findyourdreamhouse
spec:
  replicas: 3
  selector:
    matchLabels:
      app: findyourdreamhouse-app
  template:
    metadata:
      labels:
        app: findyourdreamhouse-app
    spec:
      containers:
      - name: findyourdreamhouse-app
        image: ACCOUNT.dkr.ecr.REGION.amazonaws.com/findyourdreamhouse-ai:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database-url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

---
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: findyourdreamhouse-service
  namespace: findyourdreamhouse
spec:
  selector:
    app: findyourdreamhouse-app
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP

---
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: findyourdreamhouse-ingress
  namespace: findyourdreamhouse
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/healthcheck-path: /actuator/health
spec:
  rules:
  - host: api.findyourdreamhouse.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: findyourdreamhouse-service
            port:
              number: 80
```

#### 3. Deploy to EKS

```bash
# Apply manifests
kubectl apply -f namespace.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml

# Check deployment status
kubectl get pods -n findyourdreamhouse
kubectl get services -n findyourdreamhouse
kubectl get ingress -n findyourdreamhouse
```

### RDS Database Setup

#### 1. Create RDS Instance

```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier findyourdreamhouse-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 15.4 \
  --master-username admin \
  --master-user-password YourSecurePassword123 \
  --allocated-storage 20 \
  --vpc-security-group-ids sg-12345 \
  --db-subnet-group-name findyourdreamhouse-subnet-group \
  --backup-retention-period 7 \
  --multi-az \
  --storage-encrypted
```

#### 2. Database Configuration

```sql
-- Create database
CREATE DATABASE findyourdreamhouse;

-- Create application user
CREATE USER app_user WITH PASSWORD 'AppUserPassword123';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE findyourdreamhouse TO app_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_user;
```

### S3 Bucket Setup

```bash
# Create S3 bucket
aws s3 mb s3://findyourdreamhouse-prod-images

# Configure bucket policy
aws s3api put-bucket-policy --bucket findyourdreamhouse-prod-images --policy '{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowAppAccess",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::ACCOUNT:role/ecsTaskRole"
      },
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::findyourdreamhouse-prod-images/*"
    }
  ]
}'

# Enable versioning
aws s3api put-bucket-versioning --bucket findyourdreamhouse-prod-images --versioning-configuration Status=Enabled

# Enable server-side encryption
aws s3api put-bucket-encryption --bucket findyourdreamhouse-prod-images --server-side-encryption-configuration '{
  "Rules": [
    {
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }
  ]
}'
```

## ⚙️ Production Configuration

### Application Properties

```yaml
# application-prod.yaml
spring:
  application:
    name: FindYourDreamHouseAI
  profiles:
    active: prod
  
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400000  # 24 hours

aws:
  region: ${AWS_REGION}
  s3:
    bucket-name: ${S3_BUCKET_NAME}
    base-path: ${S3_BASE_PATH}

logging:
  level:
    com.dreamhouse.ai: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/findyourdreamhouse/application.log

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
```

### Environment Variables

```bash
# Database
export DATABASE_URL="jdbc:postgresql://findyourdreamhouse-db.cluster-xyz.us-east-1.rds.amazonaws.com:5432/findyourdreamhouse"
export DATABASE_USERNAME="app_user"
export DATABASE_PASSWORD="AppUserPassword123"

# JWT
export JWT_SECRET="your-32-byte-base64-encoded-secret"

# AWS
export AWS_REGION="us-east-1"
export S3_BUCKET_NAME="findyourdreamhouse-prod-images"
export S3_BASE_PATH="prod/"

# Application
export SPRING_PROFILES_ACTIVE="prod"
export SERVER_PORT="8080"
```

## 📊 Monitoring & Logging

### CloudWatch Configuration

```yaml
# cloudwatch-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cloudwatch-config
  namespace: findyourdreamhouse
data:
  fluent.conf: |
    <source>
      @type tail
      path /var/log/containers/findyourdreamhouse-app*.log
      pos_file /var/log/fluentd-containers.log.pos
      tag kubernetes.var.log.containers.findyourdreamhouse-app*
      format json
      time_key time
      time_format %Y-%m-%dT%H:%M:%S.%NZ
    </source>
    
    <match kubernetes.var.log.containers.findyourdreamhouse-app*>
      @type cloudwatch_logs
      region us-east-1
      log_group_name /aws/eks/findyourdreamhouse/application
      log_stream_name ${tag}
      auto_create_stream true
    </match>
```

### Prometheus Monitoring

```yaml
# prometheus-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: findyourdreamhouse
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
    
    scrape_configs:
    - job_name: 'findyourdreamhouse-app'
      static_configs:
      - targets: ['findyourdreamhouse-service:8080']
      metrics_path: '/actuator/prometheus'
      scrape_interval: 30s
```

### Health Checks

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Check database connectivity
            boolean dbHealthy = checkDatabaseHealth();
            
            // Check S3 connectivity
            boolean s3Healthy = checkS3Health();
            
            if (dbHealthy && s3Healthy) {
                return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("s3", "UP")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", dbHealthy ? "UP" : "DOWN")
                    .withDetail("s3", s3Healthy ? "UP" : "DOWN")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 🔒 Security Considerations

### Network Security

```yaml
# security-groups.yaml
apiVersion: v1
kind: NetworkPolicy
metadata:
  name: findyourdreamhouse-network-policy
  namespace: findyourdreamhouse
spec:
  podSelector:
    matchLabels:
      app: findyourdreamhouse-app
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to: []
    ports:
    - protocol: TCP
      port: 5432  # PostgreSQL
    - protocol: TCP
      port: 443   # HTTPS
```

### Secrets Management

```bash
# Create Kubernetes secrets
kubectl create secret generic app-secrets \
  --from-literal=database-url="jdbc:postgresql://findyourdreamhouse-db.cluster-xyz.us-east-1.rds.amazonaws.com:5432/findyourdreamhouse" \
  --from-literal=database-username="app_user" \
  --from-literal=database-password="AppUserPassword123" \
  --from-literal=jwt-secret="your-32-byte-base64-encoded-secret" \
  --namespace=findyourdreamhouse

# Create AWS Secrets Manager secrets
aws secretsmanager create-secret \
  --name "findyourdreamhouse/database" \
  --description "Database connection details" \
  --secret-string '{"url":"jdbc:postgresql://findyourdreamhouse-db.cluster-xyz.us-east-1.rds.amazonaws.com:5432/findyourdreamhouse","username":"app_user","password":"AppUserPassword123"}'
```

### SSL/TLS Configuration

```yaml
# tls-certificate.yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: findyourdreamhouse-tls
  namespace: findyourdreamhouse
spec:
  secretName: findyourdreamhouse-tls-secret
  issuerRef:
    name: letsencrypt-prod
    kind: ClusterIssuer
  dnsNames:
  - api.findyourdreamhouse.com
```

## 💾 Backup & Recovery

### Database Backup

```bash
# Automated RDS backup (already configured)
aws rds describe-db-instances --db-instance-identifier findyourdreamhouse-db

# Manual backup
aws rds create-db-snapshot \
  --db-instance-identifier findyourdreamhouse-db \
  --db-snapshot-identifier findyourdreamhouse-manual-backup-$(date +%Y%m%d)
```

### S3 Backup

```bash
# Cross-region replication
aws s3api put-bucket-replication \
  --bucket findyourdreamhouse-prod-images \
  --replication-configuration '{
    "Role": "arn:aws:iam::ACCOUNT:role/replication-role",
    "Rules": [
      {
        "Status": "Enabled",
        "Prefix": "",
        "Destination": {
          "Bucket": "arn:aws:s3:::findyourdreamhouse-backup-images",
          "StorageClass": "STANDARD_IA"
        }
      }
    ]
  }'
```

### Application Backup

```yaml
# backup-cronjob.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: app-backup
  namespace: findyourdreamhouse
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:15-alpine
            command:
            - /bin/sh
            - -c
            - |
              pg_dump $DATABASE_URL > /backup/app-backup-$(date +%Y%m%d).sql
              aws s3 cp /backup/app-backup-$(date +%Y%m%d).sql s3://findyourdreamhouse-backups/
            env:
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: app-secrets
                  key: database-url
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            emptyDir: {}
          restartPolicy: OnFailure
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Application Won't Start

```bash
# Check logs
kubectl logs -f deployment/findyourdreamhouse-app -n findyourdreamhouse

# Check events
kubectl get events -n findyourdreamhouse --sort-by='.lastTimestamp'

# Check pod status
kubectl describe pod <pod-name> -n findyourdreamhouse
```

#### 2. Database Connection Issues

```bash
# Test database connectivity
kubectl run postgres-client --rm -i --tty --restart=Never --image=postgres:15-alpine -- psql -h findyourdreamhouse-db.cluster-xyz.us-east-1.rds.amazonaws.com -U app_user -d findyourdreamhouse

# Check RDS status
aws rds describe-db-instances --db-instance-identifier findyourdreamhouse-db
```

#### 3. S3 Access Issues

```bash
# Test S3 access
kubectl run s3-client --rm -i --tty --restart=Never --image=amazon/aws-cli -- aws s3 ls s3://findyourdreamhouse-prod-images

# Check IAM permissions
aws iam get-role --role-name ecsTaskRole
```

#### 4. Performance Issues

```bash
# Check resource usage
kubectl top pods -n findyourdreamhouse

# Check node resources
kubectl top nodes

# Check application metrics
curl http://findyourdreamhouse-service:8080/actuator/metrics
```

### Debug Commands

```bash
# Get pod shell
kubectl exec -it <pod-name> -n findyourdreamhouse -- /bin/bash

# Port forward for local access
kubectl port-forward service/findyourdreamhouse-service 8080:80 -n findyourdreamhouse

# Check service endpoints
kubectl get endpoints findyourdreamhouse-service -n findyourdreamhouse

# View application logs
kubectl logs -f deployment/findyourdreamhouse-app -n findyourdreamhouse --tail=100
```

### Monitoring Commands

```bash
# Check application health
curl http://findyourdreamhouse-service:8080/actuator/health

# Check metrics
curl http://findyourdreamhouse-service:8080/actuator/metrics

# Check Prometheus metrics
curl http://findyourdreamhouse-service:8080/actuator/prometheus
```

---

**Need Help?** Check out our [Troubleshooting Guide](./TROUBLESHOOTING.md) or [open an issue](https://github.com/your-org/FindYourDreamHouseAI/issues) for deployment support.

