# Main Terraform configuration for EHR Spring Core
# This file orchestrates all the modules to deploy the complete infrastructure

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }

  # Backend configuration - should be customized per environment
  # Uncomment and configure after creating the S3 bucket and DynamoDB table
  # backend "s3" {
  #   bucket         = "ehr-terraform-state"
  #   key            = "terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "ehr-terraform-locks"
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "ehr-spring-core"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# Data source for current AWS account
data "aws_caller_identity" "current" {}

# Data source for availability zones
data "aws_availability_zones" "available" {
  state = "available"
}

# Networking module
module "networking" {
  source = "./modules/networking"

  project_name       = var.project_name
  environment        = var.environment
  vpc_cidr           = var.vpc_cidr
  availability_zones = data.aws_availability_zones.available.names
  enable_nat_gateway = var.enable_nat_gateway
}

# Aurora PostgreSQL module
module "aurora" {
  source = "./modules/aurora"

  project_name             = var.project_name
  environment              = var.environment
  vpc_id                   = module.networking.vpc_id
  database_subnet_ids      = module.networking.database_subnet_ids
  lambda_security_group_id = module.lambda.security_group_id

  database_name   = var.database_name
  master_username = var.master_username
  master_password = var.master_password

  min_capacity            = var.aurora_min_capacity
  max_capacity            = var.aurora_max_capacity
  backup_retention_period = var.backup_retention_period
  preferred_backup_window = var.preferred_backup_window

  enable_http_endpoint = var.enable_aurora_http_endpoint
}

# IAM module for Lambda execution role
module "iam" {
  source = "./modules/iam"

  project_name       = var.project_name
  environment        = var.environment
  aurora_cluster_arn = module.aurora.cluster_arn
}

# Lambda function module
module "lambda" {
  source = "./modules/lambda"

  project_name       = var.project_name
  environment        = var.environment
  vpc_id             = module.networking.vpc_id
  private_subnet_ids = module.networking.private_subnet_ids

  lambda_execution_role_arn = module.iam.lambda_execution_role_arn

  # Environment variables for Spring Boot application
  environment_variables = {
    DB_URL                 = "jdbc:postgresql://${module.aurora.cluster_endpoint}:5432/${var.database_name}"
    DB_USERNAME            = var.master_username
    DB_PASSWORD            = var.master_password
    API_KEY                = var.api_key
    DB_DDL_AUTO            = var.db_ddl_auto
    DB_SHOW_SQL            = var.db_show_sql
    SPRING_PROFILES_ACTIVE = var.environment
  }

  memory_size                    = var.lambda_memory_size
  timeout                        = var.lambda_timeout
  reserved_concurrent_executions = var.lambda_reserved_concurrency
}

# API Gateway module
module "api_gateway" {
  source = "./modules/api-gateway"

  project_name         = var.project_name
  environment          = var.environment
  lambda_function_name = module.lambda.function_name
  lambda_invoke_arn    = module.lambda.invoke_arn

  api_key_required     = var.api_gateway_key_required
  throttle_burst_limit = var.api_throttle_burst_limit
  throttle_rate_limit  = var.api_throttle_rate_limit
}
