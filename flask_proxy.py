#!/usr/bin/env python3
"""
Augment Firmware Flask Proxy
Intercepterer alle requests, logger GraphQL queries, og gemmer firmware automatisk!
"""

from flask import Flask, render_template_string, jsonify, request as flask_request
from flask_cors import CORS
import requests
import json
import os
import re
from datetime import datetime
from threading import Thread, Lock
import logging

app = Flask(__name__)
CORS(app)

# Configuration
OUTPUT_DIR = "captured_data"
FIRMWARE_DIR = os.path.join(OUTPUT_DIR, "firmware")
LOGS_DIR = os.path.join(OUTPUT_DIR, "logs")

# Create directories
os.makedirs(FIRMWARE_DIR, exist_ok=True)
os.makedirs(LOGS_DIR, exist_ok=True)

# Storage
captured_requests = []
firmware_urls = []
graphql_queries = []
data_lock = Lock()

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(os.path.join(LOGS_DIR, f'proxy_{datetime.now().strftime("%Y%m%d_%H%M%S")}.log')),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# HTML Dashboard Template
DASHBOARD_HTML = """
<!DOCTYPE html>
<html>
<head>
    <title>🔍 Augment Firmware Proxy Dashboard</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            padding: 20px;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
        }
        h1 {
            color: white;
            text-align: center;
            margin-bottom: 30px;
            font-size: 2.5em;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            text-align: center;
            transition: transform 0.3s;
        }
        .stat-card:hover {
            transform: translateY(-5px);
        }
        .stat-number {
            font-size: 3em;
            font-weight: bold;
            color: #667eea;
            margin-bottom: 10px;
        }
        .stat-label {
            font-size: 1.1em;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        .section {
            background: white;
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }
        h2 {
            color: #667eea;
            margin-bottom: 20px;
            font-size: 1.8em;
            border-bottom: 3px solid #667eea;
            padding-bottom: 10px;
        }
        .firmware-url {
            background: #f8f9fa;
            border-left: 5px solid #28a745;
            padding: 20px;
            margin-bottom: 15px;
            border-radius: 8px;
            font-family: 'Courier New', monospace;
            word-break: break-all;
            transition: background 0.3s;
        }
        .firmware-url:hover {
            background: #e9ecef;
        }
        .firmware-url strong {
            display: block;
            color: #28a745;
            margin-bottom: 10px;
            font-size: 1.1em;
        }
        .request-item {
            background: #f8f9fa;
            border-left: 5px solid #007bff;
            padding: 15px;
            margin-bottom: 15px;
            border-radius: 8px;
            font-size: 0.9em;
        }
        .request-item .method {
            display: inline-block;
            background: #007bff;
            color: white;
            padding: 5px 15px;
            border-radius: 5px;
            font-weight: bold;
            margin-right: 10px;
        }
        .request-item .url {
            color: #666;
            word-break: break-all;
        }
        .graphql-query {
            background: #f8f9fa;
            border-left: 5px solid #ffc107;
            padding: 20px;
            margin-bottom: 15px;
            border-radius: 8px;
            font-family: 'Courier New', monospace;
        }
        .graphql-query strong {
            display: block;
            color: #ffc107;
            margin-bottom: 10px;
            font-size: 1.1em;
        }
        .graphql-query pre {
            background: white;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
            font-size: 0.85em;
        }
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #999;
            font-size: 1.2em;
        }
        .empty-state svg {
            width: 100px;
            height: 100px;
            margin-bottom: 20px;
            opacity: 0.3;
        }
        .refresh-btn {
            position: fixed;
            bottom: 30px;
            right: 30px;
            background: #667eea;
            color: white;
            border: none;
            padding: 18px 35px;
            border-radius: 50px;
            font-size: 1.1em;
            cursor: pointer;
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
            transition: all 0.3s;
            font-weight: bold;
        }
        .refresh-btn:hover {
            background: #5568d3;
            transform: translateY(-3px);
            box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6);
        }
        .badge {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 0.75em;
            margin-left: 10px;
            font-weight: bold;
        }
        .timestamp {
            color: #999;
            font-size: 0.85em;
            margin-top: 5px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔍 Augment Firmware Proxy Dashboard</h1>

        <div class="stats">
            <div class="stat-card">
                <div class="stat-number" id="total-requests">0</div>
                <div class="stat-label">Total Requests</div>
            </div>
            <div class="stat-card">
                <div class="stat-number" id="firmware-count">0</div>
                <div class="stat-label">Firmware URLs</div>
            </div>
            <div class="stat-card">
                <div class="stat-number" id="graphql-count">0</div>
                <div class="stat-label">GraphQL Queries</div>
            </div>
        </div>

        <div class="section" id="firmware-section">
            <h2>🎯 Captured Firmware URLs <span class="badge" id="fw-badge">0</span></h2>
            <div id="firmware-list">
                <div class="empty-state">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                    </svg>
                    <p>Ingen firmware URLs fanget endnu...</p>
                    <p style="font-size: 0.9em; margin-top: 10px;">Trigger firmware update i Augment app'en!</p>
                </div>
            </div>
        </div>

        <div class="section" id="graphql-section">
            <h2>📊 GraphQL Queries <span class="badge" id="gql-badge">0</span></h2>
            <div id="graphql-list">
                <div class="empty-state">
                    <p>Ingen GraphQL queries fanget endnu...</p>
                </div>
            </div>
        </div>

        <div class="section" id="requests-section">
            <h2>📡 Recent Requests <span class="badge" id="req-badge">0</span></h2>
            <div id="requests-list">
                <div class="empty-state">
                    <p>Ingen requests fanget endnu...</p>
                </div>
            </div>
        </div>
    </div>

    <button class="refresh-btn" onclick="loadData()">🔄 Refresh</button>

    <script>
        function loadData() {
            fetch('/api/status')
                .then(r => r.json())
                .then(data => {
                    // Update stats
                    document.getElementById('total-requests').textContent = data.total_requests;
                    document.getElementById('firmware-count').textContent = data.firmware_urls.length;
                    document.getElementById('graphql-count').textContent = data.graphql_queries.length;

                    // Update badges
                    document.getElementById('fw-badge').textContent = data.firmware_urls.length;
                    document.getElementById('gql-badge').textContent = data.graphql_queries.length;
                    document.getElementById('req-badge').textContent = Math.min(data.total_requests, 50);

                    // Update firmware URLs
                    const fwList = document.getElementById('firmware-list');
                    if (data.firmware_urls.length > 0) {
                        fwList.innerHTML = data.firmware_urls.map(item => `
                            <div class="firmware-url">
                                <strong>🎯 Firmware URL fundet!</strong>
                                <div style="margin: 10px 0;">${item.url}</div>
                                <div class="timestamp">Fanget: ${item.timestamp}</div>
                            </div>
                        `).join('');
                    }

                    // Update GraphQL queries
                    const gqlList = document.getElementById('graphql-list');
                    if (data.graphql_queries.length > 0) {
                        gqlList.innerHTML = data.graphql_queries.map(item => `
                            <div class="graphql-query">
                                <strong>📊 GraphQL Query</strong>
                                <pre>${JSON.stringify(item.query, null, 2)}</pre>
                                <div class="timestamp">${item.timestamp}</div>
                            </div>
                        `).join('');
                    }

                    // Update requests
                    const reqList = document.getElementById('requests-list');
                    if (data.recent_requests.length > 0) {
                        reqList.innerHTML = data.recent_requests.map(item => `
                            <div class="request-item">
                                <span class="method">${item.method}</span>
                                <span class="url">${item.url}</span>
                                <div class="timestamp">${item.timestamp}</div>
                            </div>
                        `).join('');
                    }
                });
        }

        // Load data on page load
        loadData();

        // Auto-refresh every 2 seconds
        setInterval(loadData, 2000);
    </script>
</body>
</html>
"""

