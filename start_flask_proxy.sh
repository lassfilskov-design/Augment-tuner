#!/bin/bash

# Augment Flask Proxy Starter
# Double-click dette script for at starte Flask proxy

clear
echo "============================================"
echo "  🚀 AUGMENT FLASK PROXY"
echo "============================================"
echo ""

# Check om Flask er installeret
python3 -c "import flask" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "❌ Flask er ikke installeret!"
    echo ""
    echo "Kør først 'setup_flask_proxy.sh' for at installere."
    echo ""
    read -p "Tryk Enter for at lukke..."
    exit 1
fi

# Find IP adresse
IP_ADDRESS=$(hostname -I | awk '{print $1}')

if [ -z "$IP_ADDRESS" ]; then
    IP_ADDRESS="DIN-COMPUTER-IP"
fi

echo "✅ Flask proxy klar!"
echo ""
echo "============================================"
echo "  📱 ANDROID TELEFON SETUP"
echo "============================================"
echo ""
echo "1️⃣  Åbn WiFi indstillinger på din telefon"
echo "    - Hold finger på dit WiFi netværk"
echo "    - Vælg 'Modificer netværk' eller 'Avanceret'"
echo ""
echo "2️⃣  Sæt Proxy til MANUEL:"
echo "    - Proxy hostname: $IP_ADDRESS"
echo "    - Proxy port: 8888"
echo "    - Gem ændringer"
echo ""
echo "3️⃣  Åbn Augment app og trigger firmware update"
echo ""
echo "============================================"
echo "  💻 COMPUTER DASHBOARD"
echo "============================================"
echo ""
echo "📊 Åbn dashboard i browser:"
echo "    http://127.0.0.1:5000"
echo ""
echo "Se live captured data:"
echo "  • Firmware URLs (auto-downloaded!)"
echo "  • GraphQL queries"
echo "  • Alle HTTP requests"
echo ""
echo "============================================"
echo ""
echo "🚀 Starter Flask proxy nu..."
echo ""
echo "⚠️  VIGTIGT: Lad dette vindue være åbent!"
echo "    Luk det FØRST når du er færdig."
echo ""
echo "============================================"
echo ""

# Start Flask proxy
python3 flask_proxy.py

echo ""
echo "Flask proxy stoppet."
read -p "Tryk Enter for at lukke..."
