package com.exometric;

import java.util.ArrayList;
import java.util.List;

public class MetricsData {
    public Long memoryBytes;
    public Double cpuPercent;
    public Long diskBytes;
    public Long networkRxBytes;
    public Long networkTxBytes;
    public Long uptimeSeconds;
    
    public Integer playersOnline;
    public Double tps;
    public Double mspt;
    public Double currentTickTime;
    
    public Integer loadedChunks;

    public Long worldSeed;
    public Long worldTime;
    public Long worldDay;
    public Boolean isRaining;
    public String difficulty;
    
    public Long heapUsedBytes;
    public Long heapMaxBytes;

    public List<PlayerData> players = new ArrayList<>();

    public static class PlayerData {
        public String name;
        public String uuid;
        public int ping;
        public String dimension;
        public String gamemode;
        public int level;
        public double health;
        public double x;
        public double y;
        public double z;
    }

    private String safeNum(Number n) {
        if (n == null) return "null";
        if (n instanceof Double) return String.format(java.util.Locale.US, "%.2f", n);
        return n.toString();
    }

    private String safeStr(String s) {
        if (s == null) return "null";
        return "\"" + s + "\"";
    }

    private String safeBool(Boolean b) {
        if (b == null) return "null";
        return b.toString();
    }
    
    public String toJson() {
        return "{\n" +
            "  \"status\": \"running\",\n" +
            "  \"memory_bytes\": " + safeNum(memoryBytes) + ",\n" +
            "  \"cpu_percent\": " + safeNum(cpuPercent) + ",\n" +
            "  \"disk_bytes\": " + safeNum(diskBytes) + ",\n" +
            "  \"network_rx_bytes\": " + safeNum(networkRxBytes) + ",\n" +
            "  \"network_tx_bytes\": " + safeNum(networkTxBytes) + ",\n" +
            "  \"uptime_seconds\": " + safeNum(uptimeSeconds) + ",\n" +
            "  \"players_online\": " + safeNum(playersOnline) + ",\n" +
            "  \"tps\": " + safeNum(tps) + ",\n" +
            "  \"mspt\": " + safeNum(mspt) + ",\n" +
            "  \"current_tick_time\": " + safeNum(currentTickTime) + ",\n" +
            "  \"loaded_chunks\": " + safeNum(loadedChunks) + ",\n" +
            "  \"world_seed\": " + safeNum(worldSeed) + ",\n" +
            "  \"world_time\": " + safeNum(worldTime) + ",\n" +
            "  \"world_day\": " + safeNum(worldDay) + ",\n" +
            "  \"is_raining\": " + safeBool(isRaining) + ",\n" +
            "  \"difficulty\": " + safeStr(difficulty) + ",\n" +
            "  \"heap_used_bytes\": " + safeNum(heapUsedBytes) + ",\n" +
            "  \"heap_max_bytes\": " + safeNum(heapMaxBytes) + "\n" +
            "}";
    }

    public String toJsonSystem() {
        return "{\n" +
            "  \"memory_bytes\": " + safeNum(memoryBytes) + ",\n" +
            "  \"cpu_percent\": " + safeNum(cpuPercent) + ",\n" +
            "  \"disk_bytes\": " + safeNum(diskBytes) + ",\n" +
            "  \"network_rx_bytes\": " + safeNum(networkRxBytes) + ",\n" +
            "  \"network_tx_bytes\": " + safeNum(networkTxBytes) + ",\n" +
            "  \"uptime_seconds\": " + safeNum(uptimeSeconds) + "\n" +
            "}";
    }

    public String toJsonPlayers() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"players_online\": ").append(safeNum(playersOnline)).append(",\n");
        sb.append("  \"players\": [\n");
        for (int i = 0; i < players.size(); i++) {
            PlayerData p = players.get(i);
            sb.append("    {\n");
            sb.append("      \"name\": \"").append(p.name).append("\",\n");
            sb.append("      \"uuid\": \"").append(p.uuid).append("\",\n");
            sb.append("      \"ping\": ").append(p.ping).append(",\n");
            sb.append("      \"dimension\": \"").append(p.dimension).append("\",\n");
            sb.append("      \"gamemode\": \"").append(p.gamemode).append("\",\n");
            sb.append("      \"level\": ").append(p.level).append(",\n");
            sb.append("      \"health\": ").append(String.format(java.util.Locale.US, "%.1f", p.health)).append(",\n");
            sb.append("      \"x\": ").append(String.format(java.util.Locale.US, "%.1f", p.x)).append(",\n");
            sb.append("      \"y\": ").append(String.format(java.util.Locale.US, "%.1f", p.y)).append(",\n");
            sb.append("      \"z\": ").append(String.format(java.util.Locale.US, "%.1f", p.z)).append("\n");
            sb.append("    }");
            if (i < players.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }
}
