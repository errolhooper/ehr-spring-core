# Quick Start Guide for CI/CD Pipeline

This guide provides quick commands and references for working with the EHR Spring Core CI/CD pipeline.

## Prerequisites

- AWS Account with appropriate permissions
- GitHub repository access with secrets configured
- AWS CLI installed and configured
- Docker installed (for ECS deployment)

## GitHub Secrets to Configure

Configure these secrets in your GitHub repository (Settings → Secrets and variables → Actions):

```
# AWS Credentials
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1

# Database
DB_URL=jdbc:postgresql://your-aurora-endpoint:5432/ehrdb
DB_USERNAME=postgres
DB_PASSWORD=your-secure-password

# Application
API_KEY=your-secure-api-key

# Deployment Targets (choose one or more)
## For Lambda:
LAMBDA_FUNCTION_NAME=ehr-spring-core

## For ECS:
ECR_REPOSITORY=ehr-spring-core
ECS_CLUSTER=ehr-cluster
ECS_SERVICE=ehr-spring-core-service

## For Elastic Beanstalk:
EB_APPLICATION=ehr-spring-core
EB_ENVIRONMENT=ehr-spring-core-prod

# Health Check (optional)
HEALTH_CHECK_URL=https://your-app-url/actuator/health
```

## Manual Workflow Trigger

Trigger a deployment manually from GitHub:

1. Go to Actions tab
2. Select "CI/CD Pipeline"
3. Click "Run workflow"
4. Choose deployment target: none, lambda, ecs, or elastic-beanstalk
5. Click "Run workflow"

## AWS Infrastructure Setup

### Quick Aurora Setup
```bash
aws cloudformation create-stack \
  --stack-name ehr-aurora \
  --template-body file://aws-infrastructure/aurora-database.yaml \
  --parameters \
    ParameterKey=VpcId,ParameterValue=vpc-xxxxx \
    ParameterKey=SubnetIds,ParameterValue=\"subnet-a,subnet-b\" \
  --capabilities CAPABILITY_IAM
```

### Quick ECS Setup
```bash
aws cloudformation create-stack \
  --stack-name ehr-ecs \
  --template-body file://aws-infrastructure/ecs-fargate.yaml \
  --parameters \
    ParameterKey=VpcId,ParameterValue=vpc-xxxxx \
    ParameterKey=SubnetIds,ParameterValue=\"subnet-a,subnet-b\" \
    ParameterKey=DatabaseEndpoint,ParameterValue=aurora-endpoint \
    ParameterKey=DatabasePasswordSecretArn,ParameterValue=arn:aws:... \
    ParameterKey=ApiKeySecretArn,ParameterValue=arn:aws:... \
  --capabilities CAPABILITY_IAM
```

## Local Testing

### Build and Test Locally
```bash
# Run tests
mvn test

# Build JAR
mvn package

# Build Docker image
docker build -t ehr-spring-core:local .

# Run Docker container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://localhost:5432/ehrdb \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e API_KEY=test-key \
  ehr-spring-core:local
```

## Monitoring

### View Workflow Logs
```bash
# Using GitHub CLI
gh run list --workflow=deploy.yml
gh run view <run-id> --log
```

### View AWS Logs
```bash
# ECS logs
aws logs tail /ecs/ehr-spring-core --follow

# Aurora logs
aws logs tail /aws/rds/cluster/ehr-aurora-cluster/postgresql --follow

# Lambda logs
aws logs tail /aws/lambda/ehr-spring-core --follow
```

### Check Application Health
```bash
# Local
curl http://localhost:8080/actuator/health

# Production (replace with your URL)
curl https://your-app-url/actuator/health
```

## Troubleshooting

### Build Failures
```bash
# Check Maven dependencies
mvn dependency:tree

# Clean and rebuild
mvn clean install
```

### Deployment Failures
```bash
# Check CloudFormation stack status
aws cloudformation describe-stacks --stack-name ehr-ecs

# Check ECS service status
aws ecs describe-services --cluster ehr-cluster --services ehr-spring-core-service

# Check task logs
aws ecs describe-tasks --cluster ehr-cluster --tasks <task-id>
```

### Database Connection Issues
```bash
# Test connection
psql -h your-aurora-endpoint -U postgres -d ehrdb

# Check security groups
aws ec2 describe-security-groups --group-ids sg-xxxxx
```

## Common Commands

### Update ECS Service
```bash
aws ecs update-service \
  --cluster ehr-cluster \
  --service ehr-spring-core-service \
  --force-new-deployment
```

### Update Lambda Function
```bash
aws lambda update-function-code \
  --function-name ehr-spring-core \
  --zip-file fileb://target/ehr-spring-core-1.0.0.jar
```

### Update Elastic Beanstalk Environment
```bash
eb deploy ehr-spring-core-prod
```

### View CloudWatch Alarms
```bash
aws cloudwatch describe-alarms --state-value ALARM
```

### Scale ECS Service
```bash
aws ecs update-service \
  --cluster ehr-cluster \
  --service ehr-spring-core-service \
  --desired-count 4
```

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_URL` | Yes | PostgreSQL JDBC connection string |
| `DB_USERNAME` | Yes | Database username |
| `DB_PASSWORD` | Yes | Database password |
| `API_KEY` | Yes | Application API key |
| `DB_DDL_AUTO` | No | Hibernate DDL mode (default: validate) |
| `DB_SHOW_SQL` | No | Show SQL in logs (default: false) |

## Cost Estimates

- **Aurora Serverless v2**: $43-172/month
- **ECS Fargate (2 tasks)**: ~$30/month
- **Application Load Balancer**: ~$22/month
- **Lambda**: Pay per request (free tier available)
- **Elastic Beanstalk**: Instance costs + $0/month for platform

## Next Steps

1. ✅ Configure GitHub Secrets
2. ✅ Deploy AWS infrastructure using CloudFormation
3. ✅ Verify database connectivity
4. ✅ Push code to trigger pipeline
5. ✅ Monitor deployment progress
6. ✅ Test application endpoints
7. ✅ Set up monitoring and alarms

## Additional Resources

- [Complete CI/CD Documentation](CICD.md)
- [AWS Infrastructure Guide](aws-infrastructure/README.md)
- [Aurora Setup Guide](AURORA_SETUP.md)
- [Main README](README.md)
