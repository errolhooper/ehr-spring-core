# Lambda Deployment Guide

This guide explains how to deploy the Spring Boot application to AWS Lambda after the infrastructure has been provisioned with Terraform.

## Overview

The Terraform configuration creates the Lambda function infrastructure, but the application code (JAR file) needs to be deployed separately. This separation allows for:

- **Infrastructure changes** without redeploying code
- **Code updates** without Terraform state changes
- **Faster deployments** through CI/CD pipelines

## Deployment Methods

### Method 1: Manual Deployment with AWS CLI

#### Step 1: Build the Application

```bash
cd /path/to/ehr-spring-core
mvn clean package
```

This creates `target/ehr-spring-core-1.0.0.jar`

#### Step 2: Deploy to Lambda

```bash
# For development environment
aws lambda update-function-code \
  --function-name ehr-spring-core-dev-app \
  --zip-file fileb://target/ehr-spring-core-1.0.0.jar \
  --region us-east-1

# For staging environment
aws lambda update-function-code \
  --function-name ehr-spring-core-staging-app \
  --zip-file fileb://target/ehr-spring-core-1.0.0.jar \
  --region us-east-1

# For production environment
aws lambda update-function-code \
  --function-name ehr-spring-core-prod-app \
  --zip-file fileb://target/ehr-spring-core-1.0.0.jar \
  --region us-east-1
```

#### Step 3: Verify Deployment

```bash
# Check function configuration
aws lambda get-function-configuration \
  --function-name ehr-spring-core-dev-app \
  --region us-east-1

# View recent logs
aws logs tail /aws/lambda/ehr-spring-core-dev-app --follow
```

### Method 2: Using the Build Script

Use the provided build script:

```bash
./terraform/scripts/build-lambda-package.sh
```

This script:
1. Builds the JAR with Maven
2. Creates a deployment directory
3. Copies the JAR for deployment

Then deploy manually:

```bash
aws lambda update-function-code \
  --function-name ehr-spring-core-dev-app \
  --zip-file fileb://terraform/lambda-deployment/ehr-spring-core.jar \
  --region us-east-1
```

### Method 3: GitHub Actions (Recommended)

The repository includes a GitHub Actions workflow that automatically deploys code changes.

#### Setup

1. **Configure GitHub Secrets**:
   - `AWS_ACCESS_KEY_ID`: AWS access key
   - `AWS_SECRET_ACCESS_KEY`: AWS secret key
   - `AWS_REGION`: AWS region (e.g., us-east-1)

2. **Workflow Triggers**:
   - Automatic: Push to `main` or `develop` branches
   - Manual: Use workflow dispatch

#### Workflow File

Create `.github/workflows/deploy-lambda.yml`:

```yaml
name: Deploy Lambda Function

on:
  push:
    branches:
      - main
      - develop
    paths:
      - 'src/**'
      - 'pom.xml'
  workflow_dispatch:
    inputs:
      environment:
        description: 'Environment to deploy'
        required: true
        type: choice
        options:
          - dev
          - staging
          - prod

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build with Maven
        run: mvn clean package -DskipTests

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ secrets.AWS_REGION }}

      - name: Deploy to Lambda
        run: |
          ENVIRONMENT=${{ github.event.inputs.environment || 'dev' }}
          aws lambda update-function-code \
            --function-name ehr-spring-core-${ENVIRONMENT}-app \
            --zip-file fileb://target/ehr-spring-core-1.0.0.jar
```

### Method 4: Container Images (Alternative)

For larger applications, use container images instead of ZIP files.

#### Step 1: Create Dockerfile

Create `Dockerfile` in project root:

```dockerfile
FROM public.ecr.aws/lambda/java:17

# Copy application JAR
COPY target/ehr-spring-core-1.0.0.jar ${LAMBDA_TASK_ROOT}/lib/

# Set handler
CMD ["org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest"]
```

#### Step 2: Build and Push Image

```bash
# Build Docker image
docker build -t ehr-spring-core:latest .

# Tag for ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin \
  123456789012.dkr.ecr.us-east-1.amazonaws.com

docker tag ehr-spring-core:latest \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/ehr-spring-core:latest

# Push to ECR
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/ehr-spring-core:latest
```

#### Step 3: Update Lambda

```bash
aws lambda update-function-code \
  --function-name ehr-spring-core-dev-app \
  --image-uri 123456789012.dkr.ecr.us-east-1.amazonaws.com/ehr-spring-core:latest
```

#### Step 4: Update Terraform (Optional)

Modify `terraform/modules/lambda/main.tf` to use container images:

```hcl
resource "aws_lambda_function" "spring_boot_app" {
  function_name = "${var.project_name}-${var.environment}-app"
  role          = var.lambda_execution_role_arn
  
  package_type = "Image"
  image_uri    = var.image_uri
  
  # ... rest of configuration
}
```

## Lambda Configuration

### Handler Configuration

For Spring Boot on Lambda, use the Spring Cloud Function adapter:

- **Handler**: `org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest`
- **Runtime**: `java17`

### Environment Variables

The Lambda function is configured with these environment variables (set by Terraform):

