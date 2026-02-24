package com.exometric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.SecureRandom;
import java.util.Base64;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "ExoMetric.json");
    private static Config instance;
    private static long lastModifiedTime = 0;

    public static class Config {
        public boolean api_enabled = true;
        public int api_port = 0; // 0 significa que o usuário precisa configurar
        public String api_token = "";
        
        // Comentários simulados via campos se necessário, mas JSON padrão não suporta.
        // Vamos usar nomes de campos descritivos.
    }

    public static Config getConfig() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            instance = new Config();
            instance.api_token = generateSecureToken();
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            lastModifiedTime = CONFIG_FILE.lastModified();
            instance = GSON.fromJson(reader, Config.class);
            
            // Validação e preenchimento automático
            boolean changed = false;
            if (instance.api_token == null || instance.api_token.isEmpty()) {
                instance.api_token = generateSecureToken();
                changed = true;
            }
            
            if (changed) save();
        } catch (Exception e) {
            e.printStackTrace();
            instance = new Config();
        }
    }

    public static void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(instance, writer);
            }
            lastModifiedTime = CONFIG_FILE.lastModified();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkAndReload() {
        if (CONFIG_FILE.exists() && CONFIG_FILE.lastModified() > lastModifiedTime) {
            load();
            return true;
        }
        return false;
    }

    private static String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
