# EHR Spring Core

Core Spring Boot application for the EHR (Engineering & Innovation Hub) platform. This service provides analytics ingestion endpoints for events and metrics with built-in validation, authentication, and monitoring.

## Features

- **Analytics Ingestion**: REST endpoints for ingesting events and metrics
- **PostgreSQL Persistence**: Store events and metrics in PostgreSQL/Amazon Aurora
- **API Security**: X-API-Key authentication for all API endpoints
- **Validation**: Request validation using Jakarta Bean Validation
- **Health Monitoring**: Spring Boot Actuator health checks
- **API Documentation**: Interactive Swagger UI for API exploration
- **In-Memory Logging**: Configurable payload storage for debugging
- **CI/CD Pipeline**: Automated deployment to AWS Lambda, ECS, or Elastic Beanstalk

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Building the Application

```bash
mvn clean package
```

### Running the Application

```bash
java -jar target/ehr-spring-core-1.0.0.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Configuration

Configure the application via environment variables or `src/main/resources/application.yml`:

#### Database Configuration

For production with Amazon Aurora PostgreSQL:

```bash
export DB_URL="jdbc:postgresql://your-aurora-endpoint.region.rds.amazonaws.com:5432/ehrdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_secure_password"
```

For local development with PostgreSQL:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/ehrdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
```

See [AURORA_SETUP.md](AURORA_SETUP.md) for detailed Amazon Aurora PostgreSQL setup instructions.

#### API Security Configuration

```yaml
# API Key (set via environment variable for production)
security:
  api-key: ${API_KEY:default-api-key-change-in-production}

# Payload logging configuration
logging:
  payloads:
    enabled: true
    max-size: 1000

# Database configuration
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/ehrdb}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

**Important**: Always set the `API_KEY` environment variable in production:

```bash
export API_KEY=your-secure-api-key
java -jar target/ehr-spring-core-1.0.0.jar
```

## API Endpoints

### Ingest Event

```bash
POST /api/v1/ingest/events
Content-Type: application/json
X-API-Key: your-api-key

{
  "eventName": "user.login",
  "timestamp": "2025-10-30T19:52:00Z",
  "properties": {
    "userId": "123",
    "action": "login"
  }
}
```

### Ingest Metric

```bash
POST /api/v1/ingest/metrics
Content-Type: application/json
X-API-Key: your-api-key

{
  "metricName": "cpu.usage",
  "value": 75.5,
  "timestamp": "2025-10-30T19:52:00Z",
  "unit": "percent"
}
```

### Health Check

```bash
GET /actuator/health
```

## API Documentation

Access the interactive Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI specification available at:

```
http://localhost:8080/api-docs
```

## Example Usage

### Using cURL

```bash
# Set your API key
API_KEY="default-api-key-change-in-production"

# Ingest an event
curl -X POST http://localhost:8080/api/v1/ingest/events \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "eventName": "user.login",
    "timestamp": "2025-10-30T19:52:00Z",
    "properties": {
      "userId": "123",
      "action": "login"
    }
  }'

# Ingest a metric
curl -X POST http://localhost:8080/api/v1/ingest/metrics \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "metricName": "cpu.usage",
    "value": 75.5,
    "timestamp": "2025-10-30T19:52:00Z",
    "unit": "percent"
  }'

# Check health
curl http://localhost:8080/actuator/health
```

## Testing

Run the test suite:

```bash
mvn test
```

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/ehr/springcore/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── exception/       # Exception handlers
│   │   ├── model/           # DTOs and models
│   │   ├── security/        # Security filters
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.yml  # Application configuration
└── test/                    # Test classes
```

## Security

- All `/api/*` endpoints require X-API-Key authentication
- Actuator and Swagger endpoints are public
- Default API key should never be used in production
- Application logs a warning when using the default API key

## Infrastructure as Code

This project includes comprehensive Terraform configurations for deploying to AWS. The infrastructure includes:

- **VPC and Networking**: Multi-AZ setup with public, private, and database subnets
- **Aurora PostgreSQL**: Serverless v2 database cluster with automated backups
- **Lambda Functions**: Serverless compute for the Spring Boot application
- **API Gateway**: REST API endpoint with throttling and monitoring
- **IAM Roles**: Secure access control with least-privilege policies

### Quick Deploy

```bash
# Setup Terraform backend (one-time)
cd terraform
./scripts/setup-backend.sh dev us-east-1

# Configure secrets
cd environments/dev
cp secrets.tfvars.example secrets.tfvars
# Edit secrets.tfvars with your values

# Deploy infrastructure
cd ../..
./scripts/deploy.sh dev plan
./scripts/deploy.sh dev apply
```

For comprehensive infrastructure documentation, see [TERRAFORM.md](TERRAFORM.md).

## License

This is part of the EHR (Engineering & Innovation Hub) platform.
