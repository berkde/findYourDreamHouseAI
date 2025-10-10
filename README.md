# FindYourDreamHouseAI 🏠

[![Java](https://img.shields.io/badge/Java-24-007396?logo=java)](./pom.xml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?logo=spring-boot)](./pom.xml)
[![Build](https://img.shields.io/badge/Build-Maven-blue?logo=apache-maven)](./pom.xml)
[![Database](https://img.shields.io/badge/DB-PostgreSQL-336791?logo=postgresql)](#)
[![AWS](https://img.shields.io/badge/AWS-S3%20%7C%20Secrets%20Manager-FF9900?logo=amazon-aws)](#)
[![Security](https://img.shields.io/badge/Security-JWT%20%7C%20BCrypt-green)](#)
[![License](https://img.shields.io/badge/License-MIT-blue)](#)

A modern, secure, and scalable Spring Boot 3 application for real estate management. Built with enterprise-grade security, AWS integration, and a comprehensive REST API for house advertisement management, user authentication, and messaging system.

## 🚀 Quick Start

```bash
# Clone the repository
git clone https://github.com/berkde/findYourDreamHouseAI.git
cd FindYourDreamHouseAI

# Configure AWS Secrets Manager (see Configuration section)
# Start PostgreSQL database

# Build and run
mvn clean package -DskipTests
mvn spring-boot:run
```

**API Documentation**: [Complete API Reference](./docs/API.md)  
**Developer Guide**: [Setup & Development](./docs/DEVELOPER.md)  
**Deployment Guide**: [Production Deployment](./docs/DEPLOYMENT.md)

## ✨ Features

### 🔐 Authentication & Security
- **JWT-based Authentication** with encrypted tokens (A256GCM)
- **Role-based Access Control** (RBAC) with Spring Security 6
- **BCrypt password hashing** for secure user credentials
- **Stateless session management** for scalability
- **CORS configuration** for cross-origin requests

### 🏠 House Advertisement Management
- **CRUD operations** for house listings
- **Image upload and storage** via AWS S3
- **Presigned URLs** for secure image viewing
- **Search functionality** by title and description
- **Rich metadata** support for listings

### 💬 Messaging System
- **Real-time messaging** between users and property owners
- **Message threading** per house advertisement
- **Contact information** management
- **Message history** and retrieval

### ☁️ AWS Integration
- **S3 storage** for image assets with server-side encryption
- **AWS Secrets Manager** for secure configuration management
- **Presigned URLs** for secure file access
- **Regional configuration** support

### 🗄️ Database & Persistence
- **PostgreSQL** with Spring Data JPA
- **Automatic schema management** with Hibernate
- **Connection pooling** with HikariCP
- **Transaction management** and data integrity

## 🏗️ Architecture

### System Overview
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │   Spring Boot   │
│   (React/Vue)   │◄──►│   (Optional)    │◄──►│   Application   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐              │
                       │   PostgreSQL    │◄─────────────┘
                       │   Database      │
                       └─────────────────┘
                                │
                       ┌─────────────────┐
                       │   AWS S3        │
                       │   (Images)      │
                       └─────────────────┘
```

### Module Structure
```
com.dreamhouse.ai/
├── authentication/          # User management & security
│   ├── configuration/       # Security filters & configs
│   ├── controller/          # Auth REST endpoints
│   ├── service/            # User business logic
│   └── model/              # User entities & DTOs
├── house/                  # Property management
│   ├── controller/         # House ad REST endpoints
│   ├── service/           # House business logic
│   └── model/             # House entities & DTOs
└── llm/                   # AI/LLM integration (future)
```

## 🛠️ Technology Stack

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Runtime** | Java | 24 | Application runtime |
| **Framework** | Spring Boot | 3.5.x | Application framework |
| **Security** | Spring Security | 6.5.2 | Authentication & authorization |
| **Database** | PostgreSQL | Latest | Primary data store |
| **ORM** | Spring Data JPA | 3.5.x | Data persistence |
| **Cloud** | AWS SDK | 2.20.54 | S3 & Secrets Manager |
| **JWT** | JJWT | 0.12.6 | Token management |
| **Mapping** | ModelMapper | 2.0.0 | Object mapping |
| **Build** | Maven | 3.9+ | Dependency management |


## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Features](#-features)
- [Architecture](#️-architecture)
- [Technology Stack](#️-technology-stack)
- [Prerequisites](#-prerequisites)
- [Configuration](#-configuration)
- [Installation & Setup](#-installation--setup)
- [API Documentation](#-api-documentation)
- [Security](#-security)
- [Database Schema](#-database-schema)
- [Deployment](#-deployment)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)


## 📋 Prerequisites

Before you begin, ensure you have the following installed and configured:

### Required Software
- **Java 21+** (Java 24 recommended)
- **Maven 3.9+** for dependency management
- **PostgreSQL 12+** database server
- **AWS CLI** (for configuration setup)

### AWS Requirements
- **AWS Account** with appropriate permissions
- **AWS Credentials** configured (via AWS CLI, environment variables, or IAM roles)
- **S3 Bucket** for image storage
- **Secrets Manager** access for configuration

## ⚙️ Configuration

### 1. Database Configuration

The application uses PostgreSQL as the primary database. Update `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/findyourdreamhouse
    username: your_username
    password: your_password
```

### 2. AWS Secrets Manager Setup

Create the following secrets in AWS Secrets Manager:

```bash
# JWT Secret (32-byte Base64 encoded)
aws secretsmanager create-secret \
  --name jwt-secret \
  --secret-string "$(openssl rand -base64 32)"

# AWS Region
aws secretsmanager create-secret \
  --name region \
  --secret-string "us-east-2"

# S3 Bucket Name
aws secretsmanager create-secret \
  --name bucket_name \
  --secret-string "your-dreamhouse-bucket"

# S3 Base Path (optional)
aws secretsmanager create-secret \
  --name basePath \
  --secret-string "prod/"
```

### 3. Environment Variables (Optional)

You can override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/findyourdreamhouse
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export AWS_REGION=us-east-1
```

## 🚀 Installation & Setup

### Step 1: Clone and Setup

  ```bash
# Clone the repository
git clone https://github.com/berkde/findYourDreamHouseAI.git
  cd FindYourDreamHouseAI

# Verify Java version
java -version  # Should be 21+

# Verify Maven installation
mvn -version  # Should be 3.9+
```

### Step 2: Database Setup

```bash
# Start PostgreSQL (using Docker)
docker run --name postgres-db \
  -e POSTGRES_DB=findyourdreamhouse \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -p 5432:5432 \
  -d postgres:15

# Or install PostgreSQL locally and create database
createdb findyourdreamhouse
```

### Step 3: AWS Configuration

```bash
# Configure AWS credentials
aws configure

# Verify AWS access
aws sts get-caller-identity

# Create S3 bucket (if not exists)
aws s3 mb s3://your-dreamhouse-bucket
```

### Step 4: Build and Run

  ```bash
# Clean and build the project
mvn clean package -DskipTests

# Run the application
  mvn spring-boot:run

# Or run the JAR directly
java -jar target/FindYourDreamHouseAI-0.0.1-SNAPSHOT.jar
  ```

### Step 5: Verify Installation

  ```bash
# Test the application health
curl http://localhost:8080/actuator/health

# Test user registration
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass123",
    "name": "Test",
    "lastname": "User"
  }'

# Test login
curl -X POST http://localhost:8080/login \
    -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass123"
  }'
```

## 📚 API Documentation

The complete API documentation is available in [docs/API.md](./docs/API.md).

### Quick API Examples

**Authentication:**
```bash
# Register new user
POST /api/v1/auth/register

# Login
POST /login

# Get user profile
GET /api/v1/user/{userId}
```

**House Management:**
```bash
# List all houses
GET /api/v1/houseAds

# Create house ad
POST /api/v1/houseAds/create

# Upload images
POST /api/v1/houseAds/{houseAdId}/images

# Send message
POST /api/v1/houseAds/message
```


## 🔐 Security

### Authentication Flow

The application uses JWT-based authentication with encrypted tokens:

1. **User Registration/Login** → JWT token generation
2. **Token Storage** → Encrypted and stored per user
3. **Request Authorization** → Token validation on protected endpoints
4. **Role-based Access** → RBAC for fine-grained permissions

### Security Features

- **Encrypted JWT Tokens** (A256GCM encryption)
- **BCrypt Password Hashing** (12 rounds)
- **Stateless Session Management**
- **CORS Configuration** (configurable for production)
- **CSRF Protection** (disabled for API usage)

### Public vs Protected Endpoints

**Public Endpoints:**
- `POST /login` - User authentication
- `POST /api/v1/auth/register` - User registration
- `GET /api/v1/houseAds` - List all house ads
- `GET /api/v1/houseAds/id/{id}` - Get specific house ad
- `GET /api/v1/houseAds/title?title={query}` - Search house ads

**Protected Endpoints:**
- All other endpoints require `Authorization: Bearer <token>`
- Role-based access control (ROLE_USER, ROLE_ADMIN)

## 🗄️ Database Schema

### Entity Relationships

```
UserEntity (1) ──→ (N) HouseAdEntity
    │                    │
    │                    │
    └──→ (N) AddressEntity    └──→ (N) HouseAdImageEntity
    │                           │
    └──→ (N) UserRoleEntity     └──→ (N) HouseAdMessageEntity
            │
            └──→ (1) RoleEntity ──→ (N) AuthorityEntity
```

### Key Entities

**UserEntity:**
- `userUid` (Primary Key)
- `username`, `password` (BCrypt hashed)
- `name`, `lastname`, `email`
- `authorizationToken` (JWT)
- `lastLogin`, `createdAt`, `updatedAt`

**HouseAdEntity:**
- `houseAdUid` (Primary Key)
- `title`, `description`, `price`, `address`
- `user` (Foreign Key to UserEntity)
- `createdAt`, `updatedAt`

**HouseAdImageEntity:**
- `houseAdImageUid` (Primary Key)
- `imageName`, `imageUrl`, `imageType`
- `storageKey` (S3 key)
- `imageDescription`, `imageThumbnail`
- `houseAd` (Foreign Key to HouseAdEntity)

**HouseAdMessageEntity:**
- `messageUid` (Primary Key)
- `senderName`, `senderEmail`, `senderPhone`
- `subject`, `message`, `messageDate`
- `houseAd` (Foreign Key to HouseAdEntity)

## ☁️ AWS Integration

### S3 Storage Flow

```
Client Upload → HouseAdController → HouseAdsService → StorageService → S3 Bucket
     ↓
Image Metadata → Database Storage → Presigned URL Generation
```

### AWS Services Used

- **S3**: Image storage with server-side encryption (AES256)
- **Secrets Manager**: Secure configuration management
- **IAM**: Access control and permissions

### Configuration Management

All sensitive configuration is stored in AWS Secrets Manager:
- `jwt-secret`: JWT encryption key
- `region`: AWS region for services
- `bucket_name`: S3 bucket for image storage
- `basePath`: S3 object key prefix

## 🚨 Error Handling

### HTTP Status Codes

| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful operations |
| 201 | Created | Resource creation |
| 204 | No Content | Successful deletion |
| 400 | Bad Request | Validation errors |
| 401 | Unauthorized | Invalid/missing JWT |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resources |
| 500 | Internal Server Error | System errors |

### Exception Handling

- **Global Exception Handlers** per module
- **Custom Business Exceptions** with meaningful messages
- **Validation Errors** with field-specific details
- **Security Exceptions** for authentication/authorization failures


## 🚀 Deployment

### Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM openjdk:24-jdk-slim
COPY target/FindYourDreamHouseAI-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:

```bash
# Build Docker image
docker build -t findyourdreamhouse-ai .

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/findyourdreamhouse \
  -e AWS_REGION=us-east-1 \
  findyourdreamhouse-ai
```

### Production Configuration

For production deployment, consider:

1. **Environment Variables**: Use environment variables for all sensitive configuration
2. **Database**: Use managed PostgreSQL service (AWS RDS, Google Cloud SQL)
3. **Load Balancing**: Deploy behind a load balancer
4. **Monitoring**: Add application monitoring and logging
5. **Security**: Configure proper CORS, rate limiting, and security headers

## 🔧 Troubleshooting

### Common Issues

**1. Database Connection Issues**
```bash
# Check PostgreSQL status
systemctl status postgresql

# Test connection
psql -h localhost -U admin -d findyourdreamhouse
```

**2. AWS Credentials Issues**
```bash
# Check AWS credentials
aws sts get-caller-identity

# Verify secrets exist
aws secretsmanager list-secrets
```

**3. JWT Token Issues**
- Ensure JWT secret is exactly 32 bytes (Base64 encoded)
- Check token format: `Authorization: Bearer <token>`
- Verify token hasn't expired

**4. S3 Upload Issues**
- Verify S3 bucket exists and is accessible
- Check AWS permissions for S3 operations
- Ensure region configuration matches bucket region

### Debug Mode

Enable debug logging in `application.yaml`:

```yaml
logging:
  level:
    com.dreamhouse.ai: DEBUG
    org.springframework.security: DEBUG
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
curl http://localhost:8080/actuator/health/db

# Disk space
curl http://localhost:8080/actuator/health/diskSpace
```

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Add tests for new functionality
5. Run tests: `mvn test`
6. Commit changes: `git commit -m 'Add amazing feature'`
7. Push to branch: `git push origin feature/amazing-feature`
8. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc for public methods
- Write unit tests for new functionality
- Ensure all tests pass before submitting PR

### Reporting Issues

When reporting issues, please include:
- Java version
- Maven version
- PostgreSQL version
- Error logs
- Steps to reproduce

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- AWS for cloud services
- PostgreSQL community for the database
- All contributors who help improve this project

---

**Need Help?**  [open an issue](https://github.com/berkde/FindYourDreamHouseAI/issues).
