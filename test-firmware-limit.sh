#!/bin/bash
# AUGMENT SCOOTER - FIRMWARE LIMIT TESTER
# Dette script tester systematisk hastighedsgrænser for at finde firmware loftet

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  AUGMENT FIRMWARE LIMIT TESTER"
echo "═══════════════════════════════════════════════════════"
echo ""
echo "⚠️  VIGTIGT:"
echo "    - Kør scooteren i kontrolleret miljø"
echo "    - Overvåg temperatur (max 70°C motor)"
echo "    - Brug hjelm og beskyttelse"
echo ""
echo "📊 Tester følgende hastigheder:"
echo "    38 km/h (nuværende limit)"
echo "    40 km/h"
echo "    45 km/h (Augment M+ standard)"
echo "    50 km/h"
echo "    55 km/h"
echo "    60 km/h"
echo "    NO LIMIT (255)"
echo ""
read -p "Tryk ENTER for at starte test..."

# Test array: [speed_kmh, hex_value, description]
declare -a tests=(
    "38:0x26:Current limit"
    "40:0x28:+2 km/h"
    "45:0x2D:Augment M+"
    "50:0x32:+12 km/h"
    "55:0x37:+17 km/h"
    "60:0x3C:+22 km/h"
    "255:0xFF:NO LIMIT"
)

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  STARTER SYSTEMATISK TEST"
echo "═══════════════════════════════════════════════════════"
echo ""

for test in "${tests[@]}"; do
    IFS=':' read -r speed hex desc <<< "$test"

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📍 TEST: $speed km/h ($desc)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "   Hex kommando: A2 ${hex#0x} 00"
    echo ""
    echo "❓ Hvad skete der?"
    echo "   [1] Ingen ændring (stadig ~38 km/h)"
    echo "   [2] Hastigheden steg til ~$speed km/h"
    echo "   [3] Motor blev ustabil/overophedet"
    echo "   [4] Andet (skriv noter)"
    echo ""

    read -p "   Vælg resultat (1-4): " result

    case $result in
        1)
            echo "   ❌ FIRMWARE LOCKED - Ingen ændring"
            echo "      → Firmware limit er under $speed km/h"
            ;;
        2)
            echo "   ✅ SUCCESS - Hastighed steg!"
            echo "      → Firmware tillader mindst $speed km/h"
            read -p "   Præcis speedometer aflæsning: " actual_speed
            echo "      → Speedometer viste: $actual_speed km/h"
            ;;
        3)
            echo "   ⚠️  HARDWARE LIMIT - Motor ustabil"
            echo "      → Dette er sandsynligvis hardware max"
            read -p "   Hvad skete der? " issue
            echo "      → Noter: $issue"
            break
            ;;
        4)
            read -p "   Beskriv hvad der skete: " notes
            echo "      → Noter: $notes"
            ;;
    esac

    # Temperature check
    echo ""
    read -p "   Motor temperatur (°C): " temp

    if [ "$temp" -gt 70 ]; then
        echo "   🔥 ADVARSEL: Temperatur over 70°C!"
        echo "   → Lad motoren køle ned før næste test"
        read -p "   Tryk ENTER når motoren er kølet ned..."
    fi

    echo ""
    read -p "Klar til næste test? (ENTER = ja, q = stop): " continue

    if [ "$continue" = "q" ]; then
        echo ""
        echo "Test afbrudt af bruger"
        break
    fi

    echo ""
done

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  TEST KOMPLET"
echo "═══════════════════════════════════════════════════════"
echo ""
echo "📊 Resultat oversigt:"
echo ""
echo "Baseret på dine svar kan du nu bestemme:"
echo "  - Hvad er firmware limit?"
echo "  - Hvad er hardware limit?"
echo "  - Er firmware modding nødvendig?"
echo ""
echo "💡 Næste skridt:"
echo ""
echo "Hvis ALLE tests gav 'Ingen ændring':"
echo "  → Firmware er låst på 38 km/h"
echo "  → Du skal modificere firmware (se FIRMWARE_MODDING_GUIDE.md)"
echo ""
echo "Hvis NOGLE tests virkede:"
echo "  → Firmware tillader højere hastigheder!"
echo "  → Brug den højeste stabile hastighed"
echo "  → Husk temperaturovervågning i produktion"
echo ""
echo "Hvis motor blev ustabil:"
echo "  → Du har fundet hardware limit"
echo "  → Reducer hastighed til sikker niveau"
echo ""
echo "═══════════════════════════════════════════════════════"
echo ""
