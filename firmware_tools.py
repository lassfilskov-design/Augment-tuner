#!/usr/bin/env python3
"""
Firmware Extraction and Analysis Tools
Supports multiple firmware formats and extraction methods
"""

import os
import subprocess
import struct
from pathlib import Path
from typing import List, Dict

class FirmwareExtractor:
    """Extract and analyze firmware files"""

    def __init__(self, output_dir="firmware_extracted"):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(exist_ok=True)

    def extract_firmware(self, firmware_path: str) -> Dict:
        """Main entry point for firmware extraction"""
        firmware_path = Path(firmware_path)
        print(f"[*] Analyzing firmware: {firmware_path}")

        results = {
            'file': str(firmware_path),
            'size': firmware_path.stat().st_size,
            'methods': []
        }

        # Try different extraction methods
        if self._is_zip_based(firmware_path):
            results['methods'].append(self._extract_zip(firmware_path))

        if self._has_binwalk():
            results['methods'].append(self._extract_with_binwalk(firmware_path))

        # Check for common firmware formats
        results['file_type'] = self._identify_firmware_type(firmware_path)

        return results

    def _is_zip_based(self, file_path: Path) -> bool:
        """Check if file is ZIP-based (APK, JAR, etc.)"""
        try:
            with open(file_path, 'rb') as f:
                magic = f.read(4)
                return magic == b'PK\x03\x04'
        except:
            return False

    def _extract_zip(self, file_path: Path) -> Dict:
        """Extract ZIP-based firmware"""
        import zipfile

        extract_dir = self.output_dir / f"{file_path.stem}_zip"
        extract_dir.mkdir(exist_ok=True)

        try:
            with zipfile.ZipFile(file_path, 'r') as zip_ref:
                zip_ref.extractall(extract_dir)
            return {
                'method': 'zip',
                'status': 'success',
                'output': str(extract_dir)
            }
        except Exception as e:
            return {
                'method': 'zip',
                'status': 'failed',
                'error': str(e)
            }

    def _has_binwalk(self) -> bool:
        """Check if binwalk is available"""
        try:
            subprocess.run(['binwalk', '--help'],
                         capture_output=True,
                         timeout=5)
            return True
        except:
            return False

    def _extract_with_binwalk(self, file_path: Path) -> Dict:
        """Extract firmware using binwalk"""
        extract_dir = self.output_dir / f"{file_path.stem}_binwalk"
        extract_dir.mkdir(exist_ok=True)

        try:
            # Run binwalk extraction
            result = subprocess.run(
                ['binwalk', '-e', '-C', str(extract_dir), str(file_path)],
                capture_output=True,
                text=True,
                timeout=60
            )

            return {
                'method': 'binwalk',
                'status': 'success' if result.returncode == 0 else 'failed',
                'output': str(extract_dir),
                'stdout': result.stdout
            }
        except Exception as e:
            return {
                'method': 'binwalk',
                'status': 'failed',
                'error': str(e)
            }

    def _identify_firmware_type(self, file_path: Path) -> str:
        """Identify firmware type by magic bytes"""
        try:
            with open(file_path, 'rb') as f:
                header = f.read(512)

                # Check various firmware signatures
                if header.startswith(b'PK\x03\x04'):
                    return 'ZIP/APK/JAR'
                elif header.startswith(b'\x7fELF'):
                    return 'ELF Binary'
                elif header.startswith(b'dex\n'):
                    return 'Android DEX'
                elif header.startswith(b'ANDROID!'):
                    return 'Android Boot Image'
                elif b'uImage' in header[:64]:
                    return 'U-Boot Image'
                elif header.startswith(b'\x1f\x8b'):
                    return 'GZIP Compressed'
                elif header.startswith(b'BZh'):
                    return 'BZIP2 Compressed'
                elif header.startswith(b'\xfd7zXZ'):
                    return 'XZ Compressed'
                elif header.startswith(b'LZMA'):
                    return 'LZMA Compressed'
                else:
                    return 'Unknown'
        except:
            return 'Unknown'

    def get_strings(self, file_path: str, min_length: int = 6) -> List[str]:
        """Extract printable strings from firmware"""
        try:
            result = subprocess.run(
                ['strings', '-n', str(min_length), file_path],
                capture_output=True,
                text=True,
                timeout=30
            )
            return result.stdout.splitlines()
        except:
            # Fallback to manual string extraction
            strings = []
            try:
                with open(file_path, 'rb') as f:
                    data = f.read()
                    current = []

                    for byte in data:
                        if 32 <= byte <= 126:  # Printable ASCII
                            current.append(chr(byte))
                        else:
                            if len(current) >= min_length:
                                strings.append(''.join(current))
                            current = []

                    if len(current) >= min_length:
                        strings.append(''.join(current))
            except:
                pass

            return strings


