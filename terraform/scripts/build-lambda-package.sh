#!/bin/bash
# Script to build and package the Spring Boot application for Lambda deployment
# This creates a deployment package compatible with AWS Lambda

set -e

echo "=========================================="
echo "Building Spring Boot Lambda Package"
echo "=========================================="

# Navigate to project root
cd "$(dirname "${BASH_SOURCE[0]}")/.."

# Build with Maven
echo "Building with Maven..."
mvn clean package -DskipTests

# Check if JAR was created
JAR_FILE="target/ehr-spring-core-1.0.0.jar"
if [ ! -f "${JAR_FILE}" ]; then
    echo "ERROR: JAR file not found: ${JAR_FILE}"
    exit 1
fi

# Create deployment package directory
DEPLOY_DIR="terraform/lambda-deployment"
mkdir -p "${DEPLOY_DIR}"

# Copy JAR to deployment directory
echo "Copying JAR to deployment directory..."
cp "${JAR_FILE}" "${DEPLOY_DIR}/ehr-spring-core.jar"

# Create a simple wrapper script for Lambda (optional)
cat > "${DEPLOY_DIR}/README.txt" <<EOF
Lambda Deployment Package
=========================

This directory contains the Spring Boot application JAR for AWS Lambda deployment.

File: ehr-spring-core.jar

To deploy manually:
aws lambda update-function-code \\
  --function-name ehr-spring-core-dev-app \\
  --zip-file fileb://ehr-spring-core.jar

For automated deployment, use the GitHub Actions workflow or CI/CD pipeline.
EOF

echo "=========================================="
echo "Build completed successfully!"
echo "=========================================="
echo "Deployment package: ${DEPLOY_DIR}/ehr-spring-core.jar"
echo "Size: $(du -h ${DEPLOY_DIR}/ehr-spring-core.jar | cut -f1)"
echo ""
echo "Note: This package needs to be deployed separately from Terraform."
echo "The Lambda function expects the code to be deployed via:"
echo "  1. AWS CLI: aws lambda update-function-code"
echo "  2. GitHub Actions workflow"
echo "  3. CI/CD pipeline"
