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

variable "database_subnet_ids" {
  description = "List of database subnet IDs"
  type        = list(string)
}

variable "lambda_security_group_id" {
  description = "Security group ID of Lambda function"
  type        = string
}

variable "database_name" {
  description = "Name of the database"
  type        = string
  default     = "ehrdb"
}

variable "master_username" {
  description = "Master username for Aurora"
  type        = string
  default     = "postgres"
}

variable "master_password" {
  description = "Master password for Aurora (leave null to auto-generate)"
  type        = string
  default     = null
  sensitive   = true
}

variable "engine_version" {
  description = "Aurora PostgreSQL engine version"
  type        = string
  default     = "15.4"
}

variable "min_capacity" {
  description = "Minimum Aurora Serverless v2 capacity (ACU)"
  type        = number
  default     = 0.5
}

variable "max_capacity" {
  description = "Maximum Aurora Serverless v2 capacity (ACU)"
  type        = number
  default     = 2.0
}

variable "instance_count" {
  description = "Number of Aurora instances to create"
  type        = number
  default     = 1
}

variable "backup_retention_period" {
  description = "Number of days to retain backups"
  type        = number
  default     = 7
}

variable "preferred_backup_window" {
  description = "Preferred backup window (UTC)"
  type        = string
  default     = "03:00-04:00"
}

variable "preferred_maintenance_window" {
  description = "Preferred maintenance window (UTC)"
  type        = string
  default     = "sun:04:00-sun:05:00"
}

variable "skip_final_snapshot" {
  description = "Skip final snapshot when deleting cluster"
  type        = bool
  default     = false
}

variable "enable_http_endpoint" {
  description = "Enable HTTP endpoint for Aurora Data API"
  type        = bool
  default     = false
}

variable "enable_performance_insights" {
  description = "Enable Performance Insights"
  type        = bool
  default     = true
}

variable "secret_recovery_window_days" {
  description = "Number of days to recover deleted secrets"
  type        = number
  default     = 7
}
