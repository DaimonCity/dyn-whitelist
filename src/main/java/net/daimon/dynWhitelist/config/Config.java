package net.daimon.dynWhitelist.config;

import net.daimon.dynWhitelist.DynWhitelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final File CONFIG_FILE = new File("config/dyn-whitelist.properties");
    private static final Properties PROPS = new Properties();

    public static void createDefaultIfNotExists() {
        if (!CONFIG_FILE.exists()) {
            if (!CONFIG_FILE.getParentFile().mkdir()) {
                DynWhitelist.LOGGER.error("Unable to create or find the config directory.");
                return;
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {

                writer.write("# This mod was created to work with dynamically changing whitelist.\n");
                writer.write("# For this mod to work, you need to have an API with 2 endpoints: 200 and 403.\n");
                writer.write("# 200 means player can join the server, 403 not. In other cases,\n# player would get an error and won't be able to join\n");
                writer.write("###\n");
                writer.write("# JSON POST request sent by the mod to the API\n\n");
                writer.write("# { \"uuid\": \"<UUID of the joining player>\" }\n");
                writer.write("###\n");
                writer.write("# Base URL, usually for players\n");
                writer.write("api.players_url=http://127.0.0.1:3000/is_player_whitelisted\n");
                writer.write("# The url only for admins\n");
                writer.write("api.admins_url=http://127.0.0.1:3000/is_admin\n");
                writer.write("# default api.connectTimeout=3\n");
                writer.write("api.connectTimeout=3\n");
                writer.write("# default api.readTimeout=5\n");
                writer.write("api.readTimeout=5\n");
                writer.write("# api.mode=1 is admins' url, another nums are players'\n");
                writer.write("# default api.mode=0\n");
                writer.write("api.mode=0\n");
            } catch (IOException e) {
                DynWhitelist.LOGGER.error("Unable to create config file", e);
            }
        }
    }

    public static void load() {
        createDefaultIfNotExists(); // сначала создаём, если нет
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            PROPS.load(fis);
            DynWhitelist.LOGGER.info("WhitelistMod config loaded. API URL: {}", getApiUrl());
        } catch (IOException e) {
            DynWhitelist.LOGGER.error("Unable to load config file\n", e);
        }
    }

    public static String getApiUrl() {
        if (getMode() == 1) {
            return getAdminsApiUrl();
        }
        return getPlayersApiUrl();
    }
    private static String getPlayersApiUrl() {
        return PROPS.getProperty("api.players_url", "http://127.0.0.1:3000/is_player_whitelisted");
    }


    private static String getAdminsApiUrl() {
        return PROPS.getProperty("api.admins_url", "http://127.0.0.1:3000/is_admin");
    }

    public static int getConnectTimeout() {
        return Integer.parseInt(PROPS.getProperty("api.connectTimeout", "3"));
    }

    public static int getReadTimeout() {
        return Integer.parseInt(PROPS.getProperty("api.readTimeout", "5"));
    }

    public static int getMode() {
        return Integer.parseInt(PROPS.getProperty("api.mode", "0"));
    }
}
