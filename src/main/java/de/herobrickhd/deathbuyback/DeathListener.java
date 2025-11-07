package de.herobrickhd.deathbuyback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final DeathSnapshotManager manager;

    public DeathListener(DeathSnapshotManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Neues Adventure-API Feld
        Component deathMsgComponent = event.deathMessage();
        String deathMsg = "";
        if (deathMsgComponent != null) {
            deathMsg = PlainTextComponentSerializer.plainText().serialize(deathMsgComponent);
        }

        DeathSnapshot snapshot = new DeathSnapshot(
                player.getInventory().getContents(),
                player.getLevel(),
                System.currentTimeMillis(),
                deathMsg
        );

        manager.addSnapshot(player.getUniqueId(), snapshot);
    }
}
