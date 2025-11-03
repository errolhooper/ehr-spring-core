# AWS Infrastructure Templates

This directory contains AWS CloudFormation templates for deploying the EHR Spring Core application infrastructure.

## Templates

### 1. aurora-database.yaml
Creates Aurora PostgreSQL Serverless v2 database cluster with:
- Aurora PostgreSQL 15.4
- Serverless v2 auto-scaling
- Automatic backups and encryption
- CloudWatch monitoring and alarms
- Secrets Manager integration
- IAM database authentication
- Performance Insights

### 2. ecs-fargate.yaml
Creates ECS Fargate deployment infrastructure with:
- ECR repository for Docker images
- ECS cluster with Container Insights
- Fargate task definition
- Application Load Balancer
- Auto-scaling configuration
- CloudWatch logging
- IAM roles and security groups

## Deployment Order

Deploy templates in this order:

1. **Database First**: Deploy `aurora-database.yaml`
2. **Application Infrastructure**: Deploy `ecs-fargate.yaml`
3. **CI/CD Pipeline**: Configure GitHub Actions with outputs

## Quick Start

### Deploy Aurora Database

```bash
# Set variables
STACK_NAME="ehr-aurora-stack"
VPC_ID="vpc-xxxxxxxxx"
SUBNET_IDS="subnet-xxxxxx,subnet-yyyyyy"

# Deploy stack
aws cloudformation create-stack \
  --stack-name $STACK_NAME \
  --template-body file://aurora-database.yaml \
  --parameters \
    ParameterKey=VpcId,ParameterValue=$VPC_ID \
    ParameterKey=SubnetIds,ParameterValue=\"$SUBNET_IDS\" \
    ParameterKey=DatabaseName,ParameterValue=ehrdb \
    ParameterKey=MasterUsername,ParameterValue=postgres \
    ParameterKey=MinCapacity,ParameterValue=0.5 \
    ParameterKey=MaxCapacity,ParameterValue=2 \
  --capabilities CAPABILITY_IAM

# Wait for completion
aws cloudformation wait stack-create-complete --stack-name $STACK_NAME

# Get outputs
aws cloudformation describe-stacks \
  --stack-name $STACK_NAME \
  --query 'Stacks[0].Outputs'
```

### Deploy ECS Infrastructure

```bash
# Set variables
STACK_NAME="ehr-ecs-stack"
VPC_ID="vpc-xxxxxxxxx"
SUBNET_IDS="subnet-xxxxxx,subnet-yyyyyy"
DB_ENDPOINT="ehr-aurora-cluster.cluster-xxxxx.us-east-1.rds.amazonaws.com"
DB_SECRET_ARN="arn:aws:secretsmanager:us-east-1:123456789:secret:ehr-db-password"
API_KEY_SECRET_ARN="arn:aws:secretsmanager:us-east-1:123456789:secret:ehr-api-key"

# Deploy stack
aws cloudformation create-stack \
  --stack-name $STACK_NAME \
  --template-body file://ecs-fargate.yaml \
  --parameters \
    ParameterKey=VpcId,ParameterValue=$VPC_ID \
    ParameterKey=SubnetIds,ParameterValue=\"$SUBNET_IDS\" \
    ParameterKey=DatabaseEndpoint,ParameterValue=$DB_ENDPOINT \
    ParameterKey=DatabaseName,ParameterValue=ehrdb \
    ParameterKey=DatabaseUsername,ParameterValue=postgres \
    ParameterKey=DatabasePasswordSecretArn,ParameterValue=$DB_SECRET_ARN \
    ParameterKey=ApiKeySecretArn,ParameterValue=$API_KEY_SECRET_ARN \
  --capabilities CAPABILITY_IAM

# Wait for completion
aws cloudformation wait stack-create-complete --stack-name $STACK_NAME

# Get outputs
aws cloudformation describe-stacks \
  --stack-name $STACK_NAME \
  --query 'Stacks[0].Outputs'
```

## Creating Secrets

Before deploying ECS infrastructure, create the API key secret:

```bash
# Generate a secure API key
API_KEY=$(openssl rand -base64 32)

# Create secret in Secrets Manager
aws secretsmanager create-secret \
  --name ehr-api-key \
  --description "API key for EHR Spring Core" \
  --secret-string "{\"api-key\":\"$API_KEY\"}"

# Get the secret ARN
aws secretsmanager describe-secret \
  --secret-id ehr-api-key \
  --query 'ARN' \
  --output text
```

## Configuring GitHub Secrets

After deploying infrastructure, configure GitHub Secrets with the CloudFormation outputs:

```bash
# Get database connection string
DB_URL=$(aws cloudformation describe-stacks \
  --stack-name ehr-aurora-stack \
  --query 'Stacks[0].Outputs[?OutputKey==`JDBCConnectionString`].OutputValue' \
  --output text)

# Get database secret ARN
DB_SECRET=$(aws cloudformation describe-stacks \
  --stack-name ehr-aurora-stack \
  --query 'Stacks[0].Outputs[?OutputKey==`DatabaseSecretArn`].OutputValue' \
  --output text)

# Get database password
DB_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id $DB_SECRET \
  --query 'SecretString' \
  --output text | jq -r '.password')

# Get ECR repository URI
ECR_URI=$(aws cloudformation describe-stacks \
  --stack-name ehr-ecs-stack \
  --query 'Stacks[0].Outputs[?OutputKey==`ECRRepositoryUri`].OutputValue' \
  --output text)

# Get ECS cluster name
ECS_CLUSTER=$(aws cloudformation describe-stacks \
  --stack-name ehr-ecs-stack \
  --query 'Stacks[0].Outputs[?OutputKey==`ClusterName`].OutputValue' \
  --output text)

# Get ECS service name
ECS_SERVICE=$(aws cloudformation describe-stacks \
  --stack-name ehr-ecs-stack \
  --query 'Stacks[0].Outputs[?OutputKey==`ServiceName`].OutputValue' \
  --output text)

# Get Load Balancer URL
LB_URL=$(aws cloudformation describe-stacks \
  --stack-name ehr-ecs-stack \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerURL`].OutputValue' \
  --output text)

