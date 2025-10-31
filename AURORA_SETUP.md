# Amazon Aurora PostgreSQL Setup Guide

This guide explains how to configure the EHR Spring Core application to connect to an Amazon Aurora PostgreSQL database.

## Overview

The application now supports persisting events and metrics to a PostgreSQL-compatible database, specifically optimized for Amazon Aurora PostgreSQL (serverless or provisioned).

## Database Schema

The application automatically creates the following tables:

### Events Table
- `id` (BIGINT, Primary Key, Auto-increment)
- `event_name` (VARCHAR, NOT NULL)
- `timestamp` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### Event Properties Table
- `event_id` (BIGINT, Foreign Key to events.id)
- `property_key` (VARCHAR)
- `property_value` (TEXT)

### Metrics Table
- `id` (BIGINT, Primary Key, Auto-increment)
- `metric_name` (VARCHAR, NOT NULL)
- `metric_value` (DOUBLE, NOT NULL)
- `timestamp` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `unit` (VARCHAR)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

## AWS Setup

### 1. Create Aurora PostgreSQL Cluster

#### Using AWS Console:
1. Go to Amazon RDS Console
2. Click "Create database"
3. Choose "Amazon Aurora"
4. Select "PostgreSQL-compatible"
5. Choose Edition:
   - **Serverless v2** (recommended for variable workloads)
   - **Provisioned** (for predictable workloads)
6. Configure database settings:
   - **DB cluster identifier**: `ehr-aurora-cluster`
   - **Master username**: `postgres` (or custom)
   - **Master password**: Create a secure password
7. Set capacity settings (for Serverless):
   - **Minimum ACUs**: 0.5
   - **Maximum ACUs**: 2.0 (adjust based on needs)
8. Configure VPC and security:
   - Choose your VPC
   - Create or select a security group
9. Create database

#### Using AWS CLI:
```bash
# Create Aurora Serverless v2 cluster
aws rds create-db-cluster \
  --db-cluster-identifier ehr-aurora-cluster \
  --engine aurora-postgresql \
  --engine-version 15.4 \
  --master-username postgres \
  --master-user-password YOUR_SECURE_PASSWORD \
  --serverless-v2-scaling-configuration MinCapacity=0.5,MaxCapacity=2.0 \
  --vpc-security-group-ids sg-xxxxxxxx \
  --db-subnet-group-name your-subnet-group

# Create database instance
aws rds create-db-instance \
  --db-instance-identifier ehr-aurora-instance \
  --db-cluster-identifier ehr-aurora-cluster \
  --db-instance-class db.serverless \
  --engine aurora-postgresql
```

### 2. Configure Security Group

Allow inbound traffic on port 5432 from your application:

```bash
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxxxxx \
  --protocol tcp \
  --port 5432 \
  --cidr YOUR_APP_IP/32
```

Or in AWS Console:
1. Go to EC2 → Security Groups
2. Select the Aurora security group
3. Add Inbound Rule:
   - **Type**: PostgreSQL
   - **Port**: 5432
   - **Source**: Your application's security group or IP range

### 3. Create Database (if not auto-created)

Connect to your Aurora cluster and create the database:

```bash
psql -h your-cluster-endpoint.region.rds.amazonaws.com -U postgres -d postgres
```

```sql
CREATE DATABASE ehrdb;
```

### 4. IAM Authentication (Optional but Recommended)

For enhanced security, use IAM database authentication:

#### Create IAM Policy:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "rds-db:connect"
      ],
      "Resource": [
        "arn:aws:rds-db:REGION:ACCOUNT_ID:dbuser:CLUSTER_RESOURCE_ID/postgres"
      ]
    }
  ]
}
```

#### Enable IAM Authentication on Aurora:
```bash
aws rds modify-db-cluster \
  --db-cluster-identifier ehr-aurora-cluster \
  --enable-iam-database-authentication \
  --apply-immediately
```

## Application Configuration

### Environment Variables

Set the following environment variables for your application:

#### Required Variables:
```bash
# Database connection
export DB_URL="jdbc:postgresql://your-cluster-endpoint.region.rds.amazonaws.com:5432/ehrdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_secure_password"

# API Security
export API_KEY="your-secure-api-key"
```

#### Optional Variables:
```bash
# Database behavior
export DB_DDL_AUTO="update"  # Options: none, validate, update, create, create-drop
export DB_SHOW_SQL="false"   # Set to true for debugging
```

### Configuration File (application.yml)

The default configuration in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/ehrdb}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: ${DB_DDL_AUTO:update}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: ${DB_SHOW_SQL:false}
```

### DDL Auto Options

