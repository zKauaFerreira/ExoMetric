package com.exometric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class ExoMetric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Carrega configurações primeiro
        ConfigManager.load();
        
        // Start metric periodic collection
        MetricsCollector.start();
        
        // Start internal HTTP server
        StatsHttpServer.start();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MetricsCollector.setServer(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            MetricsCollector.stop();
            StatsHttpServer.stop();
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MetricsCollector.onTick();
        });

        // Registrar comando de reload
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("exometric")
                .requires(source -> source.hasPermissionLevel(4)) // Apenas admins
                .then(literal("reload")
                    .executes(context -> {
                        StatsHttpServer.reload();
                        context.getSource().sendFeedback(() -> Text.literal("§a[ExoMetric] Configurações recarregadas com sucesso!"), false);
                        return 1;
                    })
                )
            );
        });
        
        System.out.println("ExoMetric API initialized!");
    }
}
