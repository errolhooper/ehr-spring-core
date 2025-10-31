# Terraform Infrastructure

This directory contains Infrastructure-as-Code (IaC) for deploying the EHR Spring Core application to AWS.

## Quick Start

### 1. Prerequisites

- Terraform >= 1.5.0
- AWS CLI configured with credentials
- AWS account with appropriate permissions

### 2. Initialize Backend

```bash
./scripts/setup-backend.sh dev us-east-1
```

### 3. Configure Secrets

```bash
cd environments/dev
cp secrets.tfvars.example secrets.tfvars
# Edit secrets.tfvars with your actual values
```

### 4. Deploy

```bash
./scripts/deploy.sh dev plan
./scripts/deploy.sh dev apply
```

## Directory Structure

```
terraform/
├── main.tf                  # Main configuration
├── variables.tf             # Variable definitions
├── outputs.tf               # Output definitions
├── modules/                 # Reusable modules
│   ├── networking/          # VPC and networking
│   ├── aurora/              # PostgreSQL database
│   ├── lambda/              # Lambda functions
│   ├── api-gateway/         # API Gateway
│   └── iam/                 # IAM roles and policies
├── environments/            # Environment configurations
│   ├── dev/
│   ├── staging/
│   └── prod/
└── scripts/
    ├── setup-backend.sh     # Initialize backend
    └── deploy.sh            # Deploy infrastructure
```

## Modules

- **networking**: VPC, subnets, NAT gateway, route tables
- **aurora**: Aurora PostgreSQL Serverless v2 cluster
- **lambda**: Lambda function for Spring Boot application
- **api-gateway**: REST API with Lambda integration
- **iam**: IAM roles and policies

## Environments

- **dev**: Development environment (minimal resources)
- **staging**: Pre-production testing (moderate resources)
- **prod**: Production environment (full resources)

## Documentation

See [TERRAFORM.md](../TERRAFORM.md) in the root directory for comprehensive documentation.

## Support

For issues or questions, please refer to the main project documentation or create an issue.