class APKDecompiler:
    """Advanced APK decompilation using multiple tools"""

    def __init__(self, output_dir="decompiled"):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(exist_ok=True)

    def decompile_apk(self, apk_path: str) -> Dict:
        """Decompile APK using available tools"""
        apk_path = Path(apk_path)
        results = {
            'apk': str(apk_path),
            'methods': []
        }

        # Try apktool
        if self._has_apktool():
            results['methods'].append(self._decompile_with_apktool(apk_path))

        # Try jadx
        if self._has_jadx():
            results['methods'].append(self._decompile_with_jadx(apk_path))

        # Extract AndroidManifest.xml info
        results['manifest'] = self._extract_manifest_info(apk_path)

        return results

    def _has_apktool(self) -> bool:
        """Check if apktool is available"""
        try:
            subprocess.run(['apktool', '--version'],
                         capture_output=True,
                         timeout=5)
            return True
        except:
            return False

    def _has_jadx(self) -> bool:
        """Check if jadx is available"""
        try:
            subprocess.run(['jadx', '--version'],
                         capture_output=True,
                         timeout=5)
            return True
        except:
            return False

    def _decompile_with_apktool(self, apk_path: Path) -> Dict:
        """Decompile with apktool"""
        output_dir = self.output_dir / f"{apk_path.stem}_apktool"

        try:
            result = subprocess.run(
                ['apktool', 'd', str(apk_path), '-o', str(output_dir), '-f'],
                capture_output=True,
                text=True,
                timeout=120
            )

            return {
                'tool': 'apktool',
                'status': 'success' if result.returncode == 0 else 'failed',
                'output': str(output_dir)
            }
        except Exception as e:
            return {
                'tool': 'apktool',
                'status': 'failed',
                'error': str(e)
            }

    def _decompile_with_jadx(self, apk_path: Path) -> Dict:
        """Decompile with jadx"""
        output_dir = self.output_dir / f"{apk_path.stem}_jadx"

        try:
            result = subprocess.run(
                ['jadx', str(apk_path), '-d', str(output_dir)],
                capture_output=True,
                text=True,
                timeout=120
            )

            return {
                'tool': 'jadx',
                'status': 'success' if result.returncode == 0 else 'failed',
                'output': str(output_dir)
            }
        except Exception as e:
            return {
                'tool': 'jadx',
                'status': 'failed',
                'error': str(e)
            }

    def _extract_manifest_info(self, apk_path: Path) -> Dict:
        """Extract AndroidManifest.xml information"""
        import zipfile
        import xml.etree.ElementTree as ET

        try:
            with zipfile.ZipFile(apk_path, 'r') as zip_ref:
                # Try to read manifest (will be binary in APK)
                if 'AndroidManifest.xml' in zip_ref.namelist():
                    return {
                        'found': True,
                        'note': 'Manifest found but needs AAPT to decode'
                    }
        except:
            pass

        return {'found': False}


# Utility functions for firmware analysis
def find_interesting_strings(strings: List[str]) -> Dict[str, List[str]]:
    """Find interesting strings in firmware"""
    import re

    results = {
        'urls': [],
        'ips': [],
        'emails': [],
        'keys': [],
        'paths': [],
    }

    url_pattern = r'https?://[^\s]+'
    ip_pattern = r'\b(?:\d{1,3}\.){3}\d{1,3}\b'
    email_pattern = r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}'
    key_pattern = r'(?:api[_-]?key|password|secret|token)[\s:=]+[a-zA-Z0-9]+'

    for s in strings:
        if re.search(url_pattern, s, re.IGNORECASE):
            results['urls'].append(s)
        if re.search(ip_pattern, s):
            results['ips'].append(s)
        if re.search(email_pattern, s):
            results['emails'].append(s)
        if re.search(key_pattern, s, re.IGNORECASE):
            results['keys'].append(s)
        if '/' in s and len(s) > 10:
            results['paths'].append(s)

    return results


if __name__ == "__main__":
    import sys

    if len(sys.argv) < 2:
        print("Usage: python firmware_tools.py <firmware_file>")
        sys.exit(1)

    file_path = sys.argv[1]

    # Extract firmware
    extractor = FirmwareExtractor()
    results = extractor.extract_firmware(file_path)

    print(f"\n[*] Firmware Type: {results['file_type']}")
    print(f"[*] File Size: {results['size']} bytes")

    for method in results['methods']:
        print(f"\n[*] Extraction Method: {method.get('method', method.get('tool'))}")
        print(f"    Status: {method['status']}")
        if method['status'] == 'success':
            print(f"    Output: {method['output']}")

    # Extract strings
    print("\n[*] Extracting strings...")
    strings = extractor.get_strings(file_path)
    interesting = find_interesting_strings(strings)

    print(f"\n[+] Found {len(interesting['urls'])} URLs")
    print(f"[+] Found {len(interesting['ips'])} IP addresses")
    print(f"[+] Found {len(interesting['emails'])} email addresses")
    print(f"[+] Found {len(interesting['keys'])} potential keys/secrets")
