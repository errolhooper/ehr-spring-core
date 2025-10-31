# Terraform Infrastructure Documentation

This document provides comprehensive guidance on using Terraform to manage the infrastructure for the EHR Spring Core application.

## Overview

The infrastructure is defined using Terraform Infrastructure-as-Code (IaC) and includes:

- **VPC and Networking**: Virtual Private Cloud with public, private, and database subnets across multiple availability zones
- **Aurora PostgreSQL**: Serverless v2 database cluster for data persistence
- **Lambda Functions**: Serverless compute for running the Spring Boot application
- **API Gateway**: REST API endpoint for accessing the application
- **IAM Roles and Policies**: Secure access control for AWS services
- **CloudWatch**: Logging and monitoring

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        API Gateway                           │
│                  (REST API Endpoints)                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Lambda Function                           │
│              (Spring Boot Application)                       │
│                      VPC-enabled                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Aurora PostgreSQL Cluster                       │
│               (Serverless v2)                                │
│        Multi-AZ with automated backups                       │
└─────────────────────────────────────────────────────────────┘
```

## Directory Structure

```
terraform/
├── main.tf                     # Main Terraform configuration
├── variables.tf                # Variable definitions
├── outputs.tf                  # Output definitions
├── modules/                    # Reusable Terraform modules
│   ├── networking/             # VPC, subnets, routing
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── aurora/                 # Aurora PostgreSQL cluster
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── lambda/                 # Lambda function
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── api-gateway/            # API Gateway REST API
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── iam/                    # IAM roles and policies
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
├── environments/               # Environment-specific configurations
│   ├── dev/
│   │   ├── terraform.tfvars
│   │   ├── backend.tfvars
│   │   └── secrets.tfvars.example
│   ├── staging/
│   │   ├── terraform.tfvars
│   │   ├── backend.tfvars
│   │   └── secrets.tfvars.example
│   └── prod/
│       ├── terraform.tfvars
│       ├── backend.tfvars
│       └── secrets.tfvars.example
└── scripts/
    ├── setup-backend.sh        # Initialize S3 backend
    └── deploy.sh               # Deploy infrastructure
```

## Prerequisites

Before you begin, ensure you have:

1. **Terraform** installed (version >= 1.5.0)
   ```bash
   terraform --version
   ```

2. **AWS CLI** installed and configured
   ```bash
   aws --version
   aws configure
   ```

3. **AWS Credentials** with appropriate permissions:
   - VPC management
   - RDS/Aurora management
   - Lambda management
   - API Gateway management
   - IAM role management
   - S3 bucket management (for state)
   - DynamoDB table management (for state locking)

4. **Java 17** and **Maven** (for building the Spring Boot application)

## Getting Started

### Step 1: Build the Spring Boot Application

Before deploying infrastructure, build the application JAR:

```bash
cd /path/to/ehr-spring-core
mvn clean package
```

This creates `target/ehr-spring-core-1.0.0.jar`

### Step 2: Set Up Terraform Backend

The Terraform state is stored remotely in S3 for team collaboration and state locking. Run the setup script once per environment:

```bash
cd terraform
./scripts/setup-backend.sh dev us-east-1
```

This creates:
- S3 bucket: `ehr-terraform-state-dev`
- DynamoDB table: `ehr-terraform-locks-dev`

For other environments:
```bash
./scripts/setup-backend.sh staging us-east-1
./scripts/setup-backend.sh prod us-east-1
```

### Step 3: Configure Secrets

Create a secrets file for your environment:

```bash
cd environments/dev
cp secrets.tfvars.example secrets.tfvars
```

Edit `secrets.tfvars` and add your actual values:

```hcl
master_username = "postgres"
master_password = "your-secure-password-here"
api_key         = "your-api-key-here"
aws_region      = "us-east-1"
```

**Important**: Never commit `secrets.tfvars` to version control!

### Step 4: Initialize Terraform

From the `terraform` directory:

```bash
terraform init -backend-config=environments/dev/backend.tfvars
```

This downloads required providers and configures the backend.

### Step 5: Review the Plan

Generate and review an execution plan:

```bash
terraform plan \
  -var-file=environments/dev/terraform.tfvars \
  -var-file=environments/dev/secrets.tfvars \
  -out=dev.tfplan
