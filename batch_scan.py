#!/usr/bin/env python3
"""
Batch scanner for multiple APK files
Scans all APK files in a directory and generates a combined report
"""

import os
import sys
import json
from pathlib import Path
from scanner import SecurityScanner
from datetime import datetime

def scan_directory(directory: str, output_file: str = "batch_report.json"):
    """Scan all APK files in a directory"""
    apk_dir = Path(directory)

    if not apk_dir.exists():
        print(f"Error: Directory {directory} not found")
        return

    # Find all APK files
    apk_files = list(apk_dir.glob("*.apk"))

    if not apk_files:
        print(f"No APK files found in {directory}")
        return

    print(f"[*] Found {len(apk_files)} APK files to scan")

    results = {
        'scan_date': datetime.now().isoformat(),
        'directory': str(apk_dir),
        'total_apks': len(apk_files),
        'scans': []
    }

    scanner = SecurityScanner()

    # Scan each APK
    for i, apk_file in enumerate(apk_files, 1):
        print(f"\n[{i}/{len(apk_files)}] Scanning {apk_file.name}")

        try:
            report = scanner.scan_apk(str(apk_file))
            results['scans'].append({
                'apk': apk_file.name,
                'status': 'success',
                'report': report
            })
        except Exception as e:
            print(f"[-] Error scanning {apk_file.name}: {e}")
            results['scans'].append({
                'apk': apk_file.name,
                'status': 'failed',
                'error': str(e)
            })

        # Reset results for next scan
        scanner.results.clear()

    # Generate summary
    total_passwords = sum(s['report']['summary']['passwords_found']
                         for s in results['scans'] if s['status'] == 'success')
    total_urls = sum(s['report']['summary']['urls_found']
                    for s in results['scans'] if s['status'] == 'success')
    total_backends = sum(s['report']['summary']['backends_found']
                        for s in results['scans'] if s['status'] == 'success')

    results['summary'] = {
        'total_passwords': total_passwords,
        'total_urls': total_urls,
        'total_backends': total_backends,
        'successful_scans': sum(1 for s in results['scans'] if s['status'] == 'success'),
        'failed_scans': sum(1 for s in results['scans'] if s['status'] == 'failed')
    }

    # Save combined report
    with open(output_file, 'w') as f:
        json.dump(results, f, indent=2)

    print(f"\n[+] Batch scan complete!")
    print(f"[+] Report saved to: {output_file}")
    print(f"\n=== BATCH SUMMARY ===")
    print(f"Total APKs scanned: {results['total_apks']}")
    print(f"Successful: {results['summary']['successful_scans']}")
    print(f"Failed: {results['summary']['failed_scans']}")
    print(f"Total passwords found: {results['summary']['total_passwords']}")
    print(f"Total URLs found: {results['summary']['total_urls']}")
    print(f"Total backends found: {results['summary']['total_backends']}")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python batch_scan.py <directory> [output_file]")
        print("Example: python batch_scan.py ./apk_files batch_report.json")
        sys.exit(1)

    directory = sys.argv[1]
    output_file = sys.argv[2] if len(sys.argv) > 2 else "batch_report.json"

    scan_directory(directory, output_file)
