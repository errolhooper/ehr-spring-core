# IAM roles and policies for Lambda and other services

# Lambda Execution Role
resource "aws_iam_role" "lambda_execution" {
  name               = "${var.project_name}-${var.environment}-lambda-execution-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json

  tags = {
    Name = "${var.project_name}-${var.environment}-lambda-execution-role"
  }
}

# Lambda Assume Role Policy Document
data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    effect  = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

# Attach AWS Managed Policy for Lambda VPC execution
resource "aws_iam_role_policy_attachment" "lambda_vpc_execution" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

# Attach AWS Managed Policy for Lambda basic execution
resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Custom Policy for Aurora access
resource "aws_iam_policy" "lambda_aurora_access" {
  name        = "${var.project_name}-${var.environment}-lambda-aurora-access"
  description = "Policy for Lambda to access Aurora"
  policy      = data.aws_iam_policy_document.lambda_aurora_access.json

  tags = {
    Name = "${var.project_name}-${var.environment}-lambda-aurora-access"
  }
}

data "aws_iam_policy_document" "lambda_aurora_access" {
  statement {
    actions = [
      "rds-db:connect"
    ]
    effect = "Allow"
    resources = [
      var.aurora_cluster_arn
    ]
  }

  statement {
    actions = [
      "rds:DescribeDBClusters",
      "rds:DescribeDBInstances"
    ]
    effect    = "Allow"
    resources = ["*"]
  }
}

# Attach custom Aurora policy to Lambda role
resource "aws_iam_role_policy_attachment" "lambda_aurora_access" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_aurora_access.arn
}

# Custom Policy for Secrets Manager access
resource "aws_iam_policy" "lambda_secrets_manager" {
  name        = "${var.project_name}-${var.environment}-lambda-secrets-manager"
  description = "Policy for Lambda to access Secrets Manager"
  policy      = data.aws_iam_policy_document.lambda_secrets_manager.json

  tags = {
    Name = "${var.project_name}-${var.environment}-lambda-secrets-manager"
  }
}

data "aws_iam_policy_document" "lambda_secrets_manager" {
  statement {
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret"
    ]
    effect = "Allow"
    resources = [
      "arn:aws:secretsmanager:*:*:secret:${var.project_name}-${var.environment}-*"
    ]
  }
}

# Attach Secrets Manager policy to Lambda role
resource "aws_iam_role_policy_attachment" "lambda_secrets_manager" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_secrets_manager.arn
}

# CloudWatch Logs Policy
resource "aws_iam_policy" "lambda_cloudwatch_logs" {
  name        = "${var.project_name}-${var.environment}-lambda-cloudwatch-logs"
  description = "Policy for Lambda CloudWatch Logs access"
  policy      = data.aws_iam_policy_document.lambda_cloudwatch_logs.json

  tags = {
    Name = "${var.project_name}-${var.environment}-lambda-cloudwatch-logs"
  }
}

data "aws_iam_policy_document" "lambda_cloudwatch_logs" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    effect = "Allow"
    resources = [
      "arn:aws:logs:*:*:log-group:/aws/lambda/${var.project_name}-${var.environment}-*"
    ]
  }
}

# Attach CloudWatch Logs policy to Lambda role
resource "aws_iam_role_policy_attachment" "lambda_cloudwatch_logs" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_cloudwatch_logs.arn
}
