# Python Cookie Reader

This Python script reads and extracts cookies from websites, specifically designed for news websites.

## Requirements

Install the required dependencies:

```bash
pip3 install -r requirements.txt
```

## Usage

The script can be run standalone or is executed automatically by the Java service:

```bash
python3 cookie_reader.py <url>
```

Example:
```bash
python3 cookie_reader.py "https://www.example.com"
```

## Output

The script outputs a JSON object with the following structure:

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
      "value": "abc123",
      "domain": ".example.com",
      "path": "/",
      "secure": true,
      "httpOnly": true,
      "expires": null
    }
  ],
  "timestamp": null
}
```

## Integration with Spring Boot API

The Python script is called by the `CookieScanService` Java service when a request is made to the `/api/v1/cookies/scan` endpoint.
