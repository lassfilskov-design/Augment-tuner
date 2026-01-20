#!/bin/bash

# Augment Flask Proxy Setup Script
# Double-click dette script for at installere Flask proxy

echo "============================================"
echo "  AUGMENT FLASK PROXY INSTALLER"
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

# Installér dependencies
echo "📦 Installerer Flask og dependencies..."
echo "   Dette kan tage et par minutter..."
echo ""

pip3 install -r requirements.txt --user

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Flask proxy installeret succesfuldt!"
    echo ""
    echo "============================================"
    echo "  INSTALLATION FÆRDIG!"
    echo "============================================"
    echo ""
    echo "Næste skridt:"
    echo "1. Luk dette vindue"
    echo "2. Double-click på 'start_flask_proxy.sh'"
    echo "3. Følg instruktionerne der vises"
    echo ""
    echo "Features:"
    echo "  ✓ Automatisk firmware detection"
    echo "  ✓ Automatisk firmware download"
    echo "  ✓ GraphQL query logging"
    echo "  ✓ Live web dashboard"
    echo "  ✓ Alt gemmes til captured_data/"
    echo ""
else
    echo ""
    echo "❌ Installation fejlede!"
    echo "   Prøv at køre manuelt: pip3 install -r requirements.txt"
    echo ""
fi

read -p "Tryk Enter for at lukke..."
