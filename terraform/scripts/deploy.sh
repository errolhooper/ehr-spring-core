#!/bin/bash
# Script to deploy infrastructure using Terraform
# Usage: ./deploy.sh <environment> [plan|apply|destroy]

set -e

# Default values
ENVIRONMENT=${1:-dev}
ACTION=${2:-plan}

TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_DIR="${TERRAFORM_DIR}/environments/${ENVIRONMENT}"

echo "=========================================="
echo "Terraform Deployment"
echo "=========================================="
echo "Environment: ${ENVIRONMENT}"
echo "Action: ${ACTION}"
echo "Directory: ${TERRAFORM_DIR}"
echo "=========================================="

# Validate environment
if [ ! -d "${ENV_DIR}" ]; then
    echo "ERROR: Environment directory not found: ${ENV_DIR}"
    echo "Available environments: dev, staging, prod"
    exit 1
fi

# Check if secrets file exists
if [ ! -f "${ENV_DIR}/secrets.tfvars" ]; then
    echo "ERROR: Secrets file not found: ${ENV_DIR}/secrets.tfvars"
    echo "Please create it from secrets.tfvars.example"
    exit 1
fi

# Change to Terraform directory
cd "${TERRAFORM_DIR}"

# Initialize Terraform if needed
if [ ! -d ".terraform" ]; then
    echo "Initializing Terraform..."
    terraform init -backend-config="${ENV_DIR}/backend.tfvars"
fi

# Select or create workspace
echo "Selecting workspace: ${ENVIRONMENT}..."
terraform workspace select "${ENVIRONMENT}" 2>/dev/null || terraform workspace new "${ENVIRONMENT}"

# Execute Terraform action
case "${ACTION}" in
    plan)
        echo "Running terraform plan..."
        terraform plan \
            -var-file="${ENV_DIR}/terraform.tfvars" \
            -var-file="${ENV_DIR}/secrets.tfvars" \
            -out="${ENVIRONMENT}.tfplan"
        echo ""
        echo "Plan saved to: ${ENVIRONMENT}.tfplan"
        echo "To apply: ./scripts/deploy.sh ${ENVIRONMENT} apply"
        ;;
    
    apply)
        if [ -f "${ENVIRONMENT}.tfplan" ]; then
            echo "Applying saved plan: ${ENVIRONMENT}.tfplan..."
            terraform apply "${ENVIRONMENT}.tfplan"
            rm "${ENVIRONMENT}.tfplan"
        else
            echo "Running terraform apply..."
            terraform apply \
                -var-file="${ENV_DIR}/terraform.tfvars" \
                -var-file="${ENV_DIR}/secrets.tfvars"
        fi
        ;;
    
    destroy)
        echo "WARNING: This will destroy all resources in ${ENVIRONMENT} environment!"
        read -p "Are you sure? (yes/no): " -r
        if [[ $REPLY == "yes" ]]; then
            terraform destroy \
                -var-file="${ENV_DIR}/terraform.tfvars" \
                -var-file="${ENV_DIR}/secrets.tfvars"
        else
            echo "Destroy cancelled."
        fi
        ;;
    
    *)
        echo "ERROR: Invalid action: ${ACTION}"
        echo "Valid actions: plan, apply, destroy"
        exit 1
        ;;
esac

echo "=========================================="
echo "Terraform ${ACTION} completed!"
echo "=========================================="
