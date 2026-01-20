#!/usr/bin/env python3
"""
Analyzer til at scanne APK billeder for skjult firmware data
Fokuserer på ES210 (ældre model) og ALT (Alturo) billeder
"""

import os
import struct
import binascii
from pathlib import Path

def read_png_chunks(filepath):
    """Læs alle PNG chunks og find usædvanlige data"""
    print(f"\n{'='*60}")
    print(f"Analyserer: {filepath}")
    print(f"{'='*60}")

    file_size = os.path.getsize(filepath)
    print(f"Filstørrelse: {file_size:,} bytes ({file_size/1024:.2f} KB)")

    with open(filepath, 'rb') as f:
        # Check PNG signature
        signature = f.read(8)
        if signature != b'\x89PNG\r\n\x1a\n':
            print("⚠️  IKKE en valid PNG fil!")
            return

        print("✓ Valid PNG signatur")

        chunks = []
        suspicious_data = []

        while True:
            # Læs chunk length
            length_bytes = f.read(4)
            if len(length_bytes) < 4:
                break

            length = struct.unpack('>I', length_bytes)[0]

            # Læs chunk type
            chunk_type = f.read(4)
            if len(chunk_type) < 4:
                break

            chunk_name = chunk_type.decode('ascii', errors='ignore')

            # Læs chunk data
            chunk_data = f.read(length)

            # Læs CRC
            crc = f.read(4)

            chunks.append({
                'type': chunk_name,
                'length': length,
                'data': chunk_data
            })

            # Tjek for usædvanlige chunks
            standard_chunks = ['IHDR', 'PLTE', 'IDAT', 'IEND', 'tRNS', 'gAMA',
                             'cHRM', 'sRGB', 'iCCP', 'tEXt', 'zTXt', 'iTXt',
                             'bKGD', 'pHYs', 'sBIT', 'sPLT', 'hIST', 'tIME']

            if chunk_name not in standard_chunks:
                print(f"⚠️  USÆDVANLIG CHUNK: '{chunk_name}' (størrelse: {length} bytes)")
                suspicious_data.append({
                    'type': 'unusual_chunk',
                    'name': chunk_name,
                    'size': length,
                    'data': chunk_data
                })

            if chunk_name == 'IEND':
                # Tjek om der er data EFTER IEND (meget suspekt!)
                remaining_pos = f.tell()
                f.seek(0, 2)  # Gå til slutningen
                file_end = f.tell()
                extra_bytes = file_end - remaining_pos

                if extra_bytes > 0:
                    print(f"\n🚨 KRITISK FUND: {extra_bytes} bytes data EFTER IEND chunk!")
                    f.seek(remaining_pos)
                    extra_data = f.read(extra_bytes)

                    suspicious_data.append({
                        'type': 'post_iend_data',
                        'size': extra_bytes,
                        'data': extra_data
                    })

                    # Tjek om det ligner firmware
                    print(f"Første 100 bytes af ekstra data:")
                    print(binascii.hexlify(extra_data[:100]).decode())

                    # Tjek for common firmware signatures
                    if b'\x7fELF' in extra_data[:4]:
                        print("🎯 MULIGT ELF BINARY (Linux executable)!")
                    if extra_data[:2] == b'MZ':
                        print("🎯 MULIGT PE BINARY (Windows executable)!")
                    if b'FIRMWARE' in extra_data.upper():
                        print("🎯 Indeholder teksten 'FIRMWARE'!")
                break

        print(f"\nTotal antal chunks: {len(chunks)}")
        print("\nChunk oversigt:")
        for chunk in chunks:
            print(f"  - {chunk['type']:6s}: {chunk['length']:8,} bytes")

        return suspicious_data

