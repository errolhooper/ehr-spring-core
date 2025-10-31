output "api_id" {
  description = "ID of the API Gateway REST API"
  value       = aws_api_gateway_rest_api.main.id
}

output "api_arn" {
  description = "ARN of the API Gateway REST API"
  value       = aws_api_gateway_rest_api.main.arn
}

output "api_endpoint" {
  description = "Endpoint of the API Gateway REST API"
  value       = aws_api_gateway_rest_api.main.execution_arn
}

output "api_invoke_url" {
  description = "Invoke URL for the API Gateway"
  value       = "${aws_api_gateway_stage.main.invoke_url}"
}

output "stage_name" {
  description = "Name of the API Gateway stage"
  value       = aws_api_gateway_stage.main.stage_name
}

output "stage_arn" {
  description = "ARN of the API Gateway stage"
  value       = aws_api_gateway_stage.main.arn
}

output "api_key" {
  description = "API Gateway API key value"
  value       = var.api_key_required ? aws_api_gateway_api_key.main[0].value : null
  sensitive   = true
}

output "api_key_id" {
  description = "ID of the API Gateway API key"
  value       = var.api_key_required ? aws_api_gateway_api_key.main[0].id : null
}
