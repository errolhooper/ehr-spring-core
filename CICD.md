# CI/CD Pipeline Documentation

This document describes the GitHub Actions CI/CD pipeline for the EHR Spring Core application.

## Overview

The pipeline provides automated building, testing, and deployment of the application to AWS infrastructure. It supports multiple deployment targets:

1. **AWS Lambda** - Serverless deployment
2. **AWS ECS** - Container orchestration with Fargate
3. **AWS Elastic Beanstalk** - Platform-as-a-Service deployment

## Pipeline Workflow

### 1. Build and Test Job

**Trigger:** On every push or pull request to `main` or `develop` branches

**Steps:**
- Checks out the code
- Sets up Java 17 with Temurin distribution
- Caches Maven dependencies for faster builds
- Compiles the application
- Runs unit and integration tests
- Generates test reports
- Packages the application as an executable JAR
- Uploads the JAR as a build artifact

**Environment Variables:**
- `JAVA_VERSION`: Java version (default: 17)
- `MAVEN_OPTS`: Maven memory settings
- Test database configuration (H2 in-memory)

### 2. Deployment Jobs

Deployment jobs run only on pushes to the `main` branch or when manually triggered via `workflow_dispatch`.

#### Deploy to AWS Lambda

**Use Case:** Serverless API deployment with AWS Lambda

**Prerequisites:**
- Lambda function must be pre-created
- Execution role with necessary permissions
- VPC configuration if accessing RDS/Aurora

**Steps:**
1. Downloads the build artifact
2. Configures AWS credentials
3. Updates Lambda function code with the new JAR
4. Updates environment variables
5. Waits for deployment to complete

