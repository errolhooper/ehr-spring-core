# Variable definitions for EHR Spring Core Terraform configuration

variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "ehr-spring-core"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

# Networking variables
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway for private subnets"
  type        = bool
  default     = true
}

# Aurora PostgreSQL variables
variable "database_name" {
  description = "Name of the PostgreSQL database"
  type        = string
  default     = "ehrdb"
}

variable "master_username" {
  description = "Master username for Aurora PostgreSQL"
  type        = string
  default     = "postgres"
  sensitive   = true
}

variable "master_password" {
  description = "Master password for Aurora PostgreSQL"
  type        = string
  sensitive   = true
}

variable "aurora_min_capacity" {
  description = "Minimum Aurora Serverless v2 capacity (ACU)"
  type        = number
  default     = 0.5
}

variable "aurora_max_capacity" {
  description = "Maximum Aurora Serverless v2 capacity (ACU)"
  type        = number
  default     = 2.0
}

variable "backup_retention_period" {
  description = "Number of days to retain automated backups"
  type        = number
  default     = 7
}

variable "preferred_backup_window" {
  description = "Preferred backup window (UTC)"
  type        = string
  default     = "03:00-04:00"
}

variable "enable_aurora_http_endpoint" {
  description = "Enable HTTP endpoint for Aurora Data API"
  type        = bool
  default     = false
}

# Lambda variables
variable "lambda_memory_size" {
  description = "Memory size for Lambda function in MB"
  type        = number
  default     = 1024
}

variable "lambda_timeout" {
  description = "Timeout for Lambda function in seconds"
  type        = number
  default     = 60
}

variable "lambda_reserved_concurrency" {
  description = "Reserved concurrent executions for Lambda"
  type        = number
  default     = -1  # -1 means unreserved
}

# Application variables
variable "api_key" {
  description = "API key for Spring Boot application authentication"
  type        = string
  sensitive   = true
}

variable "db_ddl_auto" {
  description = "Hibernate DDL auto mode (none, validate, update, create, create-drop)"
  type        = string
  default     = "validate"
}

variable "db_show_sql" {
  description = "Show SQL queries in logs"
  type        = string
  default     = "false"
}

# API Gateway variables
variable "api_gateway_key_required" {
  description = "Require API key for API Gateway endpoints"
  type        = bool
  default     = true
}

variable "api_throttle_burst_limit" {
  description = "API Gateway throttling burst limit"
  type        = number
  default     = 100
}

variable "api_throttle_rate_limit" {
  description = "API Gateway throttling rate limit"
  type        = number
  default     = 50
}