- **`none`**: No schema management (production recommended)
- **`validate`**: Validate schema matches entities
- **`update`**: Update schema if needed (development)
- **`create`**: Drop and create schema on startup
- **`create-drop`**: Create on startup, drop on shutdown (testing only)

## Deployment Options

### Option 1: EC2 with Environment Variables

```bash
# Set environment variables
export DB_URL="jdbc:postgresql://your-cluster-endpoint.region.rds.amazonaws.com:5432/ehrdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
export API_KEY="your_api_key"

# Run application
java -jar ehr-spring-core-1.0.0.jar
```

### Option 2: ECS/Fargate Task Definition

```json
{
  "family": "ehr-spring-core",
  "containerDefinitions": [
    {
      "name": "ehr-app",
      "image": "your-ecr-repo/ehr-spring-core:latest",
      "environment": [
        {
          "name": "DB_URL",
          "value": "jdbc:postgresql://your-cluster-endpoint.region.rds.amazonaws.com:5432/ehrdb"
        },
        {
          "name": "DB_USERNAME",
          "value": "postgres"
        }
      ],
      "secrets": [
        {
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:db-password"
        },
        {
          "name": "API_KEY",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:api-key"
        }
      ]
    }
  ]
}
```

### Option 3: AWS Secrets Manager Integration

Store sensitive credentials in AWS Secrets Manager:

```bash
# Create secret
aws secretsmanager create-secret \
  --name ehr-db-credentials \
  --secret-string '{"username":"postgres","password":"your_password"}'
```

Update application to use Spring Cloud AWS:

```yaml
spring:
  cloud:
    aws:
      secretsmanager:
        enabled: true
```

### Option 4: Elastic Beanstalk

Add environment properties in `.ebextensions/environment.config`:

```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    DB_URL: jdbc:postgresql://your-cluster-endpoint.region.rds.amazonaws.com:5432/ehrdb
    DB_USERNAME: postgres
    DB_PASSWORD: your_password
    API_KEY: your_api_key
```

## Connection Pooling

The application uses HikariCP (default in Spring Boot) with optimal settings for Aurora:

Default configuration (can be customized):
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

For Aurora Serverless, consider:
- Lower `maximum-pool-size` (5-10)
- Shorter `max-lifetime` (900000ms = 15min)
- Enable `leak-detection-threshold` for debugging

## Monitoring and Troubleshooting

### Enable SQL Logging (Development Only)
```bash
export DB_SHOW_SQL="true"
```

### Check Database Connectivity
```bash
# Test connection
psql -h your-cluster-endpoint.region.rds.amazonaws.com -U postgres -d ehrdb -c "SELECT version();"
```

### CloudWatch Metrics
Monitor these Aurora metrics:
- `DatabaseConnections`
- `CPUUtilization`
- `ServerlessDatabaseCapacity` (for Serverless)
- `ReadLatency` / `WriteLatency`

### Common Issues

#### 1. Connection Timeout
- Verify security group rules allow traffic on port 5432
- Check VPC routing and subnet configuration
- Ensure Aurora cluster is in "available" state

#### 2. Authentication Failed
- Verify username and password are correct
- Check if IAM authentication is enabled (requires token instead of password)
- Ensure database user has necessary permissions

#### 3. Schema Creation Errors
- Set `DB_DDL_AUTO=validate` in production
- Manually create schema with migration tools (Flyway/Liquibase)
- Check Aurora version compatibility with Hibernate dialect

## Cost Optimization

### Serverless v2 Recommendations
- Set appropriate min/max ACU capacity
- Use Aurora Pause for dev/test environments
- Enable Performance Insights for query optimization

### Connection Management
- Use connection pooling efficiently
- Close connections properly in application code
- Monitor active connections via CloudWatch

## Security Best Practices

1. **Never hardcode credentials** - Use environment variables or Secrets Manager
2. **Use IAM authentication** when possible
3. **Enable encryption at rest** (default in Aurora)
4. **Enable encryption in transit** (SSL/TLS)
5. **Use VPC security groups** to restrict access
6. **Enable Enhanced Monitoring** for Aurora
7. **Regular backups** - Configure automated backups
8. **Use least privilege** - Grant only necessary database permissions

## Testing Locally

For local development, use Docker to run PostgreSQL:

```bash
docker run --name postgres-test \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=ehrdb \
  -p 5432:5432 \
  -d postgres:15
```

Then use local configuration:
```bash
export DB_URL="jdbc:postgresql://localhost:5432/ehrdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
```

## Additional Resources

- [Amazon Aurora PostgreSQL Documentation](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/Aurora.AuroraPostgreSQL.html)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate PostgreSQL Dialect](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#database-postgresql)
- [AWS IAM Database Authentication](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.IAMDBAuth.html)
