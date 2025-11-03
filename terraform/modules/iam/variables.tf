variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "aurora_cluster_arn" {
  description = "ARN of the Aurora cluster"
  type        = string
}
