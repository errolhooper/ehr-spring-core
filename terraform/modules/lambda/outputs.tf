output "function_name" {
  description = "Name of the Lambda function"
  value       = aws_lambda_function.spring_boot_app.function_name
}

output "function_arn" {
  description = "ARN of the Lambda function"
  value       = aws_lambda_function.spring_boot_app.arn
}

output "invoke_arn" {
  description = "Invoke ARN of the Lambda function"
  value       = aws_lambda_function.spring_boot_app.invoke_arn
}

output "security_group_id" {
  description = "ID of the Lambda security group"
  value       = aws_security_group.lambda.id
}

output "function_url" {
  description = "Lambda function URL (if enabled)"
  value       = var.enable_function_url ? aws_lambda_function_url.spring_boot_app[0].function_url : null
}

output "alias_arn" {
  description = "ARN of the Lambda alias"
  value       = aws_lambda_alias.spring_boot_app.arn
}

output "log_group_name" {
  description = "Name of the CloudWatch Log Group"
  value       = aws_cloudwatch_log_group.lambda.name
}