**Required Secrets:**
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION` (optional, defaults to us-east-1)
- `LAMBDA_FUNCTION_NAME` (optional, defaults to ehr-spring-core)
- `DB_URL` - PostgreSQL/Aurora connection string
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `API_KEY` - Application API key

**Configuration:**
```bash
# Example environment variables for Lambda
DB_URL=jdbc:postgresql://your-aurora-endpoint.region.rds.amazonaws.com:5432/ehrdb
DB_USERNAME=postgres
DB_PASSWORD=your-secure-password
API_KEY=your-secure-api-key
DB_DDL_AUTO=validate
DB_SHOW_SQL=false
```

#### Deploy to AWS ECS

**Use Case:** Containerized deployment with high availability and auto-scaling

**Prerequisites:**
- ECS cluster must be created
- ECR repository for Docker images
- Task definition with container specifications
- ECS service configured with load balancer
- VPC and security groups configured

**Steps:**
1. Downloads the build artifact
2. Configures AWS credentials
3. Logs in to Amazon ECR
4. Builds Docker image using the provided Dockerfile
5. Tags and pushes image to ECR
6. Forces new deployment of ECS service
7. Waits for service to stabilize

**Required Secrets:**
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `ECR_REPOSITORY` (optional, defaults to ehr-spring-core)
- `ECS_CLUSTER` (optional, defaults to ehr-cluster)
- `ECS_SERVICE` (optional, defaults to ehr-spring-core-service)
- `ECS_TASK_DEFINITION` (optional, defaults to ehr-spring-core-task)

**ECS Task Definition Example:**
```json
{
  "family": "ehr-spring-core-task",
  "containerDefinitions": [
    {
      "name": "ehr-app",
      "image": "123456789.dkr.ecr.us-east-1.amazonaws.com/ehr-spring-core:latest",
      "memory": 1024,
      "cpu": 512,
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "DB_URL",
          "value": "jdbc:postgresql://aurora-endpoint:5432/ehrdb"
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
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/ehr-spring-core",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ],
  "requiresCompatibilities": ["FARGATE"],
  "networkMode": "awsvpc",
  "cpu": "512",
  "memory": "1024"
}
```

#### Deploy to Elastic Beanstalk

**Use Case:** Managed platform with automatic load balancing and scaling

**Prerequisites:**
- Elastic Beanstalk application must be created
- Environment configured with appropriate platform (Java 17 Corretto)
- S3 bucket for application versions
- IAM roles configured

**Steps:**
1. Downloads the build artifact
2. Configures AWS credentials
3. Creates deployment package with JAR and configuration files
4. Uploads package to S3
5. Creates new application version
6. Updates environment with new version
7. Waits for environment to become ready

**Required Secrets:**
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `EB_APPLICATION` (optional, defaults to ehr-spring-core)
- `EB_ENVIRONMENT` (optional, defaults to ehr-spring-core-prod)
- `EB_BUCKET` (optional, auto-generated)

**Configuration Files:**
- `.ebextensions/01-environment.config` - Environment and instance settings
- `.ebextensions/02-app-config.config` - Application configuration
- `Procfile` - Process configuration

### 3. Database Migration Job

**Purpose:** Ensures database is ready before deployment

**Steps:**
1. Checks out the code
2. Sets up Java environment
3. Configures AWS credentials
4. Tests database connectivity
5. Runs database migrations (if configured)

**Notes:**
- Currently uses Hibernate's schema validation in production
- Can be extended to use Flyway or Liquibase for migrations
- Test database connectivity before deployment

### 4. Health Check Job

**Purpose:** Verifies successful deployment

**Steps:**
1. Waits 30 seconds for deployment to stabilize
2. Performs HTTP health check against `/actuator/health`
3. Retries up to 5 times with 10-second intervals
4. Reports success or failure

**Required Secrets (Optional):**
- `HEALTH_CHECK_URL` - Full URL to health endpoint
- `APPLICATION_URL` - Base application URL (health check path will be appended)

## Setup Instructions

### 1. Configure GitHub Secrets

Navigate to your repository's Settings → Secrets and variables → Actions, then add:

**AWS Credentials:**
```
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-east-1
```

**Database Configuration:**
```
DB_URL=jdbc:postgresql://your-aurora-endpoint.region.rds.amazonaws.com:5432/ehrdb
DB_USERNAME=postgres
DB_PASSWORD=your-secure-password
```

**Application Configuration:**
```
API_KEY=your-secure-api-key-minimum-32-characters
```

**Deployment Target Specific:**

For Lambda:
```
LAMBDA_FUNCTION_NAME=ehr-spring-core
```

For ECS:
```
ECR_REPOSITORY=ehr-spring-core
ECS_CLUSTER=ehr-cluster
ECS_SERVICE=ehr-spring-core-service
ECS_TASK_DEFINITION=ehr-spring-core-task
```

For Elastic Beanstalk:
```
EB_APPLICATION=ehr-spring-core
EB_ENVIRONMENT=ehr-spring-core-prod
EB_BUCKET=elasticbeanstalk-us-east-1-123456789
```

**Health Check (Optional):**
```
HEALTH_CHECK_URL=https://api.yourdomain.com/actuator/health
# OR
APPLICATION_URL=https://api.yourdomain.com
```

### 2. Create AWS Resources

#### For Lambda Deployment:

```bash
# Create execution role
aws iam create-role \
  --role-name ehr-lambda-role \
  --assume-role-policy-document file://lambda-trust-policy.json

# Attach policies
aws iam attach-role-policy \
  --role-name ehr-lambda-role \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole

# Create Lambda function
aws lambda create-function \
  --function-name ehr-spring-core \
  --runtime java17 \
  --role arn:aws:iam::ACCOUNT_ID:role/ehr-lambda-role \
  --handler org.springframework.cloud.function.adapter.aws.FunctionInvoker \
  --timeout 60 \
  --memory-size 1024 \
  --zip-file fileb://target/ehr-spring-core-1.0.0.jar
```

#### For ECS Deployment:

```bash
# Create ECR repository
aws ecr create-repository --repository-name ehr-spring-core

# Create ECS cluster
aws ecs create-cluster --cluster-name ehr-cluster

# Create task definition (use the JSON example above)
aws ecs register-task-definition --cli-input-json file://task-definition.json

# Create service
aws ecs create-service \
  --cluster ehr-cluster \
  --service-name ehr-spring-core-service \
  --task-definition ehr-spring-core-task \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx],securityGroups=[sg-xxx],assignPublicIp=ENABLED}"
```

#### For Elastic Beanstalk Deployment:

```bash
# Initialize EB CLI
eb init -p "Corretto 17 running on 64bit Amazon Linux 2" ehr-spring-core

# Create environment
eb create ehr-spring-core-prod \
  --instance-type t3.small \
  --envvars DB_URL=jdbc:postgresql://...,DB_USERNAME=postgres

# Or via AWS Console:
# 1. Go to Elastic Beanstalk Console
# 2. Create Application
# 3. Choose "Java" platform
# 4. Upload application code
# 5. Configure environment variables
```

### 3. Aurora Database Setup

See [AURORA_SETUP.md](AURORA_SETUP.md) for detailed Aurora PostgreSQL setup instructions.

**Quick Setup:**
```bash
# Create Aurora cluster
aws rds create-db-cluster \
  --db-cluster-identifier ehr-aurora-cluster \
  --engine aurora-postgresql \
  --engine-version 15.4 \
  --master-username postgres \
  --master-user-password YOUR_SECURE_PASSWORD \
  --serverless-v2-scaling-configuration MinCapacity=0.5,MaxCapacity=2.0

