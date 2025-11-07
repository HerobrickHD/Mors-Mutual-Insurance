package de.herobrickhd.deathbuyback;

import org.bukkit.plugin.java.JavaPlugin;

public class DeathBuybackPlugin extends JavaPlugin {
    private static DeathBuybackPlugin instance;
    private DeathSnapshotManager snapshotManager;
    private TraderListener traderListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        snapshotManager = new DeathSnapshotManager(this);
        traderListener = new TraderListener(snapshotManager);

        getServer().getPluginManager().registerEvents(new DeathListener(snapshotManager), this);
        getServer().getPluginManager().registerEvents(traderListener, this);
        getServer().getPluginManager().registerEvents(new DeathBuybackGuiListener(snapshotManager), this);
        getServer().getPluginManager().registerEvents(new RespawnWhisperListener(snapshotManager), this);

        // Command-Executor registrieren
        if (getCommand("mmi") != null) {
            getCommand("mmi").setExecutor(new MmiCommand(this, snapshotManager));
        }

        getLogger().info("Mors Mutual Insurance aktiviert!");
    }

    public static DeathBuybackPlugin getInstance() {
        return instance;
    }

    public DeathSnapshotManager getSnapshotManager() {
        return snapshotManager;
    }

    public TraderListener getTraderListener() {
        return traderListener;
    }
}
