# Output values for EHR Spring Core infrastructure

output "vpc_id" {
  description = "ID of the VPC"
  value       = module.networking.vpc_id
}

output "vpc_cidr" {
  description = "CIDR block of the VPC"
  value       = module.networking.vpc_cidr
}

output "private_subnet_ids" {
  description = "IDs of private subnets"
  value       = module.networking.private_subnet_ids
}

output "public_subnet_ids" {
  description = "IDs of public subnets"
  value       = module.networking.public_subnet_ids
}

output "database_subnet_ids" {
  description = "IDs of database subnets"
  value       = module.networking.database_subnet_ids
}

# Aurora outputs
output "aurora_cluster_id" {
  description = "ID of the Aurora cluster"
  value       = module.aurora.cluster_id
}

output "aurora_cluster_endpoint" {
  description = "Writer endpoint for the Aurora cluster"
  value       = module.aurora.cluster_endpoint
}

output "aurora_reader_endpoint" {
  description = "Reader endpoint for the Aurora cluster"
  value       = module.aurora.reader_endpoint
}

output "aurora_database_name" {
  description = "Name of the database"
  value       = module.aurora.database_name
}

# Lambda outputs
output "lambda_function_name" {
  description = "Name of the Lambda function"
  value       = module.lambda.function_name
}

output "lambda_function_arn" {
  description = "ARN of the Lambda function"
  value       = module.lambda.function_arn
}

output "lambda_invoke_arn" {
  description = "Invoke ARN of the Lambda function"
  value       = module.lambda.invoke_arn
}

# API Gateway outputs
output "api_gateway_id" {
  description = "ID of the API Gateway REST API"
  value       = module.api_gateway.api_id
}

output "api_gateway_url" {
  description = "Invoke URL for the API Gateway"
  value       = module.api_gateway.api_invoke_url
}

output "api_gateway_stage_name" {
  description = "Name of the API Gateway stage"
  value       = module.api_gateway.stage_name
}

# IAM outputs
output "lambda_execution_role_arn" {
  description = "ARN of the Lambda execution role"
  value       = module.iam.lambda_execution_role_arn
}

output "lambda_execution_role_name" {
  description = "Name of the Lambda execution role"
  value       = module.iam.lambda_execution_role_name
}

# Connection details (for convenience)
output "connection_details" {
  description = "Connection details for the deployed application"
  value = {
    api_endpoint    = module.api_gateway.api_invoke_url
    database_host   = module.aurora.cluster_endpoint
    database_port   = 5432
    database_name   = module.aurora.database_name
  }
}
