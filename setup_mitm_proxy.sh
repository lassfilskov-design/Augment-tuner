#!/bin/bash

# Augment Firmware MITM Proxy Setup Script
# Double-click dette script for at installere mitmproxy

echo "============================================"
echo "  AUGMENT FIRMWARE MITM PROXY INSTALLER"
echo "============================================"
echo ""

# Check om Python er installeret
if ! command -v python3 &> /dev/null; then
    echo "❌ Python3 er ikke installeret!"
    echo "   Installér Python3 først: https://www.python.org/downloads/"
    read -p "Tryk Enter for at lukke..."
    exit 1
fi

echo "✅ Python3 fundet: $(python3 --version)"
echo ""

# Check om pip er installeret
if ! command -v pip3 &> /dev/null; then
    echo "❌ pip3 er ikke installeret!"
    echo "   Installér pip3 med: sudo apt install python3-pip"
    read -p "Tryk Enter for at lukke..."
    exit 1
fi

echo "✅ pip3 fundet"
echo ""

# Installér mitmproxy
echo "📦 Installerer mitmproxy..."
echo "   Dette kan tage et par minutter..."
echo ""

pip3 install mitmproxy --user

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ mitmproxy installeret succesfuldt!"
    echo ""
    echo "============================================"
    echo "  INSTALLATION FÆRDIG!"
    echo "============================================"
    echo ""
    echo "Næste skridt:"
    echo "1. Luk dette vindue"
    echo "2. Double-click på 'start_mitm_proxy.sh'"
    echo "3. Følg instruktionerne der vises"
    echo ""
else
    echo ""
    echo "❌ Installation fejlede!"
    echo "   Prøv at køre manuelt: pip3 install mitmproxy"
    echo ""
fi

read -p "Tryk Enter for at lukke..."
