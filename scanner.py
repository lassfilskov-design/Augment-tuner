#!/usr/bin/env python3
"""
Augment Scooter Security Scanner
Analyzes APK files and firmware for security issues, credentials, endpoints, etc.
"""

import os
import re
import sys
import json
import zipfile
import subprocess
from pathlib import Path
from typing import Dict, List, Set
from collections import defaultdict

class SecurityScanner:
    def __init__(self, output_dir="scan_results"):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(exist_ok=True)
        self.results = defaultdict(list)

        # Patterns for detection
        self.password_patterns = [
            r'password\s*=\s*["\']([^"\']+)["\']',
            r'pwd\s*=\s*["\']([^"\']+)["\']',
            r'passwd\s*=\s*["\']([^"\']+)["\']',
            r'secret\s*=\s*["\']([^"\']+)["\']',
            r'api[_-]?key\s*=\s*["\']([^"\']+)["\']',
            r'apikey\s*=\s*["\']([^"\']+)["\']',
            r'token\s*=\s*["\']([^"\']+)["\']',
            r'auth[_-]?token\s*=\s*["\']([^"\']+)["\']',
            r'private[_-]?key\s*=\s*["\']([^"\']+)["\']',
            r'client[_-]?secret\s*=\s*["\']([^"\']+)["\']',
            r'access[_-]?token\s*=\s*["\']([^"\']+)["\']',
            r'encryption[_-]?key\s*=\s*["\']([^"\']+)["\']',
            r'aws[_-]?secret\s*=\s*["\']([^"\']+)["\']',
            r'db[_-]?password\s*=\s*["\']([^"\']+)["\']',
        ]

        self.url_patterns = [
            r'https?://[^\s\'"<>]+',
            r'wss?://[^\s\'"<>]+',
            r'mqtt://[^\s\'"<>]+',
            r'ftp://[^\s\'"<>]+',
        ]

        self.backend_patterns = [
            r'/api/[^\s\'"<>]+',
            r'/v\d+/[^\s\'"<>]+',
            r'endpoint\s*=\s*["\']([^"\']+)["\']',
            r'baseUrl\s*=\s*["\']([^"\']+)["\']',
            r'BASE_URL\s*=\s*["\']([^"\']+)["\']',
            r'API_URL\s*=\s*["\']([^"\']+)["\']',
            r'SERVER_URL\s*=\s*["\']([^"\']+)["\']',
        ]

        self.sensitive_files = [
            r'\.key$', r'\.pem$', r'\.p12$', r'\.jks$',
            r'\.keystore$', r'\.db$', r'\.sqlite$',
            r'config\.json$', r'secrets\.json$',
            r'\.env$', r'credentials', r'private',
        ]

        self.red_flags = [
            r'TODO.*security',
            r'FIXME.*security',
            r'XXX.*security',
            r'hardcoded',
            r'bypass',
            r'debug.*true',
            r'ssl.*verify.*false',
            r'verify.*false',
            r'allowBackup.*true',
            r'debuggable.*true',
            r'usesCleartextTraffic.*true',
            r'ACCEPT_ALL',
            r'TrustAllCerts',
            r'HostnameVerifier',
        ]

    def scan_apk(self, apk_path: str) -> Dict:
        """Main entry point for scanning an APK file"""
        print(f"[*] Scanning APK: {apk_path}")
        apk_path = Path(apk_path)

        if not apk_path.exists():
            return {"error": "APK file not found"}

        # Extract APK
        extract_dir = self.output_dir / f"{apk_path.stem}_extracted"
        self._extract_apk(apk_path, extract_dir)

        # Scan extracted contents
        self._scan_directory(extract_dir)

        # Generate report
        report = self._generate_report(apk_path.name)

        return report

    def _extract_apk(self, apk_path: Path, extract_dir: Path):
        """Extract APK file (it's just a ZIP)"""
        print(f"[*] Extracting APK to {extract_dir}")
        extract_dir.mkdir(exist_ok=True)

        try:
            with zipfile.ZipFile(apk_path, 'r') as zip_ref:
                zip_ref.extractall(extract_dir)
            print(f"[+] APK extracted successfully")
        except Exception as e:
            print(f"[-] Error extracting APK: {e}")

    def _scan_directory(self, directory: Path):
        """Recursively scan directory for security issues"""
        print(f"[*] Scanning directory: {directory}")

        for item in directory.rglob('*'):
            if item.is_file():
                # Check filename
                self._check_filename(item)

                # Scan file contents if text-based
                if self._is_text_file(item):
                    self._scan_file(item)

    def _check_filename(self, file_path: Path):
        """Check if filename matches sensitive patterns"""
        filename = file_path.name

        for pattern in self.sensitive_files:
            if re.search(pattern, filename, re.IGNORECASE):
                self.results['sensitive_files'].append({
                    'file': str(file_path),
                    'reason': f'Matches pattern: {pattern}'
                })

    def _is_text_file(self, file_path: Path) -> bool:
        """Check if file is likely text-based"""
        text_extensions = {
            '.xml', '.json', '.js', '.java', '.smali', '.txt',
            '.html', '.htm', '.css', '.properties', '.yml', '.yaml',
            '.conf', '.config', '.sh', '.bat', '.md', '.kt'
        }

        return file_path.suffix.lower() in text_extensions or file_path.suffix == ''

    def _scan_file(self, file_path: Path):
        """Scan individual file for security issues"""
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()

                # Scan for passwords/credentials
                self._scan_passwords(file_path, content)

                # Scan for URLs
                self._scan_urls(file_path, content)

                # Scan for backend endpoints
                self._scan_backends(file_path, content)

                # Scan for red flags
                self._scan_red_flags(file_path, content)

        except Exception as e:
            pass  # Skip binary or problematic files

    def _scan_passwords(self, file_path: Path, content: str):
        """Scan for hardcoded passwords and credentials"""
        for pattern in self.password_patterns:
            matches = re.finditer(pattern, content, re.IGNORECASE)
            for match in matches:
                self.results['passwords'].append({
                    'file': str(file_path),
                    'match': match.group(0),
                    'value': match.group(1) if match.groups() else match.group(0),
                    'line': content[:match.start()].count('\n') + 1
                })

    def _scan_urls(self, file_path: Path, content: str):
        """Scan for URLs"""
        for pattern in self.url_patterns:
            matches = re.finditer(pattern, content, re.IGNORECASE)
            for match in matches:
                url = match.group(0)
                self.results['urls'].append({
                    'file': str(file_path),
                    'url': url,
                    'line': content[:match.start()].count('\n') + 1
                })

    def _scan_backends(self, file_path: Path, content: str):
        """Scan for backend endpoints and API paths"""
        for pattern in self.backend_patterns:
            matches = re.finditer(pattern, content, re.IGNORECASE)
            for match in matches:
                self.results['backends'].append({
                    'file': str(file_path),
                    'endpoint': match.group(0),
                    'line': content[:match.start()].count('\n') + 1
                })

    def _scan_red_flags(self, file_path: Path, content: str):
        """Scan for security red flags"""
        for pattern in self.red_flags:
            matches = re.finditer(pattern, content, re.IGNORECASE)
            for match in matches:
                self.results['red_flags'].append({
                    'file': str(file_path),
                    'issue': match.group(0),
                    'pattern': pattern,
                    'line': content[:match.start()].count('\n') + 1
                })

    def _generate_report(self, apk_name: str) -> Dict:
        """Generate final report"""
        report = {
            'apk': apk_name,
            'summary': {
                'passwords_found': len(self.results['passwords']),
                'urls_found': len(self.results['urls']),
                'backends_found': len(self.results['backends']),
                'sensitive_files_found': len(self.results['sensitive_files']),
                'red_flags_found': len(self.results['red_flags']),
            },
            'findings': dict(self.results)
        }

        # Save JSON report
        report_file = self.output_dir / f"{Path(apk_name).stem}_report.json"
        with open(report_file, 'w') as f:
            json.dump(report, f, indent=2)
        print(f"[+] Report saved to: {report_file}")

        # Print summary
        self._print_summary(report)

        return report

    def _print_summary(self, report: Dict):
        """Print summary to console"""
        print("\n" + "="*80)
        print(f"SECURITY SCAN RESULTS: {report['apk']}")
        print("="*80)

        summary = report['summary']
        print(f"\n[!] Passwords/Credentials: {summary['passwords_found']}")
        print(f"[!] URLs Found: {summary['urls_found']}")
        print(f"[!] Backend Endpoints: {summary['backends_found']}")
        print(f"[!] Sensitive Files: {summary['sensitive_files_found']}")
        print(f"[!] Red Flags: {summary['red_flags_found']}")

        # Show some examples
        if report['findings']['passwords']:
            print(f"\n[*] Sample Passwords Found:")
            for item in report['findings']['passwords'][:5]:
                print(f"    - {item['file']}:{item['line']} -> {item['match']}")

        if report['findings']['urls']:
            print(f"\n[*] Sample URLs Found:")
            unique_urls = list(set([item['url'] for item in report['findings']['urls']]))
            for url in unique_urls[:10]:
                print(f"    - {url}")

        if report['findings']['backends']:
            print(f"\n[*] Sample Backend Endpoints:")
            for item in report['findings']['backends'][:5]:
                print(f"    - {item['endpoint']}")

        if report['findings']['red_flags']:
            print(f"\n[*] Security Red Flags:")
            for item in report['findings']['red_flags'][:10]:
                print(f"    - {item['file']}:{item['line']} -> {item['issue']}")

        print("\n" + "="*80)


def main():
    if len(sys.argv) < 2:
        print("Usage: python scanner.py <apk_file>")
        print("Example: python scanner.py augment_scooter.apk")
        sys.exit(1)

    apk_file = sys.argv[1]
    scanner = SecurityScanner()
    scanner.scan_apk(apk_file)


if __name__ == "__main__":
    main()