# Create database instance
aws rds create-db-instance \
  --db-instance-identifier ehr-aurora-instance \
  --db-cluster-identifier ehr-aurora-cluster \
  --db-instance-class db.serverless \
  --engine aurora-postgresql
```

## Manual Deployment

You can manually trigger deployments using the GitHub Actions UI:

1. Go to Actions tab in your repository
2. Select "CI/CD Pipeline" workflow
3. Click "Run workflow"
4. Select the branch and deployment target
5. Click "Run workflow"

## Monitoring and Troubleshooting

### View Workflow Runs

1. Navigate to the Actions tab
2. Select the workflow run
3. View job logs for detailed output

### Common Issues

**Build Failures:**
- Check Maven dependency resolution
- Verify Java version compatibility
- Review test logs in surefire-reports

**Deployment Failures:**
- Verify AWS credentials are correct
- Check IAM permissions
- Ensure AWS resources exist
- Review CloudWatch logs

**Health Check Failures:**
- Verify application started successfully
- Check security group rules allow traffic
- Review application logs
- Test health endpoint manually

### AWS CloudWatch Logs

**Lambda:**
```bash
aws logs tail /aws/lambda/ehr-spring-core --follow
```

**ECS:**
```bash
aws logs tail /ecs/ehr-spring-core --follow
```

**Elastic Beanstalk:**
```bash
eb logs -a ehr-spring-core
```

## Environment Variables Reference

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_URL` | Yes | - | JDBC connection string for PostgreSQL/Aurora |
| `DB_USERNAME` | Yes | - | Database username |
| `DB_PASSWORD` | Yes | - | Database password |
| `API_KEY` | Yes | - | API authentication key |
| `DB_DDL_AUTO` | No | validate | Hibernate DDL mode (none, validate, update, create) |
| `DB_SHOW_SQL` | No | false | Show SQL queries in logs |
| `AWS_REGION` | No | us-east-1 | AWS region for deployment |
| `LAMBDA_FUNCTION_NAME` | No | ehr-spring-core | Lambda function name |
| `ECR_REPOSITORY` | No | ehr-spring-core | ECR repository name |
| `ECS_CLUSTER` | No | ehr-cluster | ECS cluster name |
| `ECS_SERVICE` | No | ehr-spring-core-service | ECS service name |
| `EB_APPLICATION` | No | ehr-spring-core | Elastic Beanstalk application name |
| `EB_ENVIRONMENT` | No | ehr-spring-core-prod | Elastic Beanstalk environment name |

## Security Best Practices

1. **Never commit secrets to version control**
2. **Use GitHub Secrets for sensitive data**
3. **Use AWS Secrets Manager or Parameter Store for production secrets**
4. **Enable IAM authentication for RDS when possible**
5. **Use VPC security groups to restrict network access**
6. **Enable encryption at rest and in transit**
7. **Regularly rotate credentials**
8. **Use least privilege IAM policies**
9. **Enable AWS CloudTrail for audit logging**
10. **Review security group rules regularly**

## Cost Optimization

### Lambda
- Right-size memory allocation
- Use provisioned concurrency only when needed
- Enable Lambda Insights for monitoring

### ECS
- Use Fargate Spot for non-production workloads
- Right-size CPU and memory
- Enable auto-scaling based on metrics

### Elastic Beanstalk
- Use appropriate instance types
- Enable auto-scaling with appropriate thresholds
- Schedule scaling for predictable loads

### Database
- Use Aurora Serverless for variable workloads
- Enable Aurora Auto Scaling
- Configure appropriate backup retention
- Use read replicas only when needed

## Continuous Improvement

### Adding Database Migrations

To add Flyway migrations:

1. Add Flyway dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

2. Create migration scripts in `src/main/resources/db/migration/`

3. Update the database-migration job in the workflow

### Adding Code Quality Checks

Add SonarQube or similar tools:

```yaml
- name: Run SonarQube analysis
  run: mvn sonar:sonar -Dsonar.projectKey=ehr-spring-core
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

### Adding Security Scanning

Add dependency vulnerability scanning:

```yaml
- name: Run security scan
  uses: aquasecurity/trivy-action@master
  with:
    scan-type: 'fs'
    scan-ref: '.'
```

## Support

For issues or questions:
1. Check the GitHub Actions logs
2. Review AWS CloudWatch logs
3. Consult the main [README.md](README.md) for application details
4. Check [AURORA_SETUP.md](AURORA_SETUP.md) for database setup

## License

This is part of the EHR (Engineering & Innovation Hub) platform.
