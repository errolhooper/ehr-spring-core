variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs"
  type        = list(string)
}

variable "lambda_execution_role_arn" {
  description = "ARN of Lambda execution role"
  type        = string
}

variable "deployment_package_path" {
  description = "Path to Lambda deployment package (ZIP file)"
  type        = string
  default     = null
}

variable "image_uri" {
  description = "URI of container image (alternative to deployment package)"
  type        = string
  default     = null
}

variable "runtime" {
  description = "Lambda runtime"
  type        = string
  default     = "java17"
}

variable "handler" {
  description = "Lambda handler"
  type        = string
  default     = "org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest"
}

variable "memory_size" {
  description = "Memory size for Lambda function in MB"
  type        = number
  default     = 1024
}

variable "timeout" {
  description = "Timeout for Lambda function in seconds"
  type        = number
  default     = 60
}

variable "reserved_concurrent_executions" {
  description = "Reserved concurrent executions"
  type        = number
  default     = -1
}

variable "environment_variables" {
  description = "Environment variables for Lambda function"
  type        = map(string)
  default     = {}
}

variable "log_retention_days" {
  description = "CloudWatch Logs retention in days"
  type        = number
  default     = 7
}

variable "enable_xray" {
  description = "Enable AWS X-Ray tracing"
  type        = bool
  default     = true
}

variable "enable_function_url" {
  description = "Enable Lambda function URL"
  type        = bool
  default     = false
}