```

Review the changes that Terraform will make.

### Step 6: Apply the Configuration

Deploy the infrastructure:

```bash
terraform apply dev.tfplan
```

Or use the helper script:

```bash
./scripts/deploy.sh dev apply
```

### Step 7: Retrieve Outputs

After deployment, view the infrastructure details:

```bash
terraform output
```

Important outputs:
- `api_gateway_url`: The API endpoint URL
- `aurora_cluster_endpoint`: Database connection endpoint
- `lambda_function_name`: Name of the deployed Lambda function

## Deployment Workflow

### Using Helper Scripts

The repository includes helper scripts for common operations:

#### Deploy to Development
```bash
./scripts/deploy.sh dev plan    # Review changes
./scripts/deploy.sh dev apply   # Apply changes
```

#### Deploy to Staging
```bash
./scripts/deploy.sh staging plan
./scripts/deploy.sh staging apply
```

#### Deploy to Production
```bash
./scripts/deploy.sh prod plan
./scripts/deploy.sh prod apply
```

#### Destroy Infrastructure
```bash
./scripts/deploy.sh dev destroy
```

### Manual Workflow

If you prefer manual control:

```bash
cd terraform

# Select workspace
terraform workspace select dev || terraform workspace new dev

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

# Destroy (when needed)
terraform destroy \
  -var-file=environments/dev/terraform.tfvars \
  -var-file=environments/dev/secrets.tfvars
```

## Environment Configurations

### Development

- **Purpose**: Testing and development
- **Aurora Capacity**: 0.5-1.0 ACU
- **Lambda Memory**: 1024 MB
- **Database DDL**: `update` (auto-creates tables)
- **API Key**: Optional
- **Cost**: Lowest

### Staging

- **Purpose**: Pre-production testing
- **Aurora Capacity**: 0.5-2.0 ACU
- **Lambda Memory**: 1536 MB
- **Database DDL**: `validate` (requires manual schema)
- **API Key**: Required
- **Cost**: Medium

### Production

- **Purpose**: Live application
- **Aurora Capacity**: 1.0-4.0 ACU
- **Lambda Memory**: 2048 MB
- **Reserved Concurrency**: 10
- **Database DDL**: `validate` (requires manual schema)
- **API Key**: Required
- **Backup Retention**: 30 days
- **Cost**: Highest

## Module Details

### Networking Module

Creates:
- VPC with DNS support
- Public subnets (for NAT Gateway)
- Private subnets (for Lambda)
- Database subnets (for Aurora)
- Internet Gateway
- NAT Gateway (optional)
- Route tables

### Aurora Module

Creates:
- Aurora PostgreSQL Serverless v2 cluster
- Database security group
- Subnet group
- Secrets Manager secret for credentials
- CloudWatch log exports

Features:
- Automated backups
- Encryption at rest
- Performance Insights
- Multi-AZ support

### Lambda Module

Creates:
- Lambda function (Java 17 runtime)
- Security group
- CloudWatch Log Group
- Optional Function URL

Configuration:
- VPC-enabled for database access
- Environment variables for Spring Boot
- X-Ray tracing enabled
- Configurable memory and timeout

### API Gateway Module

Creates:
- REST API with proxy integration
- API stage
- CloudWatch logging
- Optional API key and usage plan

Features:
- Throttling controls
- Request/response logging
- X-Ray tracing
- CORS support

### IAM Module

Creates:
- Lambda execution role
- Policies for:
  - VPC networking (ENI management)
  - CloudWatch Logs
  - Aurora RDS access
  - Secrets Manager access

## State Management

### Remote State

Terraform state is stored in S3 with:
- **Versioning**: Enabled for recovery
- **Encryption**: AES-256
- **Locking**: Via DynamoDB
- **Access Control**: Private bucket

### State File Location

- Development: `s3://ehr-terraform-state-dev/dev/terraform.tfstate`
- Staging: `s3://ehr-terraform-state-staging/staging/terraform.tfstate`
- Production: `s3://ehr-terraform-state-prod/prod/terraform.tfstate`

### Workspaces

Each environment uses a separate workspace:
```bash
terraform workspace list
terraform workspace select dev
```

## Updating Lambda Code

When you update the Spring Boot application:

1. Build the new JAR:
   ```bash
   mvn clean package
   ```

2. The deployment package needs to be updated separately (not managed by this Terraform configuration). Options:
   
   a. **Manual Update**:
   ```bash
   aws lambda update-function-code \
     --function-name ehr-spring-core-dev-app \
     --zip-file fileb://target/ehr-spring-core-1.0.0.jar
   ```
   
   b. **CI/CD Pipeline**: Use GitHub Actions (see below)
   
   c. **Container Image**: Switch to container-based deployment

## Troubleshooting

### Common Issues

#### 1. Backend Initialization Fails

**Error**: `Error: Failed to get existing workspaces`

**Solution**: Ensure the S3 bucket and DynamoDB table exist:
```bash
./scripts/setup-backend.sh dev
```

#### 2. VPC Quota Exceeded

**Error**: `VPCLimitExceeded: The maximum number of VPCs has been reached`

**Solution**: Delete unused VPCs or request a quota increase

#### 3. Aurora Capacity Issues

**Error**: `Insufficient capacity`

**Solution**: Try a different region or wait and retry

#### 4. Lambda Deployment Package Too Large

**Error**: `Unzipped size must be smaller than 262144000 bytes`

**Solution**: Use Lambda layers or container images for large applications

### Debugging

Enable detailed logging:

```bash
export TF_LOG=DEBUG
terraform plan -var-file=...
```

View Lambda logs:
```bash
aws logs tail /aws/lambda/ehr-spring-core-dev-app --follow
```

Check Aurora status:
```bash
aws rds describe-db-clusters --db-cluster-identifier ehr-spring-core-dev-aurora-cluster
```

## Security Best Practices

1. **Never commit secrets**: Use `secrets.tfvars` and keep it out of version control
2. **Use IAM roles**: Avoid hardcoding AWS credentials
3. **Enable encryption**: All data is encrypted at rest and in transit
4. **Restrict access**: Use security groups and NACLs
5. **Rotate credentials**: Regularly rotate database passwords and API keys
6. **Enable monitoring**: Use CloudWatch and X-Ray for observability
7. **Backup regularly**: Aurora automated backups are enabled
8. **Use least privilege**: IAM policies grant minimal required permissions

## Cost Optimization

### Development
- Use smaller Aurora capacity (0.5 ACU min)
- Disable NAT Gateway if not needed
- Use shorter backup retention (3 days)
- Lower Lambda memory allocation

### Staging
- Balance between cost and performance
- Enable NAT Gateway for realistic testing
- Moderate Aurora capacity

### Production
- Right-size based on actual usage
- Use reserved concurrency for Lambda
- Enable Performance Insights for optimization
- Monitor costs with AWS Cost Explorer

## Cleanup

To destroy all resources in an environment:

```bash
./scripts/deploy.sh dev destroy
```

Or manually:

```bash
terraform destroy \
  -var-file=environments/dev/terraform.tfvars \
  -var-file=environments/dev/secrets.tfvars
```

**Warning**: This will permanently delete:
- All data in Aurora
- Lambda functions
- API Gateway
- VPC and networking resources

## Support and Maintenance

### Upgrading Terraform

When upgrading Terraform:

1. Check compatibility: Review provider release notes
2. Update version constraints in `main.tf`
3. Run `terraform init -upgrade`
4. Test in dev environment first

### Updating AWS Provider

Update the provider version in `main.tf`:

```hcl
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"  # Update this
    }
  }
}
```

Then run:
```bash
terraform init -upgrade
```

### Module Updates

The modules are self-contained. Update individual modules by:

1. Modifying the module's `main.tf`, `variables.tf`, or `outputs.tf`
2. Running `terraform plan` to review changes
3. Applying changes with `terraform apply`

## Additional Resources

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS Lambda with Spring Boot](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html)
- [Aurora Serverless v2](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/aurora-serverless-v2.html)
- [API Gateway](https://docs.aws.amazon.com/apigateway/)
- [Terraform Best Practices](https://www.terraform-best-practices.com/)

## Contributing

When making changes to the Terraform configuration:

1. Create a feature branch
2. Test changes in dev environment
3. Document changes in this file
4. Submit a pull request
5. Get approval before applying to staging/prod

## License

This infrastructure code is part of the EHR Spring Core project.
