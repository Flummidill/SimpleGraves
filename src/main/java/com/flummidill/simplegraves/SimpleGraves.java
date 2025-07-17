package com.flummidill.simplegraves;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

public class SimpleGraves extends JavaPlugin {

    private PlayerJoinListener playerJoinListener;
    private PlayerDeathListener playerDeathListener;
    private BlockBreakListener blockBreakListener;
    private GraveProtector graveProtector;
    private GraveManager graveManager;

    @Override
    public void onEnable() {
        getLogger().info("~ Created by Flummidill ~");

        // Initialize Grave-Manager
        getLogger().info("Initializing Grave-Manager...");
        initializeGraveManager();

        // Initialize Event Listeners
        getLogger().info("Initializing Event Listeners...");
        initializeEventListeners();

        // Load Configuration
        getLogger().info("Loading Configuration...");
        loadConfig();

        // Register Commands
        getLogger().info("Registering Commands...");
        registerCommands();

        // Check for Updates
        getLogger().info("Checking for Updates...");
        checkForUpdates();
    }

    private void initializeGraveManager() {
        graveManager = new GraveManager(this);
    }

    public void initializeEventListeners() {
        playerJoinListener = new PlayerJoinListener(this.graveManager);
        getServer().getPluginManager().registerEvents(playerJoinListener, this);
        playerDeathListener = new PlayerDeathListener(this.graveManager);
        getServer().getPluginManager().registerEvents(playerDeathListener, this);
        blockBreakListener = new BlockBreakListener(this.graveManager);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);
        graveProtector = new GraveProtector(this.graveManager);
        getServer().getPluginManager().registerEvents(graveProtector, this);
    }

    private void loadConfig() {
        boolean graveStealing = getConfig().getBoolean("grave-stealing", true);
        int maxStoredXP = getConfig().getInt("max-stored-xp", 25);
        String configVersion = getConfig().getString("config-version", "1.0.0");
        String currentVersion = getDescription().getVersion();

        saveResource("config.yml", true);
        reloadConfig();
        FileConfiguration config = getConfig();

        if (graveStealing == false) {
            blockBreakListener.disableGraveStealing();
        }
        config.set("grave-stealing", graveStealing);

        if (maxStoredXP < 0 || maxStoredXP > 100) {
            getLogger().warning("Configuration Error: \"max-stored-xp\" was configured incorrectly and reset to 25.");
            maxStoredXP = 25;
        }
        graveManager.setMaxStordXP(maxStoredXP);
        config.set("max-stored-xp", maxStoredXP);

        if ("1.0.0".equals(configVersion) || isNewerVersion(configVersion, "1.0.0")) {
            if (isOlderVersion(configVersion, currentVersion)) {
                getLogger().info("Configuration Update: \"config-version\" has been updated to \"" + currentVersion + "\".");
                configVersion = currentVersion;
            }
        } else {
            getLogger().warning("Configuration Error: \"config-version\" was configured incorrectly and reset to \"" + currentVersion + "\".");
            configVersion = currentVersion;
        }
        config.set("config-version", configVersion);

        saveConfig();
    }

    public void registerCommands() {
        CommandHandler commandHandler = new CommandHandler(graveManager);
        TabCompleter tabCompleter = new TabCompleter(graveManager);

        getCommand("graveinfo").setExecutor(commandHandler);
        getCommand("graveadmin").setExecutor(commandHandler);

        getCommand("graveinfo").setTabCompleter(tabCompleter);
        getCommand("graveadmin").setTabCompleter(tabCompleter);
    }

    private void checkForUpdates() {
        String[] latestVersion = getLatestVersion().split("\\|", 2);
        String currentVersion = getDescription().getVersion();

        if (!"error".equals(latestVersion[0])) {
            if (isNewerVersion(latestVersion[0], currentVersion)) {
                getLogger().warning("A new Version of SimpleGraves is available: " + latestVersion[0]);
                playerJoinListener.setUpdateAvailable(true);
            } else {
                getLogger().info("No new Updates available.");
            }
        } else {
            getLogger().warning("Failed to Check for Updates!\n" + latestVersion[1]);
        }
    }

    public String getLatestVersion() {
        String apiUrl = "https://api.github.com/repos/Flummidill/SimpleGraves/releases/latest";

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONObject json = new JSONObject(response.body());
                    return json.getString("tag_name").split("v")[1];
                } else {
                    return "error|java.net.ConnectException: Connection Failed with Code " + response.statusCode() + "\n        at SimpleGraves.jar//com.flummidill.simplegraves.SimpleGraves.getLatestVersion(SimpleGraves.java)";
                }
            } catch (IOException | InterruptedException e) {
                getLogger().warning("Failed to check for Updates!");

                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                return "error|" + stackTrace;
            }
        }
    }

    public boolean isNewerVersion(String comparingVersion, String referenceVersion) {
        String[] comparingVersionParts = comparingVersion.split("\\.");
        String[] referenceVersionParts = referenceVersion.split("\\.");

        for (int i = 0; i < 3; i++) {
            int comparingVersionPart = i < comparingVersionParts.length ? Integer.parseInt(comparingVersionParts[i]) : 0;
            int referenceVersionPart = i < referenceVersionParts.length ? Integer.parseInt(referenceVersionParts[i]) : 0;

            if (comparingVersionPart > referenceVersionPart) {
                return true;
            } else if (comparingVersionPart < referenceVersionPart) {
                return false;
            }
        }

        return false;
    }

    public boolean isOlderVersion(String comparingVersion, String referenceVersion) {
        String[] comparingVersionParts = comparingVersion.split("\\.");
        String[] referenceVersionParts = referenceVersion.split("\\.");

        for (int i = 0; i < 3; i++) {
            int comparingVersionPart = i < comparingVersionParts.length ? Integer.parseInt(comparingVersionParts[i]) : 0;
            int referenceVersionPart = i < referenceVersionParts.length ? Integer.parseInt(referenceVersionParts[i]) : 0;

            if (comparingVersionPart < referenceVersionPart) {
                return true;
            } else if (comparingVersionPart > referenceVersionPart) {
                return false;
            }
        }

        return false;
    }

    public void executeServerCommand(String cmd) {
        getServer().dispatchCommand(getServer().getConsoleSender(), cmd);
    }
}