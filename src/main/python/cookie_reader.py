#!/usr/bin/env python3
"""
Website Cookie Reader
Reads and extracts cookies from websites for analysis.
"""

import sys
import json
import requests
from urllib.parse import urlparse


def read_website_cookies(url):
    """
    Fetch a website and extract all cookies from the response.
    
    Args:
        url (str): The URL of the website to scan
        
    Returns:
        dict: A dictionary containing cookies and metadata
    """
    try:
        # Parse the URL to validate it
        parsed_url = urlparse(url)
        if not parsed_url.scheme or not parsed_url.netloc:
            return {
                "success": False,
                "error": "Invalid URL format",
                "url": url
            }
        
        # Set headers to mimic a real browser
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5',
        }
        
        # Make the request with a timeout
        response = requests.get(url, headers=headers, timeout=10, allow_redirects=True)
        
        # Extract cookies from the response
        cookies = []
        for cookie in response.cookies:
            # Detect HttpOnly flag properly
            http_only = False
            if hasattr(cookie, '_rest'):
                http_only = cookie._rest.get('HttpOnly', False) if cookie._rest else False
            
            cookie_data = {
                "name": cookie.name,
                "value": cookie.value,
                "domain": cookie.domain,
                "path": cookie.path,
                "secure": cookie.secure,
                "httpOnly": http_only,
                "expires": None  # Cookie expiration not always available
            }
            cookies.append(cookie_data)
        
        # Build the result
        result = {
            "success": True,
            "url": url,
            "finalUrl": response.url,
            "statusCode": response.status_code,
            "cookieCount": len(cookies),
            "cookies": cookies,
            "timestamp": None  # Will be set by Java service
        }
        
        return result
        
    except requests.exceptions.Timeout:
        return {
            "success": False,
            "error": "Request timeout",
            "url": url
        }
    except requests.exceptions.RequestException as e:
        return {
            "success": False,
            "error": f"Request failed: {str(e)}",
            "url": url
        }
    except Exception as e:
        return {
            "success": False,
            "error": f"Unexpected error: {str(e)}",
            "url": url
        }


def main():
    """
    Main entry point for the script.
    Reads URL from command line argument and outputs JSON result.
    """
    if len(sys.argv) < 2:
        result = {
            "success": False,
            "error": "No URL provided. Usage: cookie_reader.py <url>"
        }
        print(json.dumps(result))
        sys.exit(1)
    
    url = sys.argv[1]
    result = read_website_cookies(url)
    
    # Output result as JSON
    print(json.dumps(result, indent=2))
    
    # Exit with appropriate code
    sys.exit(0 if result["success"] else 1)


if __name__ == "__main__":
    main()
