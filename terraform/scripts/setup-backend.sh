#!/bin/bash
# Script to initialize Terraform backend (S3 bucket and DynamoDB table)
# This script should be run once per environment before using Terraform

set -e

# Default values
ENVIRONMENT=${1:-dev}
AWS_REGION=${2:-us-east-1}

BUCKET_NAME="ehr-terraform-state-${ENVIRONMENT}"
DYNAMODB_TABLE="ehr-terraform-locks-${ENVIRONMENT}"

echo "=========================================="
echo "Setting up Terraform Backend"
echo "=========================================="
echo "Environment: ${ENVIRONMENT}"
echo "AWS Region: ${AWS_REGION}"
echo "S3 Bucket: ${BUCKET_NAME}"
echo "DynamoDB Table: ${DYNAMODB_TABLE}"
echo "=========================================="

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "ERROR: AWS CLI is not installed. Please install it first."
    exit 1
fi

# Check AWS credentials
echo "Checking AWS credentials..."
if ! aws sts get-caller-identity &> /dev/null; then
    echo "ERROR: AWS credentials are not configured. Please configure them first."
    exit 1
fi

# Create S3 bucket for Terraform state
echo "Creating S3 bucket: ${BUCKET_NAME}..."
if aws s3api head-bucket --bucket "${BUCKET_NAME}" 2>/dev/null; then
    echo "S3 bucket already exists: ${BUCKET_NAME}"
else
    aws s3api create-bucket \
        --bucket "${BUCKET_NAME}" \
        --region "${AWS_REGION}" \
        $(if [ "${AWS_REGION}" != "us-east-1" ]; then echo "--create-bucket-configuration LocationConstraint=${AWS_REGION}"; fi)
    
    # Enable versioning
    echo "Enabling versioning on S3 bucket..."
    aws s3api put-bucket-versioning \
        --bucket "${BUCKET_NAME}" \
        --versioning-configuration Status=Enabled
    
    # Enable encryption
    echo "Enabling encryption on S3 bucket..."
    aws s3api put-bucket-encryption \
        --bucket "${BUCKET_NAME}" \
        --server-side-encryption-configuration '{
            "Rules": [{
                "ApplyServerSideEncryptionByDefault": {
                    "SSEAlgorithm": "AES256"
                }
            }]
        }'
    
    # Block public access
    echo "Blocking public access on S3 bucket..."
    aws s3api put-public-access-block \
        --bucket "${BUCKET_NAME}" \
        --public-access-block-configuration \
            "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
    
    echo "S3 bucket created successfully: ${BUCKET_NAME}"
fi

# Create DynamoDB table for state locking
echo "Creating DynamoDB table: ${DYNAMODB_TABLE}..."
if aws dynamodb describe-table --table-name "${DYNAMODB_TABLE}" --region "${AWS_REGION}" &> /dev/null; then
    echo "DynamoDB table already exists: ${DYNAMODB_TABLE}"
else
    aws dynamodb create-table \
        --table-name "${DYNAMODB_TABLE}" \
        --attribute-definitions AttributeName=LockID,AttributeType=S \
        --key-schema AttributeName=LockID,KeyType=HASH \
        --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
        --region "${AWS_REGION}" \
        --tags Key=Environment,Value="${ENVIRONMENT}" Key=ManagedBy,Value=Terraform
    
    echo "Waiting for DynamoDB table to be active..."
    aws dynamodb wait table-exists --table-name "${DYNAMODB_TABLE}" --region "${AWS_REGION}"
    
    echo "DynamoDB table created successfully: ${DYNAMODB_TABLE}"
fi

echo "=========================================="
echo "Terraform backend setup completed!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Update terraform/main.tf to uncomment the backend configuration"
echo "2. Run: cd terraform && terraform init -backend-config=environments/${ENVIRONMENT}/backend.tfvars"
echo "3. Create secrets.tfvars from secrets.tfvars.example"
echo "4. Run: terraform plan -var-file=environments/${ENVIRONMENT}/terraform.tfvars -var-file=environments/${ENVIRONMENT}/secrets.tfvars"
echo ""
