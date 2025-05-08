package com.datamap.util;

import java.io.File;

public class ConfigManager {
    private static final String CONFIG_FILENAME = "datasource.json";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".data-mapping-wizard";
    private static final String CONFIG_PATH = CONFIG_DIR + File.separator + CONFIG_FILENAME;
    static {
        // Ensure the config directory exists
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public static String getConfigPath() {
        return CONFIG_PATH;
    }

}