- `DB_URL`: JDBC connection string to Aurora
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `API_KEY`: Application API key
- `DB_DDL_AUTO`: Hibernate DDL mode
- `DB_SHOW_SQL`: SQL logging flag
- `SPRING_PROFILES_ACTIVE`: Active Spring profile

### Memory and Timeout

Configured in `terraform/environments/<env>/terraform.tfvars`:

- **Development**: 1024 MB memory, 60s timeout
- **Staging**: 1536 MB memory, 60s timeout
- **Production**: 2048 MB memory, 60s timeout

## Testing Deployment

### Test Lambda Function

```bash
# Invoke function directly
aws lambda invoke \
  --function-name ehr-spring-core-dev-app \
  --payload '{"httpMethod":"GET","path":"/actuator/health"}' \
  response.json

cat response.json
```

### Test via API Gateway

Get the API Gateway URL from Terraform outputs:

```bash
cd terraform
terraform output api_gateway_url
```

Test endpoints:

```bash
API_URL="https://xxxxx.execute-api.us-east-1.amazonaws.com/dev"

# Health check
curl "${API_URL}/actuator/health"

# Ingest event
curl -X POST "${API_URL}/api/v1/ingest/events" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "eventName": "test.event",
    "timestamp": "2025-10-31T12:00:00Z",
    "properties": {"test": "value"}
  }'
```

## Troubleshooting

### Common Issues

#### 1. Lambda Timeout

**Symptom**: Function times out after 60 seconds

**Solution**: Increase timeout in `terraform.tfvars`:
```hcl
lambda_timeout = 120
```

Then run `terraform apply`

#### 2. Out of Memory

**Symptom**: Function fails with memory errors

**Solution**: Increase memory in `terraform.tfvars`:
```hcl
lambda_memory_size = 2048
```

#### 3. Cold Start Issues

**Symptom**: First request is slow

**Solutions**:
- Use Provisioned Concurrency (costs more)
- Optimize Spring Boot startup time
- Use SnapStart (Java 11+)

#### 4. VPC Connectivity Issues

**Symptom**: Cannot connect to Aurora

**Solution**: Verify:
- Lambda is in correct VPC subnets
- Security groups allow traffic
- NAT Gateway is working (for internet access)

### Viewing Logs

```bash
# Tail logs
aws logs tail /aws/lambda/ehr-spring-core-dev-app --follow

# Search logs
aws logs filter-log-events \
  --log-group-name /aws/lambda/ehr-spring-core-dev-app \
  --filter-pattern "ERROR"

# Get recent logs
aws logs get-log-events \
  --log-group-name /aws/lambda/ehr-spring-core-dev-app \
  --log-stream-name '2025/10/31/[$LATEST]xxxxx'
```

### Debugging

Enable debug logging by updating environment variables:

```bash
aws lambda update-function-configuration \
  --function-name ehr-spring-core-dev-app \
  --environment "Variables={DB_URL=...,DB_SHOW_SQL=true,LOGGING_LEVEL_ROOT=DEBUG}"
```

## Rollback

To rollback to a previous version:

```bash
# List versions
aws lambda list-versions-by-function \
  --function-name ehr-spring-core-dev-app

# Update alias to point to previous version
aws lambda update-alias \
  --function-name ehr-spring-core-dev-app \
  --name dev \
  --function-version 5
```

## Best Practices

1. **Test in Development First**: Always deploy to dev environment first
2. **Use Versioning**: Enable Lambda versioning for rollback capability
3. **Monitor Performance**: Use CloudWatch metrics and X-Ray tracing
4. **Optimize Cold Starts**: Keep dependencies minimal
5. **Use Secrets Manager**: Store sensitive data in AWS Secrets Manager
6. **Implement Health Checks**: Ensure `/actuator/health` works
7. **Set Up Alarms**: Create CloudWatch alarms for errors and latency
8. **Regular Updates**: Keep dependencies and runtime updated

## Performance Optimization

### Reduce Cold Start Time

1. **Minimize Dependencies**: Remove unused dependencies from `pom.xml`
2. **Use Lazy Initialization**: Enable in `application.yml`:
   ```yaml
   spring:
     main:
       lazy-initialization: true
   ```
3. **Optimize Bean Creation**: Use `@Lazy` annotations
4. **Use GraalVM Native Image**: For fastest startup (requires changes)

### Improve Execution Time

1. **Connection Pooling**: Configure HikariCP for Lambda:
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 2
         minimum-idle: 1
   ```
2. **Use Aurora Data API**: For short-lived functions
3. **Cache Database Connections**: Between invocations
4. **Enable X-Ray**: Identify bottlenecks

## Additional Resources

- [AWS Lambda Java Documentation](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html)
- [Spring Cloud Function AWS Adapter](https://docs.spring.io/spring-cloud-function/docs/current/reference/html/aws.html)
- [Lambda Best Practices](https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html)
- [Serverless Java on AWS Lambda](https://aws.amazon.com/blogs/compute/category/compute/aws-lambda/)

## Support

For issues or questions:
1. Check CloudWatch Logs
2. Review Terraform outputs
3. Verify AWS permissions
4. Create an issue in the repository
