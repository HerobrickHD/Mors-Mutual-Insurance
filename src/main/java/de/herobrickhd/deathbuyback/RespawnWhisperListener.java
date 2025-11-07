package de.herobrickhd.deathbuyback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RespawnWhisperListener implements Listener {

    private final DeathSnapshotManager manager;
    private final Random random = new Random();

    public RespawnWhisperListener(DeathSnapshotManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!manager.hasPendingWhisper(uuid)) {
            return;
        }

        boolean usedFree = manager.hasUsedFree(uuid);

        double whisperChance = DeathBuybackPlugin.getInstance().getConfig()
                .getDouble("whisper_chance_after_first", 0.8);
        double rareChance = DeathBuybackPlugin.getInstance().getConfig()
                .getDouble("rare_message_chance", 0.05);

        // nach dem ersten Mal nur mit Chance
        if (usedFree && random.nextDouble() > whisperChance) {
            manager.clearPendingWhisper(uuid);
            return;
        }

        var cfg = DeathBuybackPlugin.getInstance().getConfig();
        List<String> normal = cfg.getStringList("respawn_messages");
        List<String> rare = cfg.getStringList("rare_messages");
        if (normal == null || normal.isEmpty()) {
            normal = List.of("&bWanderhändler:&7 Psst... Ich kann verlorene Inventare besorgen.");
        }

        boolean useRare = rare != null && !rare.isEmpty() && random.nextDouble() < rareChance;
        List<String> pool = useRare ? rare : normal;

        String chosen = pool.get(random.nextInt(pool.size()));

        // &-Farbcodes zu Adventure-Component
        Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(chosen);
        player.sendMessage(msg);

        manager.clearPendingWhisper(uuid);
    }
}
