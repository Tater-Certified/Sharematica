package com.github.tatercertified.sharematica.shared;

import com.github.tatercertified.sharematica.Sharematica;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;

public class Config {

    public final static Properties properties = new Properties();
    private final static String CONFIG_VERSION_KEY = "config-version";
    private final static String OPTIMIZED_NETWORKING_KEY = "use-raknet";
    private final static String OPEN_PORT_KEY = "use-upnp";
    private final static String PORT_KEY = "port";
    private final static String cfgver = "1.0";
    private static final Path config = FabricLoader.getInstance().getConfigDir().resolve("sharematica.properties");
    public static boolean upnp;
    public static boolean raknet;
    public static int port;

    public static void createConfig() {
        if (Files.notExists(config)) {
            try {
                storecfg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!(Objects.equals(properties.getProperty(CONFIG_VERSION_KEY), cfgver))) {
                properties.setProperty(CONFIG_VERSION_KEY, cfgver);
                try {
                    storecfg();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                parse();
            }
        }
    }

    /**
     * Save the config
     */
    private static void storecfg() throws IOException {
        try (OutputStream output = Files.newOutputStream(config, StandardOpenOption.CREATE)) {
            fillDefaults();
            properties.store(output, null);
        }
        parse();
    }

    /**
     * If the config value doesn't exist, set it to default
     */
    private static void fillDefaults() {
        if (!properties.containsKey(CONFIG_VERSION_KEY)) {
            properties.setProperty(CONFIG_VERSION_KEY, cfgver);
        }
        if (Objects.equals(Sharematica.ENVIRONMENT, "server")) {
            if (!properties.containsKey(OPTIMIZED_NETWORKING_KEY)) {
                properties.setProperty(OPTIMIZED_NETWORKING_KEY, "true");
            }
            if (!properties.containsKey(OPEN_PORT_KEY)) {
                properties.setProperty(OPEN_PORT_KEY, "false");
            }
            if (!properties.containsKey(PORT_KEY)) {
                properties.setProperty(PORT_KEY, "25565");
            }
        }
        if (Objects.equals(Sharematica.ENVIRONMENT, "client")) {
            // Add client-only configuration
        }
    }

    /**
     * Loads the config
     */
    private static void loadcfg() throws IOException {
        try (InputStream input = Files.newInputStream(config)) {
            properties.load(input);
        }
    }

    /**
     * Parses the config to convert into Objects
     */
    private static void parse() {
        fillDefaults();
        upnp = Boolean.parseBoolean(properties.getProperty(OPEN_PORT_KEY));
        raknet = Boolean.parseBoolean(properties.getProperty(OPTIMIZED_NETWORKING_KEY));
        port = Integer.parseInt(properties.getProperty(PORT_KEY));
    }

}
