#!/bin/bash

# Augment Firmware MITM Proxy Starter
# Double-click dette script for at starte mitmproxy

clear
echo "============================================"
echo "  AUGMENT FIRMWARE MITM PROXY"
echo "============================================"
echo ""

# Check om mitmproxy er installeret
if ! command -v mitmweb &> /dev/null && ! [ -f "$HOME/.local/bin/mitmweb" ]; then
    echo "❌ mitmproxy er ikke installeret!"
    echo ""
    echo "Kør først 'setup_mitm_proxy.sh' for at installere."
    echo ""
    read -p "Tryk Enter for at lukke..."
    exit 1
fi

# Find IP adresse
IP_ADDRESS=$(hostname -I | awk '{print $1}')

if [ -z "$IP_ADDRESS" ]; then
    IP_ADDRESS="DIN-COMPUTER-IP"
fi

echo "✅ mitmproxy klar!"
echo ""
echo "============================================"
echo "  INSTRUKTIONER TIL DIN ANDROID TELEFON"
echo "============================================"
echo ""
echo "1️⃣  Åbn WiFi indstillinger på din telefon"
echo "    - Hold finger på dit WiFi netværk"
echo "    - Vælg 'Modificer netværk' eller 'Avanceret'"
echo ""
echo "2️⃣  Sæt Proxy til MANUEL:"
echo "    - Proxy hostname: $IP_ADDRESS"
echo "    - Proxy port: 8080"
echo "    - Gem ændringer"
echo ""
echo "3️⃣  Åbn browser på telefonen og gå til:"
echo "    http://mitm.it"
echo ""
echo "4️⃣  Download og installér certifikat for Android"
echo ""
echo "5️⃣  Åbn Augment app og trigger firmware update"
echo ""
echo "6️⃣  Se captured traffic i browser på:"
echo "    http://127.0.0.1:8081"
echo ""
echo "============================================"
echo ""
echo "🚀 Starter mitmweb nu..."
echo ""
echo "⚠️  VIGTIGT: Lad dette vindue være åbent!"
echo "    Luk det FØRST når du er færdig."
echo ""
echo "============================================"
echo ""

# Start mitmweb
if command -v mitmweb &> /dev/null; then
    mitmweb --web-host 127.0.0.1 --web-port 8081
else
    $HOME/.local/bin/mitmweb --web-host 127.0.0.1 --web-port 8081
fi

echo ""
echo "mitmproxy stoppet."
read -p "Tryk Enter for at lukke..."
