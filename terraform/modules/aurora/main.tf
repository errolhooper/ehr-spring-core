# Aurora PostgreSQL Serverless v2 module

# DB Subnet Group
resource "aws_db_subnet_group" "aurora" {
  name       = "${var.project_name}-${var.environment}-aurora-subnet-group"
  subnet_ids = var.database_subnet_ids

  tags = {
    Name = "${var.project_name}-${var.environment}-aurora-subnet-group"
  }
}

# Security Group for Aurora
resource "aws_security_group" "aurora" {
  name        = "${var.project_name}-${var.environment}-aurora-sg"
  description = "Security group for Aurora PostgreSQL cluster"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [var.lambda_security_group_id]
    description     = "PostgreSQL access from Lambda"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-aurora-sg"
  }
}

# Random password for Aurora (if not provided)
resource "random_password" "aurora_master_password" {
  count   = var.master_password == null ? 1 : 0
  length  = 32
  special = true
}

# Aurora Cluster
resource "aws_rds_cluster" "aurora" {
  cluster_identifier      = "${var.project_name}-${var.environment}-aurora-cluster"
  engine                  = "aurora-postgresql"
  engine_mode             = "provisioned"
  engine_version          = var.engine_version
  database_name           = var.database_name
  master_username         = var.master_username
  master_password         = var.master_password != null ? var.master_password : random_password.aurora_master_password[0].result
  
  db_subnet_group_name    = aws_db_subnet_group.aurora.name
  vpc_security_group_ids  = [aws_security_group.aurora.id]
  
  backup_retention_period = var.backup_retention_period
  preferred_backup_window = var.preferred_backup_window
  preferred_maintenance_window = var.preferred_maintenance_window
  
  skip_final_snapshot     = var.skip_final_snapshot
  final_snapshot_identifier = var.skip_final_snapshot ? null : "${var.project_name}-${var.environment}-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"
  
  storage_encrypted       = true
  
  enabled_cloudwatch_logs_exports = ["postgresql"]
  
  serverlessv2_scaling_configuration {
    min_capacity = var.min_capacity
    max_capacity = var.max_capacity
  }

  enable_http_endpoint    = var.enable_http_endpoint

  tags = {
    Name = "${var.project_name}-${var.environment}-aurora-cluster"
  }

  lifecycle {
    ignore_changes = [final_snapshot_identifier]
  }
}

# Aurora Cluster Instance
resource "aws_rds_cluster_instance" "aurora" {
  count              = var.instance_count
  identifier         = "${var.project_name}-${var.environment}-aurora-instance-${count.index + 1}"
  cluster_identifier = aws_rds_cluster.aurora.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.aurora.engine
  engine_version     = aws_rds_cluster.aurora.engine_version
  
  publicly_accessible = false
  
  performance_insights_enabled = var.enable_performance_insights
  
  tags = {
    Name = "${var.project_name}-${var.environment}-aurora-instance-${count.index + 1}"
  }
}

# Store password in AWS Secrets Manager
resource "aws_secretsmanager_secret" "aurora_credentials" {
  name                    = "${var.project_name}-${var.environment}-aurora-credentials"
  description             = "Aurora PostgreSQL master credentials"
  recovery_window_in_days = var.secret_recovery_window_days

  tags = {
    Name = "${var.project_name}-${var.environment}-aurora-credentials"
  }
}

resource "aws_secretsmanager_secret_version" "aurora_credentials" {
  secret_id = aws_secretsmanager_secret.aurora_credentials.id
  secret_string = jsonencode({
    username            = var.master_username
    password            = var.master_password != null ? var.master_password : random_password.aurora_master_password[0].result
    engine              = "postgres"
    host                = aws_rds_cluster.aurora.endpoint
    port                = 5432
    dbname              = var.database_name
    dbClusterIdentifier = aws_rds_cluster.aurora.cluster_identifier
  })
}
