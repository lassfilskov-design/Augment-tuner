#!/usr/bin/env python3
"""
Augment Firmware OTA Interceptor
Automatically captures firmware download URLs from Augment app
"""

import json
import re
from mitmproxy import http
from pathlib import Path
import requests
from datetime import datetime

class AugmentFirmwareInterceptor:
    def __init__(self):
        self.output_dir = Path("firmware_captures")
        self.output_dir.mkdir(exist_ok=True)
        print("[*] Augment Firmware Interceptor Started")
        print("[*] Listening for GraphQL requests...")
        print("[*] Trigger firmware check in Augment app now!")

    def request(self, flow: http.HTTPFlow) -> None:
        """Intercept outgoing requests"""
        # Check if this is a GraphQL request
        if "graphql" in flow.request.pretty_url.lower() or \
           (flow.request.method == "POST" and flow.request.content):

            try:
                body = flow.request.text
                if body and "FirmwareUpgrade" in body:
                    print("\n" + "="*80)
                    print("[+] FOUND FIRMWARE CHECK REQUEST!")
                    print("="*80)

                    # Parse request
                    try:
                        data = json.loads(body)
                        print(f"\n[*] Query: {data.get('query', 'N/A')[:100]}...")
                        print(f"[*] Variables: {json.dumps(data.get('variables', {}), indent=2)}")

                        # Save request
                        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                        request_file = self.output_dir / f"firmware_request_{timestamp}.json"
                        with open(request_file, 'w') as f:
                            json.dump(data, f, indent=2)
                        print(f"[*] Request saved to: {request_file}")

                    except json.JSONDecodeError:
                        print(f"[*] Raw request: {body[:200]}...")

            except Exception as e:
                pass

    def response(self, flow: http.HTTPFlow) -> None:
        """Intercept incoming responses"""
        # Check if this is a GraphQL response
        if flow.request.method == "POST" and flow.response.content:
            try:
                body = flow.response.text
                if body and ("firmware" in body.lower() or "Firmware" in body):
                    print("\n" + "="*80)
                    print("[+] FOUND FIRMWARE RESPONSE!")
                    print("="*80)

                    try:
                        data = json.loads(body)

                        # Save response
                        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                        response_file = self.output_dir / f"firmware_response_{timestamp}.json"
                        with open(response_file, 'w') as f:
                            json.dump(data, f, indent=2)

                        print(f"\n[*] Response saved to: {response_file}")
                        print(f"\n[*] Full response:\n{json.dumps(data, indent=2)}")

                        # Extract firmware info
                        self._extract_firmware_info(data)

                    except json.JSONDecodeError:
                        # Try to find URLs in response
                        urls = re.findall(r'https?://[^\s"\'<>]+\.bin', body)
                        if urls:
                            print(f"\n[+] Found firmware URLs:")
                            for url in urls:
                                print(f"    - {url}")
                                self._download_firmware(url)

            except Exception as e:
                pass

    def _extract_firmware_info(self, data):
        """Extract firmware download info from GraphQL response"""
        print("\n" + "="*80)
        print("[+] EXTRACTING FIRMWARE INFO")
        print("="*80)

        # Try to find firmware info in response
        def find_firmware_data(obj, path=""):
            if isinstance(obj, dict):
                for key, value in obj.items():
                    new_path = f"{path}.{key}" if path else key

                    # Check if this looks like firmware info
                    if any(keyword in key.lower() for keyword in ['firmware', 'download', 'url', 'version', 'partition']):
                        print(f"\n[*] {new_path}: {value}")

                    # Check for download URL
                    if isinstance(value, str) and value.startswith('http') and '.bin' in value:
                        print(f"\n[+++] FIRMWARE DOWNLOAD URL FOUND!")
                        print(f"[+++] URL: {value}")
                        self._download_firmware(value)

                    find_firmware_data(value, new_path)

            elif isinstance(obj, list):
                for i, item in enumerate(obj):
                    find_firmware_data(item, f"{path}[{i}]")

        find_firmware_data(data)

    def _download_firmware(self, url):
        """Download firmware file"""
        print(f"\n[*] Downloading firmware from: {url}")

        try:
            filename = url.split('/')[-1]
            output_path = self.output_dir / filename

            print(f"[*] Saving to: {output_path}")

            response = requests.get(url, stream=True)
            response.raise_for_status()

            total_size = int(response.headers.get('content-length', 0))
            downloaded = 0

            with open(output_path, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)
                        downloaded += len(chunk)
                        if total_size > 0:
                            percent = (downloaded / total_size) * 100
                            print(f"\r[*] Progress: {percent:.1f}% ({downloaded}/{total_size} bytes)", end='')

            print(f"\n[+++] FIRMWARE DOWNLOADED SUCCESSFULLY!")
            print(f"[+++] File: {output_path}")
            print(f"[+++] Size: {downloaded} bytes")

            # Analyze firmware
            self._analyze_firmware(output_path)

        except Exception as e:
            print(f"\n[-] Download failed: {e}")

    def _analyze_firmware(self, firmware_path):
        """Quick analysis of firmware file"""
        print(f"\n" + "="*80)
        print("[*] QUICK FIRMWARE ANALYSIS")
        print("="*80)

        try:
            with open(firmware_path, 'rb') as f:
                header = f.read(512)

            # Check file type
            if header.startswith(b'\x7fELF'):
                print("[*] File type: ELF Binary")
            elif header.startswith(b'PK'):
                print("[*] File type: ZIP Archive")
            else:
                print(f"[*] File type: Unknown (first bytes: {header[:16].hex()})")

            # File size
            size = firmware_path.stat().st_size
            print(f"[*] File size: {size} bytes ({size/1024:.1f} KB)")

            # Save analysis
            analysis_file = firmware_path.with_suffix('.txt')
            with open(analysis_file, 'w') as f:
                f.write(f"Firmware Analysis\n")
                f.write(f"================\n\n")
                f.write(f"File: {firmware_path.name}\n")
                f.write(f"Size: {size} bytes\n")
                f.write(f"Downloaded: {datetime.now()}\n")
                f.write(f"\nFirst 512 bytes (hex):\n{header.hex()}\n")

            print(f"[*] Analysis saved to: {analysis_file}")

        except Exception as e:
            print(f"[-] Analysis failed: {e}")


# mitmproxy addon
addons = [AugmentFirmwareInterceptor()]