def is_firmware_url(url):
    """Check if URL looks like a firmware download"""
    firmware_patterns = [
        r'\.bin$', r'\.hex$', r'\.img$', r'\.fw$',
        r'/firmware/', r'/ota/', r'firmware',
        r'\.amazonaws\.com.*\.(bin|hex|img)',
    ]
    return any(re.search(pattern, url, re.IGNORECASE) for pattern in firmware_patterns)

def is_graphql_request(url, data):
    """Check if request is a GraphQL query"""
    if '/graphql' in url.lower():
        return True
    if data and isinstance(data, dict):
        return 'query' in data or 'mutation' in data
    return False

def save_firmware(url, content):
    """Save firmware file"""
    try:
        filename = os.path.basename(url.split('?')[0])
        if not filename:
            filename = f"firmware_{datetime.now().strftime('%Y%m%d_%H%M%S')}.bin"

        filepath = os.path.join(FIRMWARE_DIR, filename)
        with open(filepath, 'wb') as f:
            f.write(content)

        logger.info(f"💾 Firmware saved: {filepath} ({len(content)} bytes)")
        return filepath
    except Exception as e:
        logger.error(f"Failed to save firmware: {e}")
        return None

@app.route('/')
def dashboard():
    """Main dashboard"""
    return render_template_string(DASHBOARD_HTML)

