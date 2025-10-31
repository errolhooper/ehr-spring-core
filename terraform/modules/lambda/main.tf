# Lambda function module for Spring Boot application

# Security Group for Lambda
resource "aws_security_group" "lambda" {
  name        = "${var.project_name}-${var.environment}-lambda-sg"
  description = "Security group for Lambda function"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-lambda-sg"
  }
}

# Lambda function
resource "aws_lambda_function" "spring_boot_app" {
  function_name = "${var.project_name}-${var.environment}-app"
  role          = var.lambda_execution_role_arn
  
  # For now, using a placeholder. In real deployment, this would point to the JAR file
  # Package the Spring Boot application as a ZIP or use container image
  filename         = var.deployment_package_path
  source_code_hash = var.deployment_package_path != null ? filebase64sha256(var.deployment_package_path) : null
  
  # Alternative: Use container image
  # package_type = "Image"
  # image_uri    = var.image_uri
  
  handler = "org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest"
  runtime = var.runtime
  
  memory_size = var.memory_size
  timeout     = var.timeout
  
  reserved_concurrent_executions = var.reserved_concurrent_executions
  
  vpc_config {
    subnet_ids         = var.private_subnet_ids
    security_group_ids = [aws_security_group.lambda.id]
  }
  
  environment {
    variables = var.environment_variables
  }
  
  tracing_config {
    mode = var.enable_xray ? "Active" : "PassThrough"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-lambda"
  }

  # Ignore changes to source code hash to prevent unnecessary updates
  lifecycle {
    ignore_changes = [
      source_code_hash,
      filename
    ]
  }
}

# CloudWatch Log Group for Lambda
resource "aws_cloudwatch_log_group" "lambda" {
  name              = "/aws/lambda/${aws_lambda_function.spring_boot_app.function_name}"
  retention_in_days = var.log_retention_days

  tags = {
    Name = "${var.project_name}-${var.environment}-lambda-logs"
  }
}

# Lambda function URL (optional - for direct access without API Gateway)
resource "aws_lambda_function_url" "spring_boot_app" {
  count              = var.enable_function_url ? 1 : 0
  function_name      = aws_lambda_function.spring_boot_app.function_name
  authorization_type = "NONE"

  cors {
    allow_origins     = ["*"]
    allow_methods     = ["*"]
    allow_headers     = ["*"]
    expose_headers    = ["*"]
    max_age           = 3600
  }
}

# Lambda Alias for versioning
resource "aws_lambda_alias" "spring_boot_app" {
  name             = var.environment
  function_name    = aws_lambda_function.spring_boot_app.function_name
  function_version = "$LATEST"

  lifecycle {
    ignore_changes = [function_version]
  }
}
