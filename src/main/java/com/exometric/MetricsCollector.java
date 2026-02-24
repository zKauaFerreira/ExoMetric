package com.exometric;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsCollector {

    private static volatile MetricsData cachedMetrics = new MetricsData();
    private static ScheduledExecutorService scheduler;
    private static MinecraftServer activeServer = null;

    // TPS calculation
    private static final int TPS_SAMPLE_SIZE = 100;
    private static final long[] tickTimes = new long[TPS_SAMPLE_SIZE];
    private static int tickIndex = 0;
    private static long lastTickTime = System.currentTimeMillis();

    public static void setServer(MinecraftServer server) {
        activeServer = server;
    }

    public static void start() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(MetricsCollector::collect, 1, 5, TimeUnit.SECONDS);
        }
    }

    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        activeServer = null;
    }

    public static void onTick() {
        long now = System.currentTimeMillis();
        tickTimes[tickIndex] = now - lastTickTime;
        lastTickTime = now;
        tickIndex = (tickIndex + 1) % TPS_SAMPLE_SIZE;
    }

    private static void collect() {
        // Verifica se a config mudou antes de coletar
        try {
            if (ConfigManager.checkAndReload()) {
                StatsHttpServer.reload();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        MetricsData data = new MetricsData();
        
        try {
            data.memoryBytes = SystemMetrics.getMemoryUsageBytes();
            data.cpuPercent = SystemMetrics.getCpuUsagePercent();
            data.diskBytes = SystemMetrics.getDiskUsageBytes();
            Long[] net = SystemMetrics.getNetworkMetricsBytes();
            if (net != null && net.length == 2) {
                data.networkRxBytes = net[0];
                data.networkTxBytes = net[1];
            }
            data.uptimeSeconds = SystemMetrics.getUptimeSeconds();
            
            data.heapMaxBytes = Runtime.getRuntime().totalMemory();
            data.heapUsedBytes = data.heapMaxBytes - Runtime.getRuntime().freeMemory();

            if (activeServer != null) {
                try {
                    data.playersOnline = activeServer.getPlayerManager().getPlayerList().size();
                } catch (Throwable t) {
                    data.playersOnline = 0;
                }
                
                try {
                    for (ServerPlayerEntity player : activeServer.getPlayerManager().getPlayerList()) {
                        MetricsData.PlayerData pd = new MetricsData.PlayerData();
                        
                        try { pd.name = player.getName().getString(); } catch (Throwable t) { pd.name = "Unknown"; }
                        try { pd.uuid = player.getUuidAsString(); } catch (Throwable t) { pd.uuid = ""; }
                        
                        try {
                            pd.ping = player.networkHandler.getLatency();
                        } catch (Throwable t) {
                            pd.ping = 0;
                        }

                        try { 
                            pd.dimension = player.getWorld().getRegistryKey().getValue().toString(); 
                        } catch (Throwable t) { 
                            pd.dimension = "minecraft:overworld"; 
                        }
                        if (pd.dimension == null) pd.dimension = "minecraft:overworld";
                        try { pd.gamemode = player.interactionManager.getGameMode().getName().toUpperCase(); } catch (Throwable t) {}
                        try { pd.level = player.experienceLevel; } catch (Throwable t) {}
                        try { pd.health = player.getHealth(); } catch (Throwable t) {}
                        try { pd.x = player.getX(); } catch (Throwable t) {}
                        try { pd.y = player.getY(); } catch (Throwable t) {}
                        try { pd.z = player.getZ(); } catch (Throwable t) {}
                        
                        data.players.add(pd);
                    }
                } catch (Throwable t) {
                    // Ignora caso getPlayerList() quebre a iteração
                }

                try {
                    long total = 0;
                    for (long t : tickTimes) {
                        total += t;
                    }
                    double averageTickTime = (double) total / TPS_SAMPLE_SIZE;
                    data.mspt = averageTickTime;
                    
                    double tps = 1000.0 / averageTickTime;
                    if (tps > 20.0) tps = 20.0;
                    data.tps = tps;
                    
                    data.currentTickTime = (double) tickTimes[(tickIndex - 1 + TPS_SAMPLE_SIZE) % TPS_SAMPLE_SIZE];
                } catch (Throwable t) {}

                try {
                    int chunks = 0;
                    for (ServerWorld w : activeServer.getWorlds()) {
                        try { chunks += w.getChunkManager().getLoadedChunkCount(); } catch (Throwable t) {}
                    }
                    data.loadedChunks = chunks;
                } catch (Throwable t) {}

                try {
                    ServerWorld overworld = activeServer.getOverworld();
                    if (overworld != null) {
                        try { data.worldSeed = overworld.getSeed(); } catch (Throwable t) {}
                        try { data.worldTime = overworld.getTimeOfDay(); } catch (Throwable t) {}
                        try { data.worldDay = overworld.getTimeOfDay() / 24000L; } catch (Throwable t) {}
                        try { data.isRaining = overworld.isRaining(); } catch (Throwable t) {}
                    }
                    try { data.difficulty = activeServer.getSaveProperties().getDifficulty().getName(); } catch(Throwable t) {}
                } catch (Throwable t) {}
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        cachedMetrics = data;
    }

    public static MetricsData getLatestMetrics() {
        return cachedMetrics;
    }
}
