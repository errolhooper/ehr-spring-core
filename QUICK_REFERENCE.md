# Quick Reference Guide

This guide provides quick commands for common operations with the EHR Spring Core infrastructure.

## Prerequisites Check

```bash
# Check Terraform version
terraform --version

# Check AWS CLI
aws --version

# Check AWS credentials
aws sts get-caller-identity

# Check Java version
java --version

# Check Maven version
mvn --version
```

## Initial Setup

```bash
# 1. Clone repository
git clone https://github.com/errolhooper/ehr-spring-core.git
cd ehr-spring-core

# 2. Build application
mvn clean package

# 3. Setup Terraform backend
cd terraform
./scripts/setup-backend.sh dev us-east-1

# 4. Configure secrets
cd environments/dev
cp secrets.tfvars.example secrets.tfvars
nano secrets.tfvars  # Edit with your values

# 5. Deploy infrastructure
cd ../..
./scripts/deploy.sh dev plan
./scripts/deploy.sh dev apply
```

## Common Terraform Commands

```bash
# Navigate to terraform directory
cd terraform

# Initialize Terraform
terraform init -backend-config=environments/dev/backend.tfvars

# Plan changes
terraform plan \
  -var-file=environments/dev/terraform.tfvars \
  -var-file=environments/dev/secrets.tfvars

# Apply changes
terraform apply \
  -var-file=environments/dev/terraform.tfvars \
  -var-file=environments/dev/secrets.tfvars

# View outputs
terraform output

# View specific output
terraform output api_gateway_url

# Destroy infrastructure
terraform destroy \
  -var-file=environments/dev/terraform.tfvars \
  -var-file=environments/dev/secrets.tfvars

# Format code
terraform fmt -recursive

# Validate configuration
terraform validate

# Show state
terraform show

# List resources
terraform state list

# View specific resource
terraform state show module.aurora.aws_rds_cluster.aurora
```

## Lambda Deployment

```bash
# Build application
mvn clean package

# Deploy to dev
aws lambda update-function-code \
  --function-name ehr-spring-core-dev-app \
  --zip-file fileb://target/ehr-spring-core-1.0.0.jar

# Deploy to staging
aws lambda update-function-code \
  --function-name ehr-spring-core-staging-app \
  --zip-file fileb://target/ehr-spring-core-1.0.0.jar

# Deploy to prod
aws lambda update-function-code \
  --function-name ehr-spring-core-prod-app \
  --zip-file fileb://target/ehr-spring-core-1.0.0.jar

# Wait for update
aws lambda wait function-updated \
  --function-name ehr-spring-core-dev-app

# Get function info
aws lambda get-function-configuration \
  --function-name ehr-spring-core-dev-app
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=IngestionControllerTest

# Skip tests during build
mvn package -DskipTests

# Run with debug
mvn test -X
```

## API Testing

```bash
# Get API Gateway URL
cd terraform
API_URL=$(terraform output -raw api_gateway_url)

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

# Ingest metric
curl -X POST "${API_URL}/api/v1/ingest/metrics" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "metricName": "test.metric",
    "value": 42.5,
    "timestamp": "2025-10-31T12:00:00Z",
    "unit": "count"
  }'
```

## Monitoring and Logs

```bash
# Tail Lambda logs
aws logs tail /aws/lambda/ehr-spring-core-dev-app --follow

# Filter for errors
aws logs filter-log-events \
  --log-group-name /aws/lambda/ehr-spring-core-dev-app \
  --filter-pattern "ERROR"

# Get API Gateway logs
aws logs tail /aws/apigateway/ehr-spring-core-dev --follow

# View RDS logs
aws rds describe-db-log-files \
  --db-instance-identifier ehr-spring-core-dev-aurora-instance-1

# Get CloudWatch metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/Lambda \
  --metric-name Invocations \
  --dimensions Name=FunctionName,Value=ehr-spring-core-dev-app \
  --start-time 2025-10-31T00:00:00Z \
  --end-time 2025-10-31T23:59:59Z \
  --period 3600 \
  --statistics Sum
```

## Database Operations

```bash
# Get database endpoint
cd terraform
DB_ENDPOINT=$(terraform output -raw aurora_cluster_endpoint)

# Connect to database
psql -h ${DB_ENDPOINT} -U postgres -d ehrdb

# Run SQL query
psql -h ${DB_ENDPOINT} -U postgres -d ehrdb -c "SELECT * FROM events LIMIT 10;"

# Export data
pg_dump -h ${DB_ENDPOINT} -U postgres -d ehrdb > backup.sql

# Import data
psql -h ${DB_ENDPOINT} -U postgres -d ehrdb < backup.sql
```

## Workspace Management

```bash
# List workspaces
terraform workspace list

# Create workspace
terraform workspace new staging

# Select workspace
terraform workspace select dev

# Delete workspace
terraform workspace delete staging

# Show current workspace
terraform workspace show
```

## Security Operations

```bash
# Get database password from Secrets Manager
aws secretsmanager get-secret-value \
  --secret-id ehr-spring-core-dev-aurora-credentials \
  --query SecretString \
  --output text | jq -r .password

# Update API key
aws lambda update-function-configuration \
  --function-name ehr-spring-core-dev-app \
  --environment "Variables={API_KEY=new-key-here,...}"

# List IAM roles
aws iam list-roles --query 'Roles[?starts_with(RoleName, `ehr-spring-core`)].RoleName'

# View security group rules
aws ec2 describe-security-groups \
  --filters "Name=tag:Name,Values=ehr-spring-core-dev-*" \
  --query 'SecurityGroups[*].[GroupName,GroupId]'
```

## Cost Management

