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
        public int food;
        public float saturation;
        public double x;
        public double y;
        public double z;
        public String avatar_url;
        public long onlineSeconds;
        
        public ItemData mainHand;
        public ItemData offHand;
        
        public List<ItemData> armor = new ArrayList<>();
        public List<ItemData> hotbar = new ArrayList<>();
        public List<ItemData> mainInventory = new ArrayList<>();
    }

    public static class ItemData {
        public String id;
        public int count;
        public int slot;
        public String name;
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
            sb.append("      \"food\": ").append(p.food).append(",\n");
            sb.append("      \"saturation\": ").append(String.format(java.util.Locale.US, "%.1f", p.saturation)).append(",\n");
            sb.append("      \"x\": ").append(String.format(java.util.Locale.US, "%.1f", p.x)).append(",\n");
            sb.append("      \"y\": ").append(String.format(java.util.Locale.US, "%.1f", p.y)).append(",\n");
            sb.append("      \"z\": ").append(String.format(java.util.Locale.US, "%.1f", p.z)).append(",\n");
            sb.append("      \"online_seconds\": ").append(p.onlineSeconds).append(",\n");
            sb.append("      \"avatar_url\": \"").append(p.avatar_url).append("\",\n");
            
            sb.append("      \"main_hand\": ").append(itemToJson(p.mainHand)).append(",\n");
            sb.append("      \"off_hand\": ").append(itemToJson(p.offHand)).append(",\n");
            
            sb.append("      \"armor\": ").append(itemListToJson(p.armor)).append(",\n");
            sb.append("      \"hotbar\": ").append(itemListToJson(p.hotbar)).append(",\n");
            sb.append("      \"main_inventory\": ").append(itemListToJson(p.mainInventory)).append("\n");
            
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

    private String itemToJson(ItemData item) {
        if (item == null || item.id == null || item.id.equals("minecraft:air")) return "null";
        return "{\"id\": \"" + item.id + "\", \"count\": " + item.count + ", \"slot\": " + item.slot + ", \"name\": \"" + (item.name != null ? item.name : "") + "\"}";
    }

    private String itemListToJson(List<ItemData> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(itemToJson(list.get(i)));
            if (i < list.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
