package com.exometric;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Collection;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.lang.reflect.Method;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsCollector {

    private static volatile MetricsData cachedMetrics = new MetricsData();
    private static ScheduledExecutorService scheduler;
    private static MinecraftServer activeServer = null;
    
    // Rastreio de tempo online (UUID -> LoginTime)
    private static final Map<UUID, Long> loginTimes = new HashMap<>();

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
        try {
            if (ConfigManager.checkAndReload()) {
                StatsHttpServer.reload();
            }
        } catch (Throwable t) {}

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
                data.playersOnline = activeServer.getPlayerManager().getPlayerList().size();
                
                for (ServerPlayerEntity player : activeServer.getPlayerManager().getPlayerList()) {
                    try {
                        MetricsData.PlayerData pd = new MetricsData.PlayerData();
                        
                        pd.name = player.getName().getString();
                        pd.uuid = player.getUuidAsString();
                        
                        try {
                            pd.ping = player.networkHandler.getLatency();
                        } catch (Throwable t) { pd.ping = 0; }
                        
                        // FIX: Safe dimension retrieval to avoid NoSuchMethodError
                        pd.dimension = getDimensionSafe(player);
                        
                        try {
                            pd.gamemode = player.interactionManager.getGameMode().getName().toUpperCase();
                        } catch (Throwable t) { pd.gamemode = "SURVIVAL"; }
                        
                        pd.level = player.experienceLevel;
                        pd.health = player.getHealth();
                        
                        try {
                            pd.food = player.getHungerManager().getFoodLevel();
                            pd.saturation = player.getHungerManager().getSaturationLevel();
                        } catch (Throwable t) {
                            pd.food = 20;
                            pd.saturation = 5.0f;
                        }

                        pd.x = player.getX();
                        pd.y = player.getY();
                        pd.z = player.getZ();
                        
                        // Tempo Online
                        UUID u = player.getUuid();
                        if (!loginTimes.containsKey(u)) {
                            loginTimes.put(u, System.currentTimeMillis());
                        }
                        pd.onlineSeconds = (System.currentTimeMillis() - loginTimes.getOrDefault(u, System.currentTimeMillis())) / 1000;

                        // Skin / Avatar
                        String skinIdentifier = getSkinIdentifierFromProfile(player);
                        if (skinIdentifier == null) skinIdentifier = player.getUuidAsString();
                        pd.avatar_url = "https://mc-heads.net/avatar/" + skinIdentifier + "/64";
                        
                        // Itens nas mãos
                        pd.mainHand = convertItem(player.getMainHandStack(), -1);
                        pd.offHand = convertItem(player.getOffHandStack(), 40);
                        
                        // Hotbar (0-8)
                        for (int i = 0; i < 9; i++) {
                            ItemStack stack = player.getInventory().getStack(i);
                            if (!stack.isEmpty()) pd.hotbar.add(convertItem(stack, i));
                        }
                        
                        // Main Inventory (9-35)
                        for (int i = 9; i < 36; i++) {
                            ItemStack stack = player.getInventory().getStack(i);
                            if (!stack.isEmpty()) pd.mainInventory.add(convertItem(stack, i));
                        }
                        
                        // Armor (36-39)
                        for (int i = 36; i < 40; i++) {
                            ItemStack stack = player.getInventory().getStack(i);
                            if (!stack.isEmpty()) pd.armor.add(convertItem(stack, i));
                        }
                        
                        data.players.add(pd);
                    } catch (Throwable pt) {
                        pt.printStackTrace();
                    }
                }
                
                loginTimes.keySet().removeIf(id -> activeServer.getPlayerManager().getPlayer(id) == null);

                // TPS
                long total = 0;
                for (long t : tickTimes) total += t;
                double avg = (double) total / TPS_SAMPLE_SIZE;
                data.mspt = avg;
                data.tps = Math.min(20.0, 1000.0 / avg);
                data.currentTickTime = (double) tickTimes[(tickIndex - 1 + TPS_SAMPLE_SIZE) % TPS_SAMPLE_SIZE];

                // World stats
                int chunks = 0;
                for (ServerWorld w : activeServer.getWorlds()) chunks += w.getChunkManager().getLoadedChunkCount();
                data.loadedChunks = chunks;

                ServerWorld overworld = activeServer.getOverworld();
                if (overworld != null) {
                    data.worldSeed = overworld.getSeed();
                    data.worldTime = overworld.getTimeOfDay();
                    data.worldDay = overworld.getTimeOfDay() / 24000L;
                    data.isRaining = overworld.isRaining();
                }
                data.difficulty = activeServer.getSaveProperties().getDifficulty().getName();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        cachedMetrics = data;
    }

    private static String getDimensionSafe(ServerPlayerEntity player) {
        try {
            // Tenta o método direto primeiro
            return player.getWorld().getRegistryKey().getValue().toString();
        } catch (Throwable t) {
            try {
                // Tenta via reflection buscando QUALQUER método que retorne um objeto tipo World/ServerWorld
                for (Method m : player.getClass().getMethods()) {
                    if (m.getParameterCount() == 0) {
                        Class<?> ret = m.getReturnType();
                        if (ret.getName().endsWith("World") || ret.getName().endsWith("ServerWorld")) {
                            Object world = m.invoke(player);
                            if (world != null) {
                                // Usa reflexão para pegar a RegistryKey do mundo retornado
                                Method getReg = world.getClass().getMethod("getRegistryKey");
                                Object regKey = getReg.invoke(world);
                                Method getVal = regKey.getClass().getMethod("getValue");
                                Object ident = getVal.invoke(regKey);
                                return ident.toString();
                            }
                        }
                    }
                }
            } catch (Throwable t2) {}
        }
        return "minecraft:overworld";
    }

    private static MetricsData.ItemData convertItem(ItemStack stack, int slot) {
        if (stack == null || stack.isEmpty()) return null;
        MetricsData.ItemData item = new MetricsData.ItemData();
        item.id = Registries.ITEM.getId(stack.getItem()).toString();
        item.count = stack.getCount();
        item.slot = slot;
        item.name = stack.getName().getString();
        return item;
    }

    private static String getSkinIdentifierFromProfile(ServerPlayerEntity player) {
        try {
            GameProfile profile = player.getGameProfile();
            Collection<Property> textures = profile.getProperties().get("textures");
            
            for (Property property : textures) {
                String value = property.value();
                if (value == null || value.isEmpty()) continue;
                String decoded = new String(Base64.getDecoder().decode(value));
                if (decoded.contains("\"url\":")) {
                    int start = decoded.indexOf("\"url\":\"") + 7;
                    int end = decoded.indexOf("\"", start);
                    String url = decoded.substring(start, end);
                    if (url.contains("/")) return url.substring(url.lastIndexOf('/') + 1);
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static MetricsData getLatestMetrics() {
        return cachedMetrics;
    }
}
