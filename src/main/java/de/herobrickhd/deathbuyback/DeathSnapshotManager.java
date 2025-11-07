package de.herobrickhd.deathbuyback;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DeathSnapshotManager {
    private final DeathBuybackPlugin plugin;
    private final Map<UUID, Deque<DeathSnapshot>> cache = new HashMap<>();
    private final Set<UUID> pendingWhisper = new HashSet<>();
    private final int maxSnapshots = 7;
    private final File dataFolder;

    public DeathSnapshotManager(DeathBuybackPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        loadAll();
    }

    public void addSnapshot(UUID uuid, DeathSnapshot snapshot) {
        Deque<DeathSnapshot> list = cache.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        list.addFirst(snapshot);
        while (list.size() > maxSnapshots) {
            list.removeLast();
        }
        savePlayer(uuid);

        // wenn er free noch nicht benutzt hat, bekommt er nach Respawn einen Whisper
        if (!hasUsedFree(uuid)) {
            pendingWhisper.add(uuid);
        }
    }

    public List<DeathSnapshot> getSnapshots(UUID uuid) {
        Deque<DeathSnapshot> dq = cache.get(uuid);
        return dq != null ? new ArrayList<>(dq) : List.of();
    }

    public boolean hasPendingWhisper(UUID uuid) {
        return pendingWhisper.contains(uuid);
    }

    public void clearPendingWhisper(UUID uuid) {
        pendingWhisper.remove(uuid);
    }

    public boolean hasUsedFree(UUID uuid) {
        File f = getFile(uuid);
        if (!f.exists()) return false;
        return YamlConfiguration.loadConfiguration(f).getBoolean("used_free", false);
    }

    public void setUsedFree(UUID uuid, boolean used) {
        File f = getFile(uuid);
        YamlConfiguration cfg = f.exists() ? YamlConfiguration.loadConfiguration(f) : new YamlConfiguration();
        cfg.set("used_free", used);
        try {
            cfg.save(f);
        } catch (IOException ignored) {}
    }

    /**
     * kompletter Verlauf löschen – Flag bleibt erhalten!
     */
    public void clearSnapshots(UUID uuid) {
        cache.remove(uuid);

        File f = getFile(uuid);
        if (f.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            // nur die Tode löschen, used_free NICHT anfassen
            cfg.set("deaths", null);
            try {
                cfg.save(f);
            } catch (IOException e) {
                plugin.getLogger().warning("Konnte Verlauf für " + uuid + " nicht speichern: " + e.getMessage());
            }
        }

        pendingWhisper.remove(uuid);
    }

    /**
     * nur das free-Flag zurücksetzen – Verlauf bleibt erhalten!
     */
    public void resetUsedFree(UUID uuid) {
        setUsedFree(uuid, false);
        // wenn er wieder free hat, soll er beim nächsten Tod auch wieder geflüstert werden
        // (aber nicht zwingend jetzt)
    }
    
    /**
     * Entfernt einen bestimmten Tod aus dem Verlauf.
     */
    public void removeSnapshot(UUID uuid, int index) {
        Deque<DeathSnapshot> deque = cache.get(uuid);
        if (deque == null) return;

        List<DeathSnapshot> list = new ArrayList<>(deque);
        if (index < 0 || index >= list.size()) return;

        list.remove(index);

        Deque<DeathSnapshot> newDeque = new ArrayDeque<>();
        for (DeathSnapshot ds : list) {
            newDeque.addLast(ds);
        }

        cache.put(uuid, newDeque);
        savePlayer(uuid);
    }


    private File getFile(UUID id) {
        return new File(dataFolder, id + ".yml");
    }

    private void savePlayer(UUID uuid) {
        File f = getFile(uuid);
        YamlConfiguration cfg = new YamlConfiguration();

        Deque<DeathSnapshot> list = cache.get(uuid);
        if (list != null) {
            ConfigurationSection root = cfg.createSection("deaths");
            int i = 0;
            for (DeathSnapshot s : list) {
                s.toConfig(root.createSection(String.valueOf(i++)));
            }
        }

        if (hasUsedFree(uuid)) {
            cfg.set("used_free", true);
        }

        try {
            cfg.save(f);
        } catch (IOException e) {
            plugin.getLogger().warning(e.getMessage());
        }
    }

    private void loadAll() {
        File[] files = dataFolder.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            try {
                UUID id = UUID.fromString(f.getName().replace(".yml", ""));
                loadPlayer(id);
            } catch (Exception ignored) {}
        }
    }

    private void loadPlayer(UUID uuid) {
        File f = getFile(uuid);
        if (!f.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection sec = cfg.getConfigurationSection("deaths");
        if (sec == null) return;

        List<String> keys = new ArrayList<>(sec.getKeys(false));
        keys.sort(Comparator.comparingInt(Integer::parseInt));

        Deque<DeathSnapshot> q = new ArrayDeque<>();
        for (String k : keys) {
            ConfigurationSection s = sec.getConfigurationSection(k);
            if (s != null) {
                q.add(DeathSnapshot.fromConfig(s));
            }
        }
        cache.put(uuid, q);
    }
}
