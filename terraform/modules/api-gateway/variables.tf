variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "lambda_function_name" {
  description = "Name of the Lambda function"
  type        = string
}

variable "lambda_invoke_arn" {
  description = "Invoke ARN of the Lambda function"
  type        = string
}

variable "api_key_required" {
  description = "Require API key for endpoints"
  type        = bool
  default     = true
}

variable "throttle_burst_limit" {
  description = "API Gateway throttling burst limit"
  type        = number
  default     = 100
}

variable "throttle_rate_limit" {
  description = "API Gateway throttling rate limit"
  type        = number
  default     = 50
}

variable "quota_limit" {
  description = "API Gateway quota limit"
  type        = number
  default     = 10000
}

variable "quota_period" {
  description = "API Gateway quota period (DAY, WEEK, MONTH)"
  type        = string
  default     = "DAY"
}

variable "log_retention_days" {
  description = "CloudWatch Logs retention in days"
  type        = number
  default     = 7
}

variable "enable_xray_tracing" {
  description = "Enable X-Ray tracing"
  type        = bool
  default     = true
}

variable "enable_data_trace" {
  description = "Enable data trace logging"
  type        = bool
  default     = false
}
