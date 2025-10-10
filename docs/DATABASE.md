# Database Schema Documentation

[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?logo=postgresql)](#)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.0+-green?logo=hibernate)](#)

Comprehensive database schema documentation for the FindYourDreamHouseAI application.

## 📋 Table of Contents

- [Database Overview](#-database-overview)
- [Entity Relationships](#-entity-relationships)
- [Table Definitions](#-table-definitions)
- [Indexes](#-indexes)
- [Constraints](#-constraints)
- [Data Types](#-data-types)
- [Migration Scripts](#-migration-scripts)
- [Performance Optimization](#-performance-optimization)
- [Backup & Recovery](#-backup--recovery)

## 🗄️ Database Overview

### Technology Stack

- **Database**: PostgreSQL 15+
- **ORM**: Hibernate 6.0+ (via Spring Data JPA)
- **Connection Pool**: HikariCP
- **Migration**: Flyway (optional)
- **Schema Management**: Hibernate DDL Auto

### Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/findyourdreamhouse
    username: admin
    password: admin
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

## 🔗 Entity Relationships

### ER Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Users       │    │   House Ads     │    │   Messages      │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ user_uid (PK)   │◄───┤ house_ad_uid    │◄───┤ message_uid     │
│ username        │    │ (PK)            │    │ (PK)            │
│ password        │    │ title           │    │ sender_name     │
│ name            │    │ description     │    │ sender_email    │
│ lastname        │    │ price           │    │ sender_phone    │
│ email           │    │ address         │    │ subject         │
│ auth_token      │    │ user_uid (FK)   │    │ message         │
│ last_login      │    │ created_at      │    │ message_date    │
│ created_at      │    │ updated_at      │    │ house_ad_uid    │
│ updated_at      │    └─────────────────┘    │ (FK)            │
└─────────────────┘           │               │ sent_at         │
         │                    │               │ created_at      │
         │                    │               └─────────────────┘
         │                    │
         │                    ▼
         │           ┌─────────────────┐
         │           │ House Ad Images │
         │           ├─────────────────┤
         │           │ image_uid (PK)  │
         │           │ image_name      │
         │           │ image_url       │
         │           │ image_type      │
         │           │ storage_key     │
         │           │ description     │
         │           │ thumbnail       │
         │           │ house_ad_uid    │
         │           │ (FK)            │
         │           │ created_at      │
         │           └─────────────────┘
         │
         ▼
┌─────────────────┐
│    Addresses    │
├─────────────────┤
│ address_uid (PK)│
│ street          │
│ city            │
│ state           │
│ postal_code     │
│ country         │
│ user_uid (FK)   │
│ created_at      │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  User Roles     │
├─────────────────┤
│ user_uid (FK)   │
│ role_id (FK)    │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│     Roles       │
├─────────────────┤
│ role_id (PK)    │
│ role_name       │
│ description     │
│ created_at      │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  Role Authorities│
├─────────────────┤
│ role_id (FK)    │
│ authority_id (FK)│
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  Authorities    │
├─────────────────┤
│ authority_id (PK)│
│ authority_name  │
│ description     │
│ created_at      │
└─────────────────┘
```

## 📊 Table Definitions

### Users Table

```sql
CREATE TABLE users (
    user_uid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    lastname VARCHAR(100),
    email VARCHAR(100),
    authorization_token TEXT,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### House Ads Table

```sql
CREATE TABLE house_ads (
    house_ad_uid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(15,2),
    address VARCHAR(500),
    user_uid UUID NOT NULL REFERENCES users(user_uid) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_house_ads_user_uid ON house_ads(user_uid);
CREATE INDEX idx_house_ads_created_at ON house_ads(created_at);
CREATE INDEX idx_house_ads_price ON house_ads(price);
CREATE INDEX idx_house_ads_title ON house_ads USING gin(to_tsvector('english', title));

-- Full-text search index
CREATE INDEX idx_house_ads_search ON house_ads USING gin(
    to_tsvector('english', title || ' ' || COALESCE(description, ''))
);

-- Trigger for updated_at
CREATE TRIGGER update_house_ads_updated_at 
    BEFORE UPDATE ON house_ads 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### House Ad Images Table

```sql
CREATE TABLE house_ad_images (
    house_ad_image_uid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    image_name VARCHAR(255) NOT NULL,
    image_url VARCHAR(500),
    image_type VARCHAR(100),
    storage_key VARCHAR(500),
    image_description TEXT,
    image_thumbnail VARCHAR(500),
    house_ad_uid UUID NOT NULL REFERENCES house_ads(house_ad_uid) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_house_ad_images_house_ad_uid ON house_ad_images(house_ad_uid);
CREATE INDEX idx_house_ad_images_storage_key ON house_ad_images(storage_key);
```

### House Ad Messages Table

```sql
CREATE TABLE house_ad_messages (
    message_uid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receiver_house_ad_uid UUID NOT NULL REFERENCES house_ads(house_ad_uid) ON DELETE CASCADE,
    sender_name VARCHAR(100) NOT NULL,
    sender_email VARCHAR(100) NOT NULL,
    sender_phone VARCHAR(20),
    subject VARCHAR(200),
    message TEXT NOT NULL,
    message_date TIMESTAMP NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_house_ad_messages_house_ad_uid ON house_ad_messages(receiver_house_ad_uid);
CREATE INDEX idx_house_ad_messages_sender_email ON house_ad_messages(sender_email);
CREATE INDEX idx_house_ad_messages_sent_at ON house_ad_messages(sent_at);
```

### Addresses Table

```sql
CREATE TABLE addresses (
    address_uid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    street VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL,
    user_uid UUID NOT NULL REFERENCES users(user_uid) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_addresses_user_uid ON addresses(user_uid);
CREATE INDEX idx_addresses_city ON addresses(city);
CREATE INDEX idx_addresses_country ON addresses(country);
```

### Roles and Authorities Tables

```sql
-- Roles table
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Authorities table
CREATE TABLE authorities (
    authority_id SERIAL PRIMARY KEY,
    authority_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles junction table
CREATE TABLE user_roles (
    user_uid UUID NOT NULL REFERENCES users(user_uid) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    PRIMARY KEY (user_uid, role_id)
);

-- Role authorities junction table
CREATE TABLE role_authorities (
    role_id INTEGER NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    authority_id INTEGER NOT NULL REFERENCES authorities(authority_id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, authority_id)
);

-- Indexes
CREATE INDEX idx_user_roles_user_uid ON user_roles(user_uid);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_authorities_role_id ON role_authorities(role_id);
CREATE INDEX idx_role_authorities_authority_id ON role_authorities(authority_id);
```

## 🔍 Indexes

### Performance Indexes

```sql
-- Composite indexes for common queries
CREATE INDEX idx_house_ads_user_created ON house_ads(user_uid, created_at DESC);
CREATE INDEX idx_house_ad_images_house_created ON house_ad_images(house_ad_uid, created_at);
CREATE INDEX idx_messages_house_sent ON house_ad_messages(receiver_house_ad_uid, sent_at DESC);

-- Partial indexes for active records
CREATE INDEX idx_active_users ON users(user_uid) WHERE authorization_token IS NOT NULL;
CREATE INDEX idx_recent_house_ads ON house_ads(house_ad_uid) WHERE created_at > CURRENT_DATE - INTERVAL '30 days';

-- Text search indexes
CREATE INDEX idx_house_ads_title_trgm ON house_ads USING gin(title gin_trgm_ops);
CREATE INDEX idx_house_ads_description_trgm ON house_ads USING gin(description gin_trgm_ops);
```

### Index Maintenance

```sql
-- Analyze tables for better query planning
ANALYZE users;
ANALYZE house_ads;
ANALYZE house_ad_images;
ANALYZE house_ad_messages;
ANALYZE addresses;

-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Rebuild indexes if needed
REINDEX INDEX idx_house_ads_title;
```

## 🔒 Constraints

### Primary Key Constraints

```sql
-- All tables have UUID primary keys except roles and authorities
-- UUIDs are generated using gen_random_uuid() function
```

### Foreign Key Constraints

```sql
-- House ads reference users
ALTER TABLE house_ads 
ADD CONSTRAINT fk_house_ads_user 
FOREIGN KEY (user_uid) REFERENCES users(user_uid) ON DELETE CASCADE;

-- House ad images reference house ads
ALTER TABLE house_ad_images 
ADD CONSTRAINT fk_house_ad_images_house_ad 
FOREIGN KEY (house_ad_uid) REFERENCES house_ads(house_ad_uid) ON DELETE CASCADE;

-- Messages reference house ads
ALTER TABLE house_ad_messages 
ADD CONSTRAINT fk_messages_house_ad 
FOREIGN KEY (receiver_house_ad_uid) REFERENCES house_ads(house_ad_uid) ON DELETE CASCADE;

-- Addresses reference users
ALTER TABLE addresses 
ADD CONSTRAINT fk_addresses_user 
FOREIGN KEY (user_uid) REFERENCES users(user_uid) ON DELETE CASCADE;
```

### Check Constraints

```sql
-- Email format validation
ALTER TABLE users 
ADD CONSTRAINT chk_users_email_format 
CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- Price validation
ALTER TABLE house_ads 
ADD CONSTRAINT chk_house_ads_price_positive 
CHECK (price IS NULL OR price >= 0);

-- Phone format validation
ALTER TABLE house_ad_messages 
ADD CONSTRAINT chk_messages_phone_format 
CHECK (sender_phone IS NULL OR sender_phone ~ '^\+?[1-9]\d{1,14}$');
```

### Unique Constraints

```sql
-- Username uniqueness
ALTER TABLE users 
ADD CONSTRAINT uk_users_username UNIQUE (username);

-- Email uniqueness (if required)
ALTER TABLE users 
ADD CONSTRAINT uk_users_email UNIQUE (email);

-- Role name uniqueness
ALTER TABLE roles 
ADD CONSTRAINT uk_roles_name UNIQUE (role_name);

-- Authority name uniqueness
ALTER TABLE authorities 
ADD CONSTRAINT uk_authorities_name UNIQUE (authority_name);
```

## 📝 Data Types

### Custom Data Types

```sql
-- Create custom types if needed
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE house_ad_status AS ENUM ('DRAFT', 'PUBLISHED', 'SOLD', 'REMOVED');
CREATE TYPE image_type AS ENUM ('JPEG', 'PNG', 'GIF', 'WEBP');

-- Add status columns
ALTER TABLE users ADD COLUMN status user_status DEFAULT 'ACTIVE';
ALTER TABLE house_ads ADD COLUMN status house_ad_status DEFAULT 'PUBLISHED';
```

### JSON Data Types

```sql
-- Add JSON columns for flexible data storage
ALTER TABLE house_ads ADD COLUMN metadata JSONB;
ALTER TABLE users ADD COLUMN preferences JSONB;

-- Create indexes on JSON fields
CREATE INDEX idx_house_ads_metadata ON house_ads USING gin(metadata);
CREATE INDEX idx_users_preferences ON users USING gin(preferences);

-- Example JSON queries
SELECT * FROM house_ads WHERE metadata->>'bedrooms' = '3';
SELECT * FROM users WHERE preferences->>'theme' = 'dark';
```

## 🔄 Migration Scripts

### Flyway Migration Example

```sql
-- V1__Create_initial_schema.sql
-- Create all tables as defined above

-- V2__Add_indexes.sql
-- Add performance indexes

-- V3__Add_constraints.sql
-- Add check constraints and additional foreign keys

-- V4__Add_json_columns.sql
-- Add JSONB columns for metadata

-- V5__Add_full_text_search.sql
-- Add full-text search capabilities
```

### Hibernate DDL Auto

```yaml
# Development
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recreate schema on startup

# Staging
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Update schema on startup

# Production
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Only validate schema
```

## ⚡ Performance Optimization

### Query Optimization

```sql
-- Enable query statistics
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Monitor slow queries
SELECT query, mean_time, calls, total_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;

-- Optimize specific queries
EXPLAIN (ANALYZE, BUFFERS) 
SELECT h.*, u.username 
FROM house_ads h 
JOIN users u ON h.user_uid = u.user_uid 
WHERE h.created_at > CURRENT_DATE - INTERVAL '7 days'
ORDER BY h.created_at DESC;
```

### Connection Pooling

```yaml
# HikariCP configuration
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

### Caching Strategy

```java
// Entity-level caching
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserEntity {
    // Entity definition
}

// Query caching
@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Query("SELECT u FROM UserEntity u WHERE u.username = :username")
Optional<UserEntity> findByUsername(@Param("username") String username);
```

## 💾 Backup & Recovery

### Backup Strategies

```bash
# Full database backup
pg_dump -h localhost -U admin -d findyourdreamhouse > backup_$(date +%Y%m%d).sql

# Compressed backup
pg_dump -h localhost -U admin -d findyourdreamhouse | gzip > backup_$(date +%Y%m%d).sql.gz

# Schema-only backup
pg_dump -h localhost -U admin -d findyourdreamhouse --schema-only > schema_backup.sql

# Data-only backup
pg_dump -h localhost -U admin -d findyourdreamhouse --data-only > data_backup.sql
```

### Point-in-Time Recovery

```bash
# Create base backup
pg_basebackup -h localhost -U admin -D /backup/base -Ft -z -P

# Enable WAL archiving
# In postgresql.conf:
# wal_level = replica
# archive_mode = on
# archive_command = 'cp %p /backup/wal/%f'

# Restore to specific point in time
pg_restore -h localhost -U admin -d findyourdreamhouse_restored \
  --create --clean --if-exists backup_20250107.sql
```

### Automated Backups

```bash
#!/bin/bash
# backup_script.sh

BACKUP_DIR="/backup/postgresql"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="findyourdreamhouse"

# Create backup directory
mkdir -p $BACKUP_DIR

# Create backup
pg_dump -h localhost -U admin -d $DB_NAME | gzip > $BACKUP_DIR/backup_$DATE.sql.gz

# Keep only last 7 days of backups
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete

# Upload to S3
aws s3 cp $BACKUP_DIR/backup_$DATE.sql.gz s3://findyourdreamhouse-backups/
```

### Monitoring and Alerts

```sql
-- Database size monitoring
SELECT 
    pg_size_pretty(pg_database_size('findyourdreamhouse')) as database_size,
    pg_size_pretty(pg_total_relation_size('users')) as users_table_size,
    pg_size_pretty(pg_total_relation_size('house_ads')) as house_ads_table_size;

-- Connection monitoring
SELECT 
    count(*) as total_connections,
    count(*) FILTER (WHERE state = 'active') as active_connections,
    count(*) FILTER (WHERE state = 'idle') as idle_connections
FROM pg_stat_activity 
WHERE datname = 'findyourdreamhouse';

-- Long-running queries
SELECT 
    pid,
    now() - pg_stat_activity.query_start AS duration,
    query
FROM pg_stat_activity 
WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes';
```

---

**Database Version**: PostgreSQL 15+  
**Last Updated**: 2025-01-07  
**Maintainer**: Database Team (db-team@company.com)