echo "Configure these in GitHub Secrets:"
echo "DB_URL: $DB_URL"
echo "DB_USERNAME: postgres"
echo "DB_PASSWORD: $DB_PASSWORD"
echo "API_KEY: (from ehr-api-key secret)"
echo "ECR_REPOSITORY: ${ECR_URI##*/}"
echo "ECS_CLUSTER: $ECS_CLUSTER"
echo "ECS_SERVICE: $ECS_SERVICE"
echo "HEALTH_CHECK_URL: $LB_URL/actuator/health"
```

## Stack Updates

### Update Aurora Configuration

```bash
aws cloudformation update-stack \
  --stack-name ehr-aurora-stack \
  --template-body file://aurora-database.yaml \
  --parameters \
    ParameterKey=VpcId,UsePreviousValue=true \
    ParameterKey=SubnetIds,UsePreviousValue=true \
    ParameterKey=DatabaseName,UsePreviousValue=true \
    ParameterKey=MasterUsername,UsePreviousValue=true \
    ParameterKey=MinCapacity,ParameterValue=1 \
    ParameterKey=MaxCapacity,ParameterValue=4 \
  --capabilities CAPABILITY_IAM
```

### Update ECS Service

```bash
# Force new deployment with latest image
aws ecs update-service \
  --cluster ehr-cluster \
  --service ehr-spring-core-service \
  --force-new-deployment
```

## Monitoring

### View CloudWatch Logs

```bash
# ECS logs
aws logs tail /ecs/ehr-spring-core --follow

# Aurora logs
aws logs tail /aws/rds/cluster/ehr-aurora-cluster/postgresql --follow
```

### Check CloudWatch Alarms

```bash
aws cloudwatch describe-alarms \
  --alarm-name-prefix ehr- \
  --state-value ALARM
```

### View ECS Service Status

```bash
aws ecs describe-services \
  --cluster ehr-cluster \
  --services ehr-spring-core-service
```

### Check Aurora Metrics

```bash
aws cloudwatch get-metric-statistics \
  --namespace AWS/RDS \
  --metric-name ServerlessDatabaseCapacity \
  --dimensions Name=DBClusterIdentifier,Value=ehr-aurora-cluster \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Average
```

## Cleanup

Delete stacks in reverse order:

```bash
# Delete ECS stack
aws cloudformation delete-stack --stack-name ehr-ecs-stack

# Wait for deletion
aws cloudformation wait stack-delete-complete --stack-name ehr-ecs-stack

# Delete Aurora stack (creates final snapshot)
aws cloudformation delete-stack --stack-name ehr-aurora-stack

# Wait for deletion
aws cloudformation wait stack-delete-complete --stack-name ehr-aurora-stack

# Delete secrets (optional)
aws secretsmanager delete-secret --secret-id ehr-db-password --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id ehr-api-key --force-delete-without-recovery
```

## Cost Estimation

### Aurora Serverless v2
- Minimum: ~$43/month (0.5 ACU, 24/7)
- Typical: ~$86-172/month (0.5-2 ACU range)
- Storage: $0.10/GB-month
- Backups: $0.021/GB-month

### ECS Fargate
- 2 tasks @ 0.5 vCPU, 1 GB: ~$30/month
- Application Load Balancer: ~$22/month
- Data transfer: Variable

### Total Estimated Cost
- Minimum: ~$95/month
- Typical: ~$140-225/month (includes small usage)

## Security Best Practices

1. **Enable MFA Delete** on CloudFormation stacks
2. **Use IAM roles** instead of access keys when possible
3. **Enable CloudTrail** for audit logging
4. **Rotate secrets** regularly via Secrets Manager
5. **Review security groups** and restrict access
6. **Enable VPC Flow Logs** for network monitoring
7. **Use WAF** with Application Load Balancer
8. **Enable GuardDuty** for threat detection

## Troubleshooting

### Stack Creation Fails

```bash
# Check events
aws cloudformation describe-stack-events \
  --stack-name ehr-aurora-stack \
  --max-items 10

# Check resource status
aws cloudformation describe-stack-resources \
  --stack-name ehr-aurora-stack
```

### Aurora Connection Issues

```bash
# Test connectivity from EC2 instance in same VPC
psql -h ehr-aurora-cluster.cluster-xxxxx.us-east-1.rds.amazonaws.com \
  -U postgres \
  -d ehrdb

# Check security group rules
aws ec2 describe-security-groups \
  --group-ids sg-xxxxxxxxx
```

### ECS Tasks Failing

```bash
# Check task logs
aws ecs describe-tasks \
  --cluster ehr-cluster \
  --tasks <task-id>

# View recent logs
aws logs tail /ecs/ehr-spring-core --since 1h
```

## Additional Resources

- [AWS CloudFormation Documentation](https://docs.aws.amazon.com/cloudformation/)
- [Amazon Aurora Documentation](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/)
- [Amazon ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS Fargate Documentation](https://docs.aws.amazon.com/fargate/)
- [Main CI/CD Documentation](../CICD.md)
- [Aurora Setup Guide](../AURORA_SETUP.md)