```bash
# Get cost estimate
aws ce get-cost-and-usage \
  --time-period Start=2025-10-01,End=2025-10-31 \
  --granularity MONTHLY \
  --metrics UnblendedCost \
  --filter file://cost-filter.json

# List resources by tag
aws resourcegroupstaggingapi get-resources \
  --tag-filters Key=Project,Values=ehr-spring-core \
  --query 'ResourceTagMappingList[*].[ResourceARN]'

# View Aurora capacity
aws rds describe-db-clusters \
  --db-cluster-identifier ehr-spring-core-dev-aurora-cluster \
  --query 'DBClusters[0].ServerlessV2ScalingConfiguration'
```

## Troubleshooting

```bash
# Check Lambda status
aws lambda get-function \
  --function-name ehr-spring-core-dev-app \
  --query 'Configuration.State'

# Get last error
aws lambda get-function \
  --function-name ehr-spring-core-dev-app \
  --query 'Configuration.LastUpdateStatus'

# Verify VPC connectivity
aws ec2 describe-network-interfaces \
  --filters "Name=description,Values=*ehr-spring-core-dev-app*"

# Check Aurora status
aws rds describe-db-clusters \
  --db-cluster-identifier ehr-spring-core-dev-aurora-cluster \
  --query 'DBClusters[0].Status'

# Test database connectivity from Lambda
aws lambda invoke \
  --function-name ehr-spring-core-dev-app \
  --payload '{"httpMethod":"GET","path":"/actuator/health"}' \
  response.json && cat response.json

# View recent deployments
aws lambda list-versions-by-function \
  --function-name ehr-spring-core-dev-app \
  --query 'Versions[-5:].[Version,LastModified]'
```

## Backup and Recovery

```bash
# Create Aurora snapshot
aws rds create-db-cluster-snapshot \
  --db-cluster-snapshot-identifier ehr-dev-manual-snapshot \
  --db-cluster-identifier ehr-spring-core-dev-aurora-cluster

# List snapshots
aws rds describe-db-cluster-snapshots \
  --db-cluster-identifier ehr-spring-core-dev-aurora-cluster

# Restore from snapshot
aws rds restore-db-cluster-from-snapshot \
  --db-cluster-identifier ehr-dev-restored \
  --snapshot-identifier ehr-dev-manual-snapshot

# Export Terraform state
terraform state pull > terraform-state-backup.json

# Import existing resource
terraform import module.aurora.aws_rds_cluster.aurora ehr-spring-core-dev-aurora-cluster
```

## Performance Tuning

```bash
# Update Lambda memory
aws lambda update-function-configuration \
  --function-name ehr-spring-core-dev-app \
  --memory-size 2048

# Update Lambda timeout
aws lambda update-function-configuration \
  --function-name ehr-spring-core-dev-app \
  --timeout 120

# Set reserved concurrency
aws lambda put-function-concurrency \
  --function-name ehr-spring-core-dev-app \
  --reserved-concurrent-executions 10

# Update Aurora capacity
aws rds modify-db-cluster \
  --db-cluster-identifier ehr-spring-core-dev-aurora-cluster \
  --serverless-v2-scaling-configuration MinCapacity=1.0,MaxCapacity=4.0
```

## GitHub Actions

```bash
# Trigger Terraform deploy workflow
gh workflow run terraform-deploy.yml \
  --field environment=dev \
  --field action=apply

# Trigger Lambda deploy workflow
gh workflow run deploy-lambda.yml \
  --field environment=dev

# List workflow runs
gh run list --workflow=terraform-deploy.yml

# View workflow run
gh run view <run-id>

# Download artifacts
gh run download <run-id>
```

## Environment Variables

```bash
# Set for local testing
export DB_URL="jdbc:postgresql://localhost:5432/ehrdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
export API_KEY="test-api-key"

# Run application locally
mvn spring-boot:run

# Or with JAR
java -jar target/ehr-spring-core-1.0.0.jar
```

## Cleanup

```bash
# Delete all Lambda versions (keep $LATEST)
aws lambda list-versions-by-function \
  --function-name ehr-spring-core-dev-app \
  --query 'Versions[?Version!=`$LATEST`].Version' \
  --output text | xargs -n1 -I {} \
  aws lambda delete-function --function-name ehr-spring-core-dev-app:{}

# Delete old CloudWatch logs
aws logs describe-log-streams \
  --log-group-name /aws/lambda/ehr-spring-core-dev-app \
  --query 'logStreams[?lastEventTime<`1698710400000`].logStreamName' \
  --output text | xargs -n1 -I {} \
  aws logs delete-log-stream \
  --log-group-name /aws/lambda/ehr-spring-core-dev-app \
  --log-stream-name {}

# Clean Maven build
mvn clean

# Clean Terraform cache
rm -rf .terraform .terraform.lock.hcl
```

## Useful Aliases

Add to your `~/.bashrc` or `~/.zshrc`:

```bash
# Terraform shortcuts
alias tf='terraform'
alias tfp='terraform plan'
alias tfa='terraform apply'
alias tfo='terraform output'
alias tfi='terraform init'

# AWS shortcuts
alias awsl='aws lambda'
alias awsrds='aws rds'
alias awslogs='aws logs'

# Project shortcuts
alias ehr-dev='cd ~/projects/ehr-spring-core && terraform workspace select dev'
alias ehr-build='cd ~/projects/ehr-spring-core && mvn clean package'
alias ehr-test='cd ~/projects/ehr-spring-core && mvn test'
```

## Additional Resources

- [AWS CLI Reference](https://docs.aws.amazon.com/cli/latest/reference/)
- [Terraform CLI](https://www.terraform.io/cli/commands)
- [Maven Commands](https://maven.apache.org/guides/getting-started/)
- [PostgreSQL Commands](https://www.postgresql.org/docs/current/app-psql.html)