def analyze_png_dimensions_vs_size(filepath):
    """Sammenlign PNG dimensioner med filstørrelse for at finde anomalier"""
    with open(filepath, 'rb') as f:
        f.read(8)  # Skip signature
        f.read(4)  # Skip IHDR length
        f.read(4)  # Skip IHDR type

        width = struct.unpack('>I', f.read(4))[0]
        height = struct.unpack('>I', f.read(4))[0]
        bit_depth = struct.unpack('>B', f.read(1))[0]
        color_type = struct.unpack('>B', f.read(1))[0]

        print(f"\nPNG Dimensioner: {width}x{height}")
        print(f"Bit depth: {bit_depth}, Color type: {color_type}")

        # Beregn forventet størrelse (rough estimate)
        pixels = width * height
        channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}.get(color_type, 4)
        expected_raw = pixels * channels * (bit_depth // 8)

        actual_size = os.path.getsize(filepath)

        print(f"Forventet rå data: ~{expected_raw:,} bytes")
        print(f"Faktisk filstørrelse: {actual_size:,} bytes")

        ratio = actual_size / expected_raw if expected_raw > 0 else 0
        print(f"Komprimerings ratio: {ratio:.2f}x")

        if ratio > 1.5:
            print("⚠️  Filen er usædvanligt stor for sine dimensioner!")
            return True

        return False

def search_for_patterns(filepath):
    """Søg efter firmware-relaterede patterns i billedet"""
    print("\nSøger efter firmware patterns...")

    with open(filepath, 'rb') as f:
        data = f.read()

    patterns = {
        b'FIRMWARE': 'FIRMWARE tekst',
        b'UPDATE': 'UPDATE tekst',
        b'VERSION': 'VERSION tekst',
        b'.bin': '.bin fil extension',
        b'.hex': '.hex fil extension',
        b'.img': '.img fil extension',
        b'ES210': 'ES210 reference',
        b'ALT': 'ALT/Alturo reference',
        b'AUGMENT': 'AUGMENT reference',
    }

    findings = []
    for pattern, description in patterns.items():
        count = data.count(pattern)
        if count > 0:
            print(f"  ✓ Fandt '{pattern.decode(errors='ignore')}' {count} gange - {description}")
            findings.append((pattern, count))

    return findings

def main():
    print("="*60)
    print("AUGMENT APK BILLEDE ANALYZER")
    print("Søger efter skjult firmware data i PNG filer")
    print("="*60)

    # Find alle ES210 og ALT billeder
    base_path = Path("/home/user/Augment-tuner/apk_extracted/res")

    target_files = []
    for pattern in ["*es210*.png", "*alt*.png"]:
        target_files.extend(base_path.rglob(pattern))

    if not target_files:
        print("Ingen billeder fundet!")
        return

    print(f"\nFandt {len(target_files)} relevante billeder\n")

    all_suspicious = []

    for filepath in sorted(target_files, key=lambda x: os.path.getsize(x), reverse=True):
        suspicious = read_png_chunks(str(filepath))
        size_anomaly = analyze_png_dimensions_vs_size(str(filepath))
        patterns = search_for_patterns(str(filepath))

        if suspicious or size_anomaly or patterns:
            all_suspicious.append({
                'file': filepath,
                'size': os.path.getsize(filepath),
                'suspicious_chunks': suspicious,
                'size_anomaly': size_anomaly,
                'patterns': patterns
            })

    # Opsummering
    print("\n" + "="*60)
    print("OPSUMMERING AF FUND")
    print("="*60)

    if all_suspicious:
        print(f"\n🔍 Fandt {len(all_suspicious)} filer med potentielt skjult data:\n")
        for item in all_suspicious:
            print(f"📁 {item['file'].name}")
            print(f"   Størrelse: {item['size']:,} bytes ({item['size']/1024:.2f} KB)")
            if item['suspicious_chunks']:
                print(f"   ⚠️  {len(item['suspicious_chunks'])} mistænkelige chunks/data")
            if item['size_anomaly']:
                print(f"   ⚠️  Usædvanlig størrelse")
            if item['patterns']:
                print(f"   ⚠️  Firmware patterns fundet")
            print()
    else:
        print("\nIngen åbenlyse tegn på skjult firmware data i billederne.")
        print("Dette betyder IKKE at firmware ikke er gemt på anden måde.")

    # Næste skridt
    print("\n" + "="*60)
    print("NÆSTE SKRIDT")
    print("="*60)
    print("""
1. Tjek assets/ mappen for .bin, .hex, .img eller andre firmware filer
2. Analysér JavaScript bundle for firmware download URLs
3. Undersøg om firmware downloades runtime via API
4. Tjek for Base64 encoded data i resources
    """)

if __name__ == "__main__":
    main()