@app.route('/api/status')
def api_status():
    """API endpoint for dashboard data"""
    with data_lock:
        return jsonify({
            'total_requests': len(captured_requests),
            'firmware_urls': firmware_urls[-20:],  # Last 20
            'graphql_queries': graphql_queries[-20:],  # Last 20
            'recent_requests': captured_requests[-50:]  # Last 50
        })

@app.route('/proxy', methods=['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'])
def proxy():
    """Main proxy endpoint"""
    try:
        # Get target URL from query parameter
        target_url = flask_request.args.get('url')
        if not target_url:
            return jsonify({'error': 'Missing url parameter'}), 400

        # Prepare request
        method = flask_request.method
        headers = dict(flask_request.headers)
        headers.pop('Host', None)  # Remove Host header

        data = None
        if flask_request.data:
            try:
                data = json.loads(flask_request.data)
            except:
                data = flask_request.data

        # Log request
        timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        request_info = {
            'method': method,
            'url': target_url,
            'timestamp': timestamp
        }

        with data_lock:
            captured_requests.append(request_info)

        logger.info(f"📡 {method} {target_url}")

        # Check if GraphQL
        if is_graphql_request(target_url, data):
            logger.info(f"📊 GraphQL query detected!")
            with data_lock:
                graphql_queries.append({
                    'query': data,
                    'timestamp': timestamp
                })

        # Forward request
        response = requests.request(
            method=method,
            url=target_url,
            headers=headers,
            data=flask_request.data if flask_request.data else None,
            allow_redirects=False,
            timeout=30
        )

        # Check response for firmware URLs
        try:
            response_json = response.json()
            response_text = json.dumps(response_json)

            # Look for URLs in response
            url_pattern = r'https?://[^\s"\'>]+'
            found_urls = re.findall(url_pattern, response_text)

            for url in found_urls:
                if is_firmware_url(url):
                    logger.info(f"🎯 FIRMWARE URL FOUND: {url}")
                    with data_lock:
                        if not any(item['url'] == url for item in firmware_urls):
                            firmware_urls.append({
                                'url': url,
                                'timestamp': timestamp
                            })

                    # Try to download firmware automatically
                    try:
                        logger.info(f"⬇️  Downloading firmware...")
                        fw_response = requests.get(url, timeout=60)
                        if fw_response.status_code == 200:
                            save_firmware(url, fw_response.content)
                    except Exception as e:
                        logger.error(f"Failed to download firmware: {e}")
        except:
            pass

        # Check if response itself is firmware
        if is_firmware_url(target_url) and response.status_code == 200:
            logger.info(f"💾 Downloading firmware from direct request...")
            save_firmware(target_url, response.content)

        # Return response
        return response.content, response.status_code, dict(response.headers)

    except Exception as e:
        logger.error(f"Proxy error: {e}")
        return jsonify({'error': str(e)}), 500

def print_instructions():
    """Print setup instructions"""
    print("\n" + "="*60)
    print("  🚀 AUGMENT FIRMWARE FLASK PROXY")
    print("="*60)
    print("\n📊 Dashboard: http://127.0.0.1:5000")
    print("\n📱 ANDROID SETUP:")
    print("\n1. Sæt proxy i WiFi indstillinger:")
    print("   - Proxy: Manuel")
    print("   - Hostname: <DIN_COMPUTER_IP>")
    print("   - Port: 8888")
    print("\n2. For HTTPS skal du installere certifikat")
    print("   (brug mitmproxy for dette)")
    print("\n3. Åbn Augment app og trigger firmware update")
    print("\n4. Se captured data i dashboard!")
    print("\n" + "="*60)
    print("\n⚠️  Lad dette vindue være åbent!\n")

if __name__ == '__main__':
    print_instructions()
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)
