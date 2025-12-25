#!/usr/bin/env python3
"""
Web Interface for APK Security Scanner
Upload and analyze APK files through a web browser
"""

from flask import Flask, render_template, request, jsonify, send_file
from werkzeug.utils import secure_filename
import os
import json
from pathlib import Path
from scanner import SecurityScanner
from firmware_tools import FirmwareExtractor, APKDecompiler

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['MAX_CONTENT_LENGTH'] = 500 * 1024 * 1024  # 500MB max file size
app.config['ALLOWED_EXTENSIONS'] = {'apk', 'bin', 'img', 'zip', 'jar'}

# Create upload directory
Path(app.config['UPLOAD_FOLDER']).mkdir(exist_ok=True)

def allowed_file(filename):
    """Check if file extension is allowed"""
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in app.config['ALLOWED_EXTENSIONS']

@app.route('/')
def index():
    """Main page"""
    return render_template('index.html')

@app.route('/upload', methods=['POST'])
def upload_file():
    """Handle file upload and scanning"""
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided'}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400

    if not allowed_file(file.filename):
        return jsonify({'error': 'Invalid file type. Allowed: APK, BIN, IMG, ZIP, JAR'}), 400

    # Save uploaded file
    filename = secure_filename(file.filename)
    filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(filepath)

    try:
        # Run security scan
        scanner = SecurityScanner(output_dir=f"scan_results/{Path(filename).stem}")
        report = scanner.scan_apk(filepath)

        # Also try to extract firmware
        extractor = FirmwareExtractor(output_dir=f"firmware_extracted/{Path(filename).stem}")
        firmware_info = extractor.extract_firmware(filepath)

        # Decompile if it's an APK
        decompile_info = None
        if filename.lower().endswith('.apk'):
            decompiler = APKDecompiler(output_dir=f"decompiled/{Path(filename).stem}")
            decompile_info = decompiler.decompile_apk(filepath)

        # Combine results
        full_report = {
            'security_scan': report,
            'firmware_info': firmware_info,
            'decompile_info': decompile_info
        }

        return jsonify({
            'success': True,
            'filename': filename,
            'report': full_report
        })

    except Exception as e:
        return jsonify({
            'error': f'Scan failed: {str(e)}'
        }), 500

@app.route('/reports')
def list_reports():
    """List all scan reports"""
    reports_dir = Path('scan_results')
    reports = []

    if reports_dir.exists():
        for report_file in reports_dir.rglob('*_report.json'):
            try:
                with open(report_file, 'r') as f:
                    data = json.load(f)
                    reports.append({
                        'filename': report_file.name,
                        'apk': data.get('apk'),
                        'summary': data.get('summary')
                    })
            except:
                pass

    return jsonify({'reports': reports})

@app.route('/report/<path:filename>')
def get_report(filename):
    """Get specific report"""
    report_path = Path('scan_results') / filename

    if not report_path.exists():
        return jsonify({'error': 'Report not found'}), 404

    try:
        with open(report_path, 'r') as f:
            data = json.load(f)
        return jsonify(data)
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/download/<path:filename>')
def download_report(filename):
    """Download report as JSON file"""
    report_path = Path('scan_results') / filename

    if not report_path.exists():
        return jsonify({'error': 'Report not found'}), 404

    return send_file(report_path, as_attachment=True)


if __name__ == '__main__':
    print("[*] Starting Augment Security Scanner Web Interface")
    print("[*] Upload APK files at: http://localhost:5000")
    app.run(debug=True, host='0.0.0.0', port=5000)
