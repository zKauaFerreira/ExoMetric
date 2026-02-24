# ExoMetric

**ExoMetric** is a high-performance Fabric mod for Minecraft 1.21.x designed for external telemetry. It exposes detailed server metrics (TPS, MSPT, Players) and Linux/Pterodactyl container data (CPU, RAM, Disk) via a secure internal HTTP API.

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

### GET `/mc-stats/players`
Returns a detailed list of all online players with coordinates and status.

### GET `/mc-stats/system`
Returns only hardware and container resource metrics.

**Required Parameter:** `?token=YOUR_TOKEN`

## 🛡️ Security

- The mod uses `SecureRandom` to generate 256-bit high-security tokens on the first boot.
- Recommended for integration with external Discord bots or status dashboards.
- Manual `/exometric reload` command is also available for administrators (OP 4).

## 📄 License

This project is licensed under CC0-1.0.
