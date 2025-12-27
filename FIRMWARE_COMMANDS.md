# Augment Scooter - Firmware Control Commands

## ✅ VERIFIED WORKING - 00006881 Command Structure

**Service:** `00006681-0000-1000-8000-00805f9b34fb` (SECONDARY)
**Characteristic:** `00006881-0000-1000-8000-00805f9b34fb` (CONDITION_Notify)

⚠️ **SAFE characteristic** - This is the master firmware controller

---

## Speed Unlock Command

### Request Format:
```
Bytes: 68 83 66 83
│      └─┬─┘ └─┬─┘
│        │     └─ Service UUID last 4 hex (6683 = 00006683)
│        └─────── Characteristic UUID last 4 hex (6883 = 00006883)
```

### Response Format:
```
Bytes: 6c 02 46
│      │  │  └─ Value (0x46 = 70 decimal) → Result: 38 km/t
│      │  └──── Subcommand/Type (0x02)
│      └─────── Command Type (0x6c)
```

**Result:** Speed unlocked to 38 km/t

---

## Command Pattern Hypothesis

### Query Format (4 bytes):
```
[CHAR_UUID_LOW][CHAR_UUID_HIGH][SERVICE_UUID_LOW][SERVICE_UUID_HIGH]

Example:
68 83 66 83 = Query settings from Service 6683, Characteristic 6883
```

### Response Format (3 bytes):
```
[CMD_TYPE][SUBTYPE][VALUE]

Example:
6c 02 46 = Command type 6c, subtype 02, value 0x46
```

---

## Other Possible Query Targets

Based on UUID pattern, these queries might work:

```
68 80 66 80  → Query PRIMARY service (6680/6880)
68 82 66 82  → Query UNKNOWN service (6682/6882)
68 84 66 84  → Query STATUS service (6684/6884)
68 85 66 85  → Query FIRMWARE service (6685/6885)
68 87 66 87  → Query BATTERY service (6687/6887)
68 88 66 88  → Query GPS service (6688/6888)
```

⚠️ **WARNING:** Only test on 00006881 - other characteristics may brick scooter!

---

## Value Interpretation

### Speed Value Mystery:
```
Response: 0x46 = 70 decimal
Actual speed: 38 km/t

Possible formulas:
- 70 - 32 = 38 ✓ (Fahrenheit-like offset?)
- 70 * 0.54 ≈ 38 (conversion factor?)
- Other encoding?
```

**TODO:** Test other service/char combinations to understand value encoding

---

## Next Steps

1. ✅ Document working 68836683 command
2. ⏳ Test other UUID combinations (6880/6680, etc)
3. ⏳ Understand value encoding (46 → 38)
4. ⏳ Find write commands (not just query)
5. ⏳ Map all firmware control codes
