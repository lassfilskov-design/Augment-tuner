#!/bin/bash
# Quick start script for Augment Security Scanner

echo "🛴 Augment Security Scanner - Quick Start"
echo "========================================="
echo ""

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed"
    exit 1
fi

echo "✅ Python 3 found"

# Install requirements
echo ""
echo "📦 Installing Python dependencies..."
pip install -r requirements.txt

# Create directories
echo ""
echo "📁 Creating working directories..."
mkdir -p uploads scan_results firmware_extracted decompiled

echo "✅ Directories created"

# Check for optional tools
echo ""
echo "🔍 Checking for optional tools..."

if command -v binwalk &> /dev/null; then
    echo "  ✅ binwalk found"
else
    echo "  ⚠️  binwalk not found (optional for firmware extraction)"
fi

if command -v apktool &> /dev/null; then
    echo "  ✅ apktool found"
else
    echo "  ⚠️  apktool not found (optional for APK decompilation)"
fi

if command -v jadx &> /dev/null; then
    echo "  ✅ jadx found"
else
    echo "  ⚠️  jadx not found (optional for Java decompilation)"
fi

echo ""
echo "========================================="
echo "✅ Setup complete!"
echo ""
echo "To start the web interface:"
echo "  python3 web_app.py"
echo ""
echo "To scan an APK from command line:"
echo "  python3 scanner.py your_app.apk"
echo ""
echo "For more info, see README.md"
echo "========================================="
