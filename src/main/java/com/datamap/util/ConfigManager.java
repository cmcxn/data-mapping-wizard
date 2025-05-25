package com.datamap.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final String CONFIG_FILENAME = "datasource.json";
    private static final String GLOBAL_CONFIG_FILENAME = "global-config.json";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".data-mapping-wizard";
    private static final String CONFIG_PATH = CONFIG_DIR + File.separator + CONFIG_FILENAME;
    private static final String GLOBAL_CONFIG_PATH = CONFIG_DIR + File.separator + GLOBAL_CONFIG_FILENAME;

    // 全局配置类
    public static class GlobalConfig {
        private String workingDirectory;

        public GlobalConfig() {
        }

        public String getWorkingDirectory() {
            return workingDirectory;
        }

        public void setWorkingDirectory(String workingDirectory) {
            this.workingDirectory = workingDirectory;
        }
    }

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

    public static String getGlobalConfigPath() {
        return GLOBAL_CONFIG_PATH;
    }

    /**
     * 保存全局配置
     */
    public static void saveGlobalConfig(String workingDirectory) throws IOException {
        GlobalConfig config = new GlobalConfig();
        config.setWorkingDirectory(workingDirectory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new File(GLOBAL_CONFIG_PATH), config);
    }

    /**
     * 加载全局配置
     */
    public static GlobalConfig loadGlobalConfig() throws IOException {
        File configFile = new File(GLOBAL_CONFIG_PATH);
        if (!configFile.exists()) {
            // 返回默认配置
            GlobalConfig defaultConfig = new GlobalConfig();
            defaultConfig.setWorkingDirectory(System.getProperty("user.home"));
            return defaultConfig;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(configFile, GlobalConfig.class);
    }

    /**
     * 获取当前工作目录
     */
    public static String getCurrentWorkingDirectory() {
        try {
            return loadGlobalConfig().getWorkingDirectory();
        } catch (IOException e) {
            // 返回默认目录
            return System.getProperty("user.home");
        }
    }
}