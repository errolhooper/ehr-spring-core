# Cookie Scanner Feature

## Overview

The Cookie Scanner is a Python-powered feature integrated into the EHR Spring Core API that allows scanning websites (particularly news websites) to extract and analyze cookies. This feature is designed for analyzing cookie usage on customer websites.

## Features

- **Python-based Cookie Extraction**: Uses the `requests` library to fetch websites and extract cookies
- **REST API Integration**: Seamlessly integrated into the existing Spring Boot API
- **Database Persistence**: All scan results are stored in PostgreSQL for historical analysis
- **Security**: URL validation prevents command injection attacks
- **Configurable**: Python script path and timeout are configurable via application properties
- **Comprehensive Testing**: Full test coverage with mocked service tests

## API Endpoint

### Scan Website for Cookies

**Endpoint**: `POST /api/v1/cookies/scan`

**Authentication**: Requires `X-API-Key` header

**Request Body**:
```json
{
  "url": "https://www.example.com"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "url": "https://www.example.com",
  "finalUrl": "https://www.example.com/",
  "statusCode": 200,
  "cookieCount": 5,
  "cookies": [
    {
      "name": "session_id",
      "value": "abc123xyz",
      "domain": ".example.com",
      "path": "/",
      "secure": true,
      "httpOnly": true,
      "expires": null
    }
  ],
  "timestamp": "2025-12-03T21:00:00Z",
  "message": "Cookie scan completed successfully"
}
```

**Error Response** (200 OK with success: false):
```json
{
  "success": false,
  "url": "https://www.example.com",
  "error": "Connection timeout",
  "timestamp": "2025-12-03T21:00:00Z",
  "message": "Cookie scan failed"
}
```

## Example Usage

### Using cURL

```bash
# Set your API key
API_KEY="default-api-key-change-in-production"

# Scan a news website
curl -X POST http://localhost:8080/api/v1/cookies/scan \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "url": "https://www.bbc.com/news"
  }'
```

### Using Python

```python
import requests

api_key = "default-api-key-change-in-production"
headers = {
    "Content-Type": "application/json",
    "X-API-Key": api_key
}

data = {
    "url": "https://www.bbc.com/news"
}

response = requests.post(
    "http://localhost:8080/api/v1/cookies/scan",
    headers=headers,
    json=data
)

print(response.json())
```

## Configuration

The cookie scanner can be configured in `application.yml`:

```yaml
cookie-scanner:
  python-script-path: ${COOKIE_SCANNER_SCRIPT_PATH:src/main/python/cookie_reader.py}
  timeout-seconds: 30
```

### Environment Variables

- `COOKIE_SCANNER_SCRIPT_PATH`: Override the default Python script location
- `API_KEY`: Set the API key for authentication (required for production)

## Python Setup

### Prerequisites

- Python 3.8 or higher
- `requests` library

### Installation

```bash
# Navigate to the Python scripts directory
cd src/main/python

# Install dependencies
pip3 install -r requirements.txt
```

Or install globally:

```bash
pip3 install requests==2.31.0
```

## Database Schema

The cookie scanner creates a `cookie_scans` table with the following structure:

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key (auto-increment) |
| url | VARCHAR(2048) | The URL that was scanned |
| final_url | VARCHAR(2048) | Final URL after redirects |
| success | BOOLEAN | Whether the scan was successful |
| status_code | INTEGER | HTTP status code |
| cookie_count | INTEGER | Number of cookies found |
| cookies_json | TEXT | JSON array of cookie data |
| error | VARCHAR(1024) | Error message if scan failed |
| timestamp | TIMESTAMP | When the scan was performed |

## Security Features

1. **URL Validation**: Only allows `http://` and `https://` schemes
2. **URI Parsing**: Uses Java's `URI` class to validate and sanitize URLs
3. **Timeout Protection**: Prevents long-running processes (configurable, default 30 seconds)
4. **Process Isolation**: Python script runs in a separate process
5. **Error Handling**: Comprehensive error handling and logging
6. **API Authentication**: All endpoints require API key authentication

## Testing

The feature includes comprehensive unit tests:

```bash
# Run all tests
mvn test

# Run only cookie scanner tests
mvn test -Dtest=CookieScanControllerTest
```

## Architecture

```
┌─────────────────┐
│  REST Client    │
└────────┬────────┘
         │ POST /api/v1/cookies/scan
         │
┌────────▼────────────────────────┐
│  CookieScanController           │
│  - Validates request            │
│  - Returns response             │
└────────┬────────────────────────┘
         │
┌────────▼────────────────────────┐
│  CookieScanService              │
│  - Validates URL                │
│  - Executes Python script       │
│  - Parses JSON response         │
│  - Stores results in DB         │
└────────┬────────────────────────┘
         │
┌────────▼────────────────────────┐
│  Python Process                 │
│  (cookie_reader.py)             │
│  - Fetches website              │
│  - Extracts cookies             │
│  - Returns JSON                 │
└────────┬────────────────────────┘
         │
┌────────▼────────────────────────┐
│  PostgreSQL Database            │
│  (cookie_scans table)           │
└─────────────────────────────────┘
```

## Troubleshooting

### Python script not found

**Error**: `Cookie reader script not found`

**Solution**: Verify the Python script path in your configuration:
```yaml
cookie-scanner:
  python-script-path: src/main/python/cookie_reader.py
```

### Python dependencies missing

**Error**: `ModuleNotFoundError: No module named 'requests'`

**Solution**: Install Python dependencies:
```bash
pip3 install -r src/main/python/requirements.txt
```

### Timeout errors

**Error**: `Request timeout - process took too long`

**Solution**: Increase the timeout in `application.yml`:
```yaml
cookie-scanner:
  timeout-seconds: 60  # Increase from default 30 seconds
```

## Limitations

1. The cookie scanner can only extract cookies from the HTTP response headers
2. Cookies set by JavaScript after page load are not captured
3. Network restrictions may prevent access to some websites
4. The feature requires Python 3.8+ to be installed on the server

## Future Enhancements

Possible future improvements:
- Add support for JavaScript-rendered pages using Selenium or Playwright
- Implement cookie classification (tracking, functional, etc.)
- Add batch scanning for multiple URLs
- Implement rate limiting for external requests
- Add cookie change detection and alerting
- Support for custom headers and authentication

## Support

For issues or questions, please refer to the main project README or open an issue in the repository.
