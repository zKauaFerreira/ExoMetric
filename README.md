# ExoMetric

**ExoMetric** is a high-performance Fabric mod for Minecraft 1.21.x designed for external telemetry. It exposes detailed server metrics (TPS, MSPT, Players) and Linux/Pterodactyl container data (CPU, RAM, Disk) via a secure internal HTTP API.

> [!TIP]
> **Tested on HidenCloud and it works perfectly!** 🚀

## 🚀 Quick Start

1. Place `exometric-1.0.0.jar` into your server's `mods/` folder.
2. Start the server once to generate the configuration file.
3. Edit `config/ExoMetric.json` and set your `api_port`.
4. **Save the file** and the mod will apply changes automatically within 5 seconds.
5. Access: `http://server-ip:port/mc-stats?token=YOUR_TOKEN`

## ✨ Features

- **Game Metrics**: TPS (Ticks Per Second), MSPT (Milli-seconds Per Tick), online players count, loaded chunks, seed, and weather.
- **System Metrics**: Real-time CPU usage (delta), RAM usage (cgroups/system), Disk usage, and Network traffic (RX/TX).
- **Player Data**: Detailed list including Name, UUID, Ping, Dimension, Gamemode, Health, Level, and Coordinates.
- **Security**: Authentication via Query string token (`?token=...`).
- **Auto-Reload (Hot-Swap)**: Change the token or port in the JSON file and the system restarts the API automatically without rebooting Minecraft.

## ⚙️ Configuration

The config file is located at `config/ExoMetric.json`:

| Field | Type | Description |
|-------|------|-----------|
| `api_enabled` | Boolean | Enables/Disables the metrics server. |
| `api_port` | Integer | HTTP Port (must be allocated/open on your host). |
| `api_token` | String | Automatically generated access token (can be customized). |

> **Note:** The mod monitors this file. Any saved changes will be applied in real-time.

## 🔗 API Reference

### GET `/mc-stats`
Returns the full server and system summary.

**Example Response:**
```json
{
  "status": "running",
  "memory_bytes": 754241536,
  "cpu_percent": 0.31,
  "disk_bytes": 344161165312,
  "network_rx_bytes": 410768,
  "network_tx_bytes": 7628,
  "uptime_seconds": 66133,
  "players_online": 0,
  "tps": 20.00,
  "mspt": 50.00,
  "current_tick_time": 50.00,
  "loaded_chunks": 0,
  "world_seed": -6461033676995397900,
  "world_time": 12016878,
  "world_day": 500,
  "is_raining": false,
  "difficulty": "normal",
  "heap_used_bytes": 271404632,
  "heap_max_bytes": 369098752
}
```

### GET `/mc-stats/players`
Returns a detailed list of all online players with coordinates and status.

**Example Response:**
```json
{
  "players_online": 1,
  "players": [
    {
      "name": "kauafpss_",
      "uuid": "e618e273-d894-3646-ade8-7a13ef58d6c6",
      "ping": 0,
      "dimension": "minecraft:overworld",
      "gamemode": "SURVIVAL",
      "level": 32,
      "health": 20,
      "food": 20,
      "saturation": 11,
      "x": 556.9,
      "y": 67,
      "z": 75.9,
      "online_seconds": 0,
      "avatar_url": "https://mc-heads.net/avatar/e618e273-d894-3646-ade8-7a13ef58d6c6/64",
      "main_hand": null,
      "off_hand": null,
      "armor": [
        {
          "id": "minecraft:diamond_boots",
          "count": 1,
          "slot": 36,
          "name": "Diamond Boots"
        },
        {
          "id": "minecraft:diamond_leggings",
          "count": 1,
          "slot": 37,
          "name": "Diamond Leggings"
        },
        {
          "id": "minecraft:diamond_chestplate",
          "count": 1,
          "slot": 38,
          "name": "Diamond Chestplate"
        }
      ],
      "hotbar": [
        {
          "id": "minecraft:diamond_axe",
          "count": 1,
          "slot": 0,
          "name": "Diamond Axe"
        },
        {
          "id": "minecraft:diamond_pickaxe",
          "count": 1,
          "slot": 1,
          "name": "Diamond Pickaxe"
        },
        {
          "id": "minecraft:diamond_shovel",
          "count": 1,
          "slot": 2,
          "name": "Diamond Shovel"
        }
      ],
      "main_inventory": [
        {
          "id": "minecraft:diamond_pickaxe",
          "count": 1,
          "slot": 9,
          "name": "Diamond Pickaxe"
        }
      ]
    }
  ]
}
```

### GET `/mc-stats/system`
Returns only hardware and container resource metrics.

**Example Response:**
```json
{
  "memory_bytes": 1357377536,
  "cpu_percent": 76.04,
  "disk_bytes": 344162578432,
  "network_rx_bytes": 242614,
  "network_tx_bytes": 3891089,
  "uptime_seconds": 66747
}
```

**Required Parameter:** `?token=YOUR_TOKEN`

## 🛡️ Security

- The mod uses `SecureRandom` to generate 256-bit high-security tokens on the first boot.
- Recommended for integration with external Discord bots or status dashboards.

## 📄 License

This project is licensed under CC0-1.0.
